package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.agents.enums.Vote;
import agh.edu.learning.DataSplitter;
import agh.edu.messages.M;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static agh.edu.agents.enums.S_Type.*;
import static agh.edu.agents.enums.Split.SIMPLE;
import static agh.edu.agents.enums.Vote.*;
import static akka.pattern.Patterns.ask;


/**

 ArrayList<Future<Object>> resps = new ArrayList<>();
 IntStream.range(0,N).forEach( i-> {
 resps.add(Patterns.ask(slaves.get(i), new Slave.WekaTrain(train.get(i)), timeout));
 //            Patterns.ask(slaves.get(i), new Slave.WekaTrain(train.get(i)), timeout);
 });
 System.out.println( resps.size() );
 for (int i = 0; i < resps.size(); i++)
 {
 resps.get( i ).onComplete(new OnComplete<Object>()
 {
 public void onComplete(Throwable t, Object result){
 System.out.println("OK ");
 }
 }, getContext().dispatcher());
 }
 */
public class Master extends AbstractActor
{
    private List<ActorRef> slaves;
    private List<Instances> train;
    private Instances eval;
    private Instances test;

    private Map<ActorRef, Boolean> isReady;
    private Map<ActorRef, Double> weight;
    private ClassChooser cc;

    private Vote vote_method;

    static public Props props() {
        return Props.create(Master.class);
    }
    public Master()
    {
        System.out.println("default constructor");
    }


    private void onKill(Kill m)
    {
        for (int i = 0; i < m.num; i++)
        {
            getContext().stop( slaves.get( i ) );
        }
        slaves.removeIf(ActorRef::isTerminated);
    }

    private void onTest(EvaluateTest m) throws Exception
    {
        System.out.println( "TEST START: " + System.currentTimeMillis() );

        Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
        ArrayList<Future<Object>> resps = new ArrayList<>();
        for (int i = 0; i < slaves.size(); i++)
            resps.add(Patterns.ask(slaves.get(i), new Slave.WekaEvaluate( test ), timeout));

        Map<ActorRef, List<Prediction>> test_res = new HashMap<>();
        for (Future<Object> resp : resps)
        {
            EvalFinished res = ((EvalFinished) Await.result(resp, timeout.duration()));
            test_res.put( res.who, res.data );
            System.out.println( res.data.size() + " : " + test.size() );
        }
        List<Integer> preds = cc.chooseClasses( test_res, vote_method );
        System.out.println( "ALL TESTED" );
        int ctr = 0;
        for (int i = 0; i < preds.size(); i++)
        {
            if( test_res.get( slaves.get(0) ).get(i).actual() == preds.get(i) ) ctr++;
        }
        System.out.println( "TEST: " + (double)(100.0 * (double)ctr / preds.size()) + "%");
//        ActorRef gh = getContext().actorOf(
//                WekaGroupHandler.props(slaves, self(),
//                        new FiniteDuration(15, TimeUnit.MINUTES)));
//        gh.tell( new WekaGroupHandler.EvalData(test), self() );
    }

    private void onEvalFinished(WekaEvalFinished m)
    {
        System.out.println(" EVAL FINISHED " + System.currentTimeMillis());
        List<Integer> p = cc.chooseClasses( m.results, Vote.WEIGHTED );
        List<Prediction> test = m.results.entrySet().iterator().next().getValue();

        int ctr = 0;
        for (int i = 0; i < test.size(); i++) {
            if (p.get(i) == test.get(i).actual()) ctr++;
        }
        System.out.println("============ " + (((double) ctr) / ((double) p.size()) + " %"));
    }

    private void onConfig( RunConf c ) throws Exception {
        // init slaves
        vote_method = c.class_method;
        cc = new ClassChooser();
        int N = c.agents.length;
        slaves = new ArrayList<>();
        isReady = new HashMap<>();
        weight = new HashMap<>();

        for (int i = 0; i < N; i++)
        {
            ActorRef it = getContext().actorOf( Slave.props( c.agents[i] ));
            slaves.add( it );
            isReady.put(it, false);
            weight.put(it, 0.0);
        }


        // data setup
        List<Instances> x = DataSplitter.splitIntoTrainAndTest( c.train, c.split_ratio );
        train = DataSplitter.split( x.get(0), N, c.split_meth, c.ol_ratio );
        this.eval = x.get(1);
        test = c.test;

        Timeout timeout = new Timeout(10, TimeUnit.MINUTES);
        ArrayList<Future<Object>> resps = new ArrayList<>();
        for (int i = 0; i < N; i++)
            resps.add(Patterns.ask(slaves.get(i), new Slave.WekaTrain(train.get(i)), timeout));

        for (Future<Object> resp : resps)
        {
            Await.result(resp, timeout.duration() );
        }
        System.out.println( "ALL TRAINED" );


        resps.clear();
        for (int i = 0; i < N; i++)
            resps.add(Patterns.ask(slaves.get(i), new Slave.WekaEvaluate(eval), timeout));
        for (Future<Object> resp : resps)
        {
            EvalFinished res = ((EvalFinished) Await.result(resp, timeout.duration()));
            double acc = DataSplitter.calculateAccuracy( res.data );
            weight.put( res.who, acc );
        }
        System.out.println("EVAL FINISHED");
        for (int i = 0; i < slaves.size(); i++) {
            System.out.println( weight.get( slaves.get(i) ) );
        }
        cc.setWeights(weight);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES

    static public class Init
    {
        public final int numberOfAgents;
        public final int algName;
        public final int splitMethod;
        public final double OL;


        public Init(int numberOfAgents, int algName, int splitMethod, double OL) {
            this.numberOfAgents = numberOfAgents;
            this.algName = algName;
            this.splitMethod = splitMethod;
            this.OL = OL;
        }
    }

    static public class GetList {}

    static public class EvaluateTest {}

    static public class Kill
    {
        public final int num;

        public Kill(int num)
        {
            this.num = num;
        }
    }

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
    public Receive createReceive() {
        return receiveBuilder()

                .match(GetList.class, x -> {

                    slaves.forEach( slave -> {
                        slave.tell( new M.Classify("abc"), self() );
                    } );

                    System.out.println( "childs" );
                    int ctr = 0;
                    for (int i = 0; i <1000; i++)
                    {
                        ctr += 2;
                    }

                    getContext().getChildren().forEach( child -> {
                        child.tell( new M.Classify("abc"), self() );
                    });
                })
//                .match(      Init.class, this::onCreateAgents)
                .match(  Kill.class, this::onKill)
                .match(  EvaluateTest.class, this::onTest)
                .match(  WekaEvalFinished.class, this::onEvalFinished)
                .match(  M.Classify.class, x -> {
                    System.out.println( "my parent "  + getContext().getParent() );
                    System.out.println( "message received from" + x.agentId );
                })
                .match(  RunConf.class, this::onConfig)
                .matchAny(o -> { System.out.println("Master received unknown message: " + o); })
                .build();
    }

    public static void main(String[] args)
    {
        ActorSystem system = ActorSystem.create("testSystem");

//kagle
//        word2vec -
//        lstm
        ConverterUtils.DataSource source = null;
        try {
            // DATA loading to master
//            source = new ConverterUtils.DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\spambase.arff");
            source = new ConverterUtils.DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
            Instances train = source.getDataSet();
            source = new ConverterUtils.DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_test.arff");
            Instances test = source.getDataSet();
            test.setClassIndex( test.numAttributes() - 1 );
            ActorRef m = system.actorOf( Master.props() ,"master" );


            RunConf RC = new RunConf.Builder()
                    .agents(new S_Type[]{SMO, SMO, SMO, SMO, J48, J48, PART, PART, PART, PART})
                    .split_meth(SIMPLE)
                    .class_method(WEIGHTED)
                    .ol_ratio(0.5)
                    .split_ratio(0.9)
                    .train(train)
                    .test(test)
                    .build();

            // SETUP MASTER
            m.tell( RC, ActorRef.noSender() );
            m.tell( new EvaluateTest(), ActorRef.noSender() );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/**

 2.152s - eval with simple divide , 0.7 - 0.9355519751097283 %

 2.175 - eval overlap divide 0.5, and split train 0.7

 */