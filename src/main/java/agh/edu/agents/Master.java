package agh.edu.agents;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.ClassSlave.ClassSetup;
import agh.edu.agents.experiment.*;
import agh.edu.agents.experiment.Loader.LoadExp;
import akka.actor.*;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static agh.edu.agents.enums.Split.SIMPLE;

// TODO handle the class strategy - only one thing has to be changes in order to check different results
// tODO handle slaves save and load - persistent
// TODO kill scenario
// TODO do we need map with Learners and types?
public class Master extends AbstractActorWithStash {
    private final String dir_prefix = "EXP/";
    private boolean exp_processing  = false;

    private RunConf curr;

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
            aggregator = getContext().actorOf(Aggregator.props( self(), c.getClass_method() ));
            // setup slaves and learners
            String exp_id = Saver.setupNewExp( c.getConf_name() );
            S_Type[] agents = c.getAgents();
            System.out.println( agents.length);
            for (int i = 0; i < N; i++)
            {
                Instances cur_data = l.get(i);
                S_Type cur_type = agents[i];
                String model_id = exp_id + "/" + cur_type + "_" + Saver.getIntID();
                System.out.println( model_id );
                ActorRef new_slave = getContext().actorOf( ClassSlave.props( new ClassSetup( aggregator, cur_type, model_id ) ) );
                slaves.put( new_slave, cur_type );
                data_split.put( new_slave, l.get(i) );
                ActorRef new_learner = getContext().actorOf( Learner.props(model_id, cur_type, cur_data, new_slave));
                learners.put( new_learner, cur_type );
            }
            System.out.println(" ALL SETUP ");
        } else {
            stash();
        }
    }

    private void onReset(String s)
    {
        if( !slaves.isEmpty() )
        {
            slaves.forEach( (k,v)-> k.tell( PoisonPill.class, ActorRef.noSender() ) );
            slaves.clear();
        }
        if( !learners.isEmpty() )
        {
            learners.forEach( (k,v)-> k.tell( PoisonPill.class, ActorRef.noSender() ) );
            learners.clear();
        }
        if( aggregator != null ) aggregator.tell( PoisonPill.class, ActorRef.noSender() );
        if( !data_split.isEmpty() ) data_split.clear();
        curr = null;
        exp_processing = false;
    }

    private void onLoad(LoadExp le) throws Exception
    {
        if( !exp_processing )
        {
            exp_processing = true;
            onReset("");

            RunConf rc = ConfParser.getConfFrom( le.getConf_path() );
            this.curr = rc;

            String dir = le.getExp_dir_path();
            Set<String> IDs = Files.walk( Paths.get( dir ) )
                    .map( x -> x.getFileName().toString().split(".")[0] )
                    .collect(Collectors.toSet());

            for (String id : IDs)
            {
                String model_id = dir + id;
                Path c_p = Paths.get( dir + id + ".conf" );
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
            // aggr
            aggregator = getContext().actorOf(Aggregator.props( self(), rc.getClass_method() ));
        }
        else {
            stash();
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES



    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
//                .matchEquals("STOP", getContext().stop(self()))
                .matchEquals("RESET", this::onReset)
                .match(  RunConf.class, this::onConfig)
                .match(  LoadExp.class, this::onLoad)
                .match(  PoisonPill.class, x -> getContext().stop(self()))
                .matchAny(o -> { System.out.println("Master received unknown message: " + o); })
                .build();
    }
}