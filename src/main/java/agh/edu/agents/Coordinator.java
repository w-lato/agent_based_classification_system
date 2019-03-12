//package agh.edu.agents;
//
//import agh.edu.agents.enums.S_Type;
//import agh.edu.agents.experiment.RunConf;
//import akka.actor.AbstractActor;
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import weka.classifiers.evaluation.Prediction;
//import weka.core.Instances;
//import weka.core.converters.ConverterUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static agh.edu.agents.enums.S_Type.PART;
//import static agh.edu.agents.enums.Split.SIMPLE;
//import static agh.edu.agents.enums.Vote.WEIGHTED;
//
//public class Coordinator extends AbstractActor {
//
//    private List<ActorRef> slaves;
//    private List<ActorRef> learners;
//    private ActorRef aggregator;
//    private ActorRef splitter;
//
//    private Vote vote_method;
//
//    static public Props props(Boolean withGUI) {
//        return Props.create(Master.class, () -> new Master(withGUI));
//    }
//
//
//    ///////////////////////////////////////////////////////////////////////////////////////////
//    //
//    //
//    //                                    HANDLERS
//
//    private void onTest(Master.EvaluateTest m) throws Exception
//    {
////        for (ActorRef actor : slaves
////        ) {
////            // todo tell to test batch file
////            actor.tell( new  , ActorRef.noSender());
////        }
//
//    }
//
//    private void onConfig(RunConf c) throws Exception
//    {
//        if (slaves != null && slaves.size() != 0) {
//            for (ActorRef x : slaves) {
//                x.tell(new Master.Kill(), ActorRef.noSender());
//            }
//            slaves.clear();
//        }
//        vote_method = c.class_method;
//        slaves = new ArrayList<>();
//        Map<ActorRef, S_Type> types = new HashMap<>();
//        // todo send data with instances and other to splitter
//
//        // todo aggregator
//    }
//
//    private void onVotingChange( SetVoting sv )
//    {
//        System.out.println( "VOTING SET TO: "+ sv.method);
//        this.vote_method = sv.method;
//    }
//
//    ///////////////////////////////////////////////////////////////////////////////////////////
//    //
//    //
//    //                                    MESSAGES
//
//
//
//    static public class SetVoting {
//        public final Vote method;
//        public SetVoting(Vote method) {
//            this.method = method;
//        }
//    }
//
//    static public class GetList {}
//
//    static public class EvaluateTest {}
//
//    static public class Kill {}
//
//    public static class WekaEvalFinished
//    {
//        final Map<ActorRef,List<Prediction>> results;
//        public WekaEvalFinished(Map<ActorRef,List<Prediction>> results)
//        {
//            this.results = results;
//        }
//    }
//
//    public static class EvalFinished
//    {
//        final List<Prediction> data;
//        final ActorRef who;
//
//        public EvalFinished(List<Prediction> data, ActorRef who) {
//            this.data = data;
//            this.who = who;
//        }
//    }
//
//    @Override
//    public AbstractActor.Receive createReceive() {
//        return receiveBuilder()
//                .match(Master.GetList.class, x -> {
//                    slaves.forEach( slave -> {
//                        slave.tell( new M.Classify("abc"), self() );
//                    } );
//
//                    System.out.println( "childs" );
//                    int ctr = 0;
//                    for (int i = 0; i <1000; i++)
//                    {
//                        ctr += 2;
//                    }
//
//                    getContext().getChildren().forEach( child -> {
//                        child.tell( new M.Classify("abc"), self() );
//                    });
//                })
//                .match(  SetVoting.class, this::onVotingChange)
////                .match(  EvaluateTest.class, this::onTest)
////                .match(  WekaEvalFinished.class, this::onEvalFinished)
//                .match(  RunConf.class, this::onConfig)
//                .matchAny(o -> { System.out.println("Master received unknown message: " + o); })
//                .build();
//    }
//
//
//
//    public static void main(String[] args)
//    {
//        ActorSystem system = ActorSystem.create("testSystem");
//        ConverterUtils.DataSource source = null;
//        try {
//            source = new ConverterUtils.DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
//            Instances train = source.getDataSet();
//            source = new ConverterUtils.DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_test.arff");
//            Instances test = source.getDataSet();
//            test.setClassIndex( test.numAttributes() - 1 );
//            ActorRef m = system.actorOf( Master.props(true) ,"master" );
//
//
//            RunConf RC = new RunConf.Builder()
//                    .agents(new S_Type[]{
//                            PART, PART, PART, PART,
//                            PART, PART, PART, PART
//                    })
//                    .split_meth(SIMPLE)
//                    .class_method(WEIGHTED)
//                    .ol_ratio(0.5)
//                    .split_ratio(0.9)
//                    .train(train)
//                    .test(test)
//                    .build();
//
//            m.tell( RC, ActorRef.noSender() );
//            m.tell( new Master.EvaluateTest(), ActorRef.noSender() );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}