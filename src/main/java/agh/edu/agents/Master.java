package agh.edu.agents;

import agh.edu.GUI.AppUI;
import agh.edu.GUI.SlaveRow;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.enums.Vote;
import agh.edu.learning.DataSplitter;
import agh.edu.messages.M;
import akka.actor.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import javafx.application.Platform;
import scala.concurrent.Await;
import scala.concurrent.Future;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static agh.edu.agents.enums.S_Type.*;
import static agh.edu.agents.enums.Split.SIMPLE;
import static agh.edu.agents.enums.Vote.*;


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

 90	2500
 90.06	90.33	72.51
 91.25	5500
 91.51	91.69	79.32
 91.66	11000
 91.97	92.15	81.46
 92.65	16500
 92.95	93.02	 83.14
 92.66	22000
 93.15	93.37	83.9
 92.75	27500
 93.63	93.73	84.08
 93.44	32250
 93.71	93.8	84.38
 93.55	37750
 93.93	93.98	84.56
 93.73	43000
 93.67	93.84	84.73
 94	48000
 94.00	94.08	84.49
 */
public class Master extends AbstractActor
{
    private AppUI GUI;

    private List<ActorRef> slaves;
    private List<Instances> train;
    private Instances eval;
    private Instances test;

    private Map<ActorRef, Boolean> isReady;
    private Map<ActorRef, Double> weight;
    private ClassChooser cc;

    private Vote vote_method;

    static public Props props(Boolean withGUI)
    {
        return Props.create(Master.class, ()-> new Master(withGUI));
    }



    public Master(Boolean withGUI)
    {
        if(withGUI) {
            GUI = AppUI.getInstance();
            GUI.setMaster( self() );
        }
        else GUI = null;
    }


    private void onKill(Kill m)
    {
//        for (int i = 0; i < m.num; i++)
//        {
//            getContext().stop( slaves.get( i ) );
//        }
        slaves.removeIf(ActorRef::isTerminated);
    }

    private void onTest(EvaluateTest m) throws Exception
    {
        System.out.println( "TEST START: " + vote_method);

        Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
        ArrayList<Future<Object>> resps = new ArrayList<>();
        long s = System.currentTimeMillis();
        for (int i = 0; i < slaves.size(); i++)
            resps.add(Patterns.ask(slaves.get(i), new Slave.WekaEvaluate( test ), timeout));

        Map<ActorRef, List<Prediction>> test_res = new HashMap<>();
        for (int i = 0; i < resps.size(); i++)
        {
            Future<Object> resp = resps.get(i);
            EvalFinished res = ((EvalFinished) Await.result(resp, timeout.duration()));
            test_res.put( res.who, res.data );
            if( GUI != null ) GUI.updateProgressOnTest(((double) i) / resps.size() );
        }

        System.out.println("------ " +  (System.currentTimeMillis() - s) );
        List<Integer> preds = cc.chooseClasses( test_res, vote_method );
        System.out.println( "ALL TESTED" );
        int ctr = 0;
        for (int i = 0; i < preds.size(); i++)
        {
            if( test_res.get( slaves.get(0) ).get(i).actual() == preds.get(i) ) ctr++;
        }
        double acc = (double)(100.0 * (double)ctr / preds.size());
        System.out.println( "TEST: " + acc + "%");
        if( GUI != null )
        {
            GUI.updateProgressOnTest(1);
            Platform.runLater(new Runnable(){
                @Override
                public void run() {
                    GUI.setAccText(acc);
                }
            });
        }
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
        if( slaves != null && slaves.size() != 0 )
        {
            for (ActorRef x : slaves) { x.tell(new Kill(), ActorRef.noSender()); }
            slaves.clear();
        }
        vote_method = c.class_method;
        cc = new ClassChooser();
        int N = c.agents.length;
        slaves = new ArrayList<>();
        isReady = new HashMap<>();
        weight = new HashMap<>();
        Map<ActorRef,S_Type> types = new HashMap<>();

        for (int i = 0; i < c.agents.length; i++) {
            System.out.println( "# " + c.agents[i] );
        }
        for (int i = 0; i < N; i++)
        {
            ActorRef it = getContext().actorOf( Slave.props( c.agents[i] ));
            types.put( it, c.agents[i] );
            slaves.add( it );
            isReady.put(it, false);
            weight.put(it, 0.0);
        }

        // data setup
        List<Instances> x = DataSplitter.splitIntoTrainAndTest( c.train, c.split_ratio );
        train = DataSplitter.split( x.get(0), N, c.split_meth, c.ol_ratio );
        this.eval = x.get(1);
        test = c.test;
        test.setClassIndex(test.numAttributes() - 1 );

        Timeout timeout = new Timeout(10, TimeUnit.MINUTES);
        ArrayList<Future<Object>> resps = new ArrayList<>();
        long s = System.currentTimeMillis();
        for (int i = 0; i < N; i++)
            resps.add(Patterns.ask(slaves.get(i), new Slave.WekaTrain(train.get(i)), timeout));

        for (int i = 0; i < resps.size(); i++)
        {
            Future<Object> resp = resps.get(i);
            Await.result(resp, timeout.duration() );
            if( GUI != null ) GUI.updateProgressOnTrain( ((i) / ((double) resps.size() * 2)) );
        }
        System.out.println( "ALL TRAINED $$ " +  (System.currentTimeMillis() - s));
        resps.clear();




        for (int i = 0; i < N; i++)
            resps.add(Patterns.ask(slaves.get(i), new Slave.WekaEvaluate( eval ), timeout));
        System.out.println(" START ");
        long st = System.currentTimeMillis();
        for (int i = 0; i < resps.size(); i++)
        {
            Future<Object> resp = resps.get(i);
            EvalFinished res = ((EvalFinished) Await.result(resp, timeout.duration()));
            double acc = DataSplitter.calculateAccuracy( res.data );
            weight.put( res.who, acc );
            if( GUI != null ) GUI.updateProgressOnTrain( (((i +  resps.size()) / ((double) resps.size())) * 2) );
        }
        System.out.println( System.currentTimeMillis() - st );
        System.out.println("EVAL FINISHED");
        for (int i = 0; i < slaves.size(); i++) {
            System.out.println( weight.get( slaves.get(i) ) );
        }
        cc.setWeights(weight);
        if( GUI != null ) {
            GUI.updateProgressOnTrain(1.0);
            List<SlaveRow> l = new ArrayList<>();

            types.forEach( (k,v) -> {
                l.add( new SlaveRow( k.toString() , v.name(), weight.get( k )  ) );
            });
            GUI.addAgentsToTable( l );
        }
    }

    private void onVotingChange( SetVoting sv )
    {
        System.out.println( "VOTING SET TO: "+ sv.method);
        this.vote_method = sv.method;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES



    static public class SetVoting {
        public final Vote method;
        public SetVoting(Vote method) {
            this.method = method;
        }
    }

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
                .match(  SetVoting.class, this::onVotingChange)
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
            ActorRef m = system.actorOf( Master.props(true) ,"master" );


            RunConf RC = new RunConf.Builder()
                    .agents(new S_Type[]{
//                            SMO, SMO, SMO, SMO,
//                            SMO, SMO, SMO, SMO,
//                            SMO, SMO, SMO, SMO
//                            J48, J48,
                            PART, PART, PART, PART,
                            PART, PART, PART, PART
                    })
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


 8 PARTS, WITH WIGHTED VOTE SIMPLE CLIT
 92.83% - -10% IMPROVEMENTS
 */