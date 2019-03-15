package agh.edu.agents;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.experiment.ConfParser;
import agh.edu.agents.experiment.RunConf;
import agh.edu.agents.experiment.Saver;
import agh.edu.agents.experiment.Splitter;
import akka.actor.*;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static agh.edu.agents.enums.Split.SIMPLE;

// TODO handle the class strategy - only one thing has to be changes in order to check different results
// tODO handle slaves save and load - persistent
// TODO kill scenario
public class Master extends AbstractActorWithStash {
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
        System.out.println(" ----------------------- "+ c.getConf_name());
        if( !exp_processing )
        {
            this.curr = c;
            int N = c.getAgents().length;
            List<Instances> l = c.getSplit_meth().equals(SIMPLE) ? Splitter.equalSplit( c.getTrain(), N )
                    : Splitter.fillSplit( c.getTrain(), N, c.getFill().get() );

            // setup aggregator
            aggregator = getContext().actorOf(Aggregator.props( self(), c.getClass_method() ));

            // setup slaves and learners
            String exp_id = Saver.setupNewExp( c.getConf_name() );
            S_Type[] agents = c.getAgents();
            for (int i = 0; i < N; i++)
            {
                Instances cur_data = l.get(i);
                S_Type cur_type = agents[i];

                ActorRef new_slave = getContext().actorOf( ClassSlave.props( new ClassSlave.ClassSetup( aggregator, cur_data, cur_type ) ) );
                slaves.put( new_slave, cur_type );
                data_split.put( new_slave, l.get(i) );
                ActorRef new_learner = getContext().actorOf( Learner.props(exp_id, cur_type, cur_data, new_slave));
                learners.put( new_learner, cur_type );
            }
            System.out.println(" ALL SETUP ");
            exp_processing = true;
        } else {
            stash();
        }

//        if (slaves != null && slaves.size() != 0) {
//            for (ActorRef x : slaves) {
//                x.tell(new Master.Kill(), ActorRef.noSender());
//            }
//            slaves.clear();
//        }
//        vote_method = c.class_method;
//        slaves = new ArrayList<>();
//        Map<ActorRef, S_Type> types = new HashMap<>();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES


    static public class GetList {}

    static public class EvaluateTest {}

    static public class Kill {}

    public static class WekaEvalFinished
    {
        final Map<ActorRef,List<Prediction>> results;
        public WekaEvalFinished(Map<ActorRef,List<Prediction>> results)
        {
            this.results = results;
        }
    }

    public static class EvalFinished
    {
        final List<Prediction> data;
        final ActorRef who;

        public EvalFinished(List<Prediction> data, ActorRef who) {
            this.data = data;
            this.who = who;
        }
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
//                .matchEquals("STOP", getContext().stop(self()))
                .match(  RunConf.class, this::onConfig)
                .matchAny(o -> { System.out.println("Master received unknown message: " + o); })
                .build();
    }



    public static void main(String[] args)
    {
        ActorSystem system = ActorSystem.create("testSystem");
        ConverterUtils.DataSource source = null;
        try {

//            source = new ConverterUtils.DataSource( "DATA\\mnist_train.arff");
            source = new ConverterUtils.DataSource( "DATA\\spambase.arff");
            Instances train = source.getDataSet();
            source = new ConverterUtils.DataSource( "DATA\\mnist_test.arff");
            Instances test = source.getDataSet();
            ActorRef m = system.actorOf( Master.props() ,"master" );



            RunConf RC = new RunConf.Builder()
                    .conf_name("TEST_CONF")
                    .agents(new S_Type[]{
//                            S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,
//                            S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,
                            S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP,S_Type.MLP
                    })
                    .split_meth(SIMPLE)
                    .class_method( ClassStrat.WEIGHTED )
                    .train( train )
//                    .test( test )
                    .build();

            RC = ConfParser.getConfFrom( "CONF/TEST_CASE" );
            m.tell( RC, ActorRef.noSender() );
            m.tell( new Master.EvaluateTest(), ActorRef.noSender() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}