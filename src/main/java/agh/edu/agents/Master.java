package agh.edu.agents;

import agh.edu.agents.ClassSlave.ClassSetup;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.experiment.*;
import akka.actor.*;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static agh.edu.agents.enums.Split.SIMPLE;

// TODO handle the class strategy - only one thing has to be changes in order to check different results
// tODO handle slaves save and load - persistent
// TODO do we need map with Learners and types?
public class Master extends AbstractActorWithStash {
    private final String dir_prefix = "EXP/";
    private final String agg_postfix = "/AGG/";
    private boolean exp_processing  = false;

    private RunConf curr;
    private String exp_path;

    private Map<ActorRef,S_Type> slaves;
    private Map<ActorRef,S_Type> learners;
    private Map<ActorRef,Instances> data_split;
    private ActorRef aggregator;


    public Master()
    {
        slaves = new HashMap<>();
        learners = new HashMap<>();
        data_split = new HashMap<>();
    }
    static public Props props() { return Props.create(Master.class, Master::new); }


    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    HANDLERS

    private void onReset(String s)
    {
        if( !slaves.isEmpty() )
        {
            slaves.forEach( (k,v)-> k.tell( PoisonPill.getInstance(), ActorRef.noSender() ) );
            slaves.clear();
        }
        if( !learners.isEmpty() )
        {
            learners.forEach( (k,v)-> k.tell( PoisonPill.getInstance(), ActorRef.noSender() ) );
            learners.clear();
        }
        if( aggregator != null ) aggregator.tell( PoisonPill.getInstance(), ActorRef.noSender() );
        if( !data_split.isEmpty() ) data_split.clear();
        curr = null;
        exp_processing = false;
    }

    private void onKill(String s)
    {
        if( !slaves.isEmpty() )
        {
            slaves.forEach( (k,v)-> k.tell( Kill.getInstance(), ActorRef.noSender() ) );
            slaves.clear();
        }
        if( !learners.isEmpty() )
        {
            learners.forEach( (k,v)-> k.tell( Kill.getInstance(), ActorRef.noSender() ) );
            learners.clear();
        }
        if( aggregator != null ) aggregator.tell( Kill.getInstance(), ActorRef.noSender() );
        if( !data_split.isEmpty() ) data_split.clear();
        curr = null;
        exp_processing = false;
    }

    // TODO handle new setup - when current experiment is finished
    // TODO kill slaves agents during new setup
    // TODO when to unstash?
    private void onConfig(RunConf c) throws Exception
    {
        if( !exp_processing )
        {
            exp_processing = true;
            onReset("");

            System.out.println(" -----------!!------------ "+ c.getConf_name());
            this.curr = c;
            int N = c.getAgents().length;
            List<Instances> l = c.getSplit_meth().equals(SIMPLE) ? Splitter.equalSplit( c.getTrain(), N )
                    : Splitter.fillSplit( c.getTrain(), N, c.getFill().get() );

            // setup aggregator
            String exp_id = Saver.setupNewExp( c.getConf_name() );
            aggregator = getContext().actorOf(Aggregator.props( self(), exp_id ));

            // setup slaves and learners
            S_Type[] agents = c.getAgents();
            for (int i = 0; i < N; i++)
            {
                Instances cur_data = l.get(i);
                S_Type cur_type = agents[i];
                String model_id = exp_id + "/" + cur_type + "_" + Saver.getIntID();

                // slaves
                ActorRef new_slave = getContext().actorOf( ClassSlave.props( new ClassSetup( aggregator, cur_type, model_id ) ) );
                slaves.put( new_slave, cur_type );
                data_split.put( new_slave, l.get(i) );

                // learners
                ActorRef new_learner = getContext().actorOf( Learner.props(model_id, cur_type, cur_data, new_slave));
                learners.put( new_learner, cur_type );
            }
            System.out.println(" !! ALL SETUP ");
        } else {
            stash();
        }
    }



    private void onLoad(LoadExp le) throws Exception
    {
        if( !exp_processing )
        {
            exp_path = le.exp_dir_path;
            loadRunConf( le.conf_path, exp_path );
            Set<String> IDs = getModelIDs( exp_path);

            for (String id : IDs)
            {
                String model_id = exp_path +"/"+ id;
                Path c_p = Paths.get( model_id + ".conf" );
                String m_p = model_id  + ".model";
                String d_p =  model_id + ".arff";

                S_Type type = Loader.getType( c_p );
                LinkedHashMap<String, Double> used_confs = Loader.getConfigs( c_p );
                Classifier model = Loader.getModel( m_p );
                Instances data = Loader.getData( d_p );

                // slave
                ActorRef new_slave = getContext().actorOf( ClassSlave.props( new ClassSetup( aggregator, type, model_id) ) );
                slaves.put( new_slave , type );

                // learner
                ActorRef new_learner = getContext().actorOf( Learner.props(model_id,model,type,data, new_slave, used_confs));
                learners.put( new_learner, type );
            }
        }
        else {
            stash();
        }
    }

    private void onSlaveOnly(SlaveOnlyExp conf) throws Exception {
        if( !exp_processing )
        {
            exp_path = conf.exp_dir_path;
            loadRunConf( conf.conf_path, exp_path );
            Set<String> IDs = getModelIDs( exp_path );

            for( String id : IDs)
            {
                String model_id = exp_path + "/" + id;
                Path c_p = Paths.get( model_id + ".conf" );
                String m_p = model_id  + ".model";

                S_Type type = Loader.getType( c_p );
                Classifier model = Loader.getModel( m_p );

                // slave
                ActorRef new_slave = getContext().actorOf( ClassSlave.props( new ClassSetup( aggregator, type, model_id), model ) );
                slaves.put( new_slave , type );
            }
        }
        else
        {
            stash();
        }
    }

    private void loadRunConf(String conf_path, String exp_dir_path) throws IOException {
        exp_processing = true;
        onReset("");
        RunConf rc = ConfParser.getConfFrom( conf_path );
        this.curr = rc;

        // aggr
        Aggregator.AggSetup setup = Loader.getAggSetup( Paths.get(exp_dir_path + agg_postfix+ "/agg.conf"), self() );
        aggregator = getContext().actorOf(Aggregator.props( setup ));
    }

    private Set<String> getModelIDs(String exp_id) throws IOException {
        return Files.list( Paths.get( exp_id ) )
                .filter( x-> !Files.isDirectory(x) )
                .map( x ->  {
                    String str = x.getFileName().toString();
                    return str.substring(0,str.lastIndexOf("."));
                })
                .collect(Collectors.toSet());

    }

    private void onLabel(Instances data) throws IOException
    {
        int query_id = Loader.getNextQueryID(exp_path + "/AGG");
        data.setClassIndex( data.numAttributes() -1 );

        // save query data
        Saver.saveQueryData( exp_path, query_id, data );
        ClassSlave.Query q = new ClassSlave.Query( query_id,data );
        for (ActorRef slave : slaves.keySet())
        {
            slave.tell( q, self() );
        }
        System.out.println( "   :!: QUERY " + query_id + " SENT TO SLAVES" );
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES

    public static final class LoadExp
    {
        private final String exp_dir_path;
        private final String conf_path;

        public LoadExp(String exp_dir_path) throws IOException
        {
            this.exp_dir_path = exp_dir_path;
            String conf_name = exp_dir_path.substring( exp_dir_path.lastIndexOf("/") + 1, exp_dir_path.lastIndexOf("_") );
            conf_path = "CONF/" + conf_name;
        }
    }

    public static final class SlaveOnlyExp
    {
        private final String exp_dir_path;
        private final String conf_path;

        public SlaveOnlyExp(String exp_dir_path) throws IOException
        {
            this.exp_dir_path = exp_dir_path;
            String conf_name = exp_dir_path.substring( exp_dir_path.lastIndexOf("/") + 1, exp_dir_path.lastIndexOf("_") );
            conf_path = "CONF/" + conf_name;
        }
    }



    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()

                // classify
                .match(  Instances.class,    this::onLabel)

                // configs
                .match(  RunConf.class,      this::onConfig)
                .match(  LoadExp.class,      this::onLoad)
                .match(  SlaveOnlyExp.class, this::onSlaveOnly)

                // others
                .matchEquals("RESET",          this::onReset)
                .matchEquals("INSTANT_KILL",   this::onKill)
                .match(             PoisonPill.class, x -> getContext().stop(self()))
                .matchAny(                            o -> { System.out.println("Master received unknown message: " + o); })
                .build();
    }

    @Override
    public void postStop()  {
        super.postStop();
        System.out.println( "MASTER STOPPED: ");
    }
}