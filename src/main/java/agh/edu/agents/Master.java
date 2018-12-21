package agh.edu.agents;

import agh.edu.learning.DataSplitter;
import agh.edu.messages.M;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.FiniteDuration;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Master extends AbstractActor
{
    private List<ActorRef> slaves;
    private Instances train;
    private Instances test;

    static public Props props() {
        return Props.create(Master.class);
    }
    static public Props props(Instances data, double ratio)
    {
        return Props.create(Master.class, () -> new Master(data, ratio));
    }

    public Master()
    {
        System.out.println("default constructor");
    }

    public Master(Instances data, double ratio)
    {
        List<Instances> x = DataSplitter.splitIntoTrainAndTest( data, ratio );
        this.train = x.get(0);
        this.test = x.get(1);
        System.out.println("Master constr:  train = " + train.size() +  "  test: " + test.size());
    }


    private void onCreateAgents( Init m )
    {
        // init
        slaves = new ArrayList<>();
        for (int i = 0; i < m.numberOfAgents; i++)
            slaves.add( getContext().actorOf( Slave.props( m.algName ) )  );

        // divide and train
        List<Instances> parts = DataSplitter.divideEqual( train, slaves.size() );
        for (int i = 0; i < slaves.size(); i++)
            slaves.get(i).tell(new Slave.WekaTrain( parts.get(i) ), self());

        for (int i = 0; i < parts.size(); i++) {

        }
    }

    private void onKill(Kill m)
    {
        for (int i = 0; i < m.num; i++)
        {
            getContext().stop( slaves.get( i ) );
        }
        slaves.removeIf(ActorRef::isTerminated);
    }

    private void onEval(EvaluateTest m)
    {
        ActorRef gh = getContext().actorOf(
                WekaGroupHandler.props(slaves, self(), new FiniteDuration(5, TimeUnit.SECONDS)));
        gh.tell( new WekaGroupHandler.EvalData(test), self() );
    }

    private void onEvalFinished(WekaEvalFinished m)
    {
        System.out.println(" EVAL FISNIHED ");
        ThresholdCurve ROC = new ThresholdCurve();
        m.results.forEach( (k,v) -> {
            System.out.println( k );
            int ctr = 0;
            for (Prediction pred : v)
            {
                if (Double.compare( pred.predicted(), pred.actual()) == 0) ctr ++;
            }
//            m.results.get(k).forEach( xd ->{
//                System.out.println( xd.actual() + " : " +  xd.predicted() + " " + xd.weight() );
//            } );
            Instances curve = ROC.getCurve(new ArrayList<>(v));
            curve.forEach(  xd -> {
                System.out.println( xd );
            });
            System.out.println( k + " :" + "  " + ctr + "  /" + v.size() +" : " + curve.size() + " " +
                    ThresholdCurve.getROCArea( curve ) );
        });
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES

    static public class Init
    {
        public final int numberOfAgents;
        public final int algName;

        public Init(int numberOfAgents, int algName)
        {
            this.numberOfAgents = numberOfAgents;
            this.algName = algName;
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
                .match(      Init.class, this::onCreateAgents)
                .match(      Kill.class, this::onKill)
                .match(  EvaluateTest.class, this::onEval)
                .match(  WekaEvalFinished.class, this::onEvalFinished)
                .match(M.Classify.class, x -> {
                    System.out.println( "my parent "  + getContext().getParent() );
                    System.out.println( "message received from" + x.agentId );
                })
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
            source = new ConverterUtils.DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\spambase.arff");
            Instances data = source.getDataSet();
            ActorRef m = system.actorOf( Master.props(data, 0.8 ) ,"master" );

            // split data into portions and send them to X agents
            m.tell( new Init(10, 0), ActorRef.noSender() );

            // eval after training
            m.tell( new EvaluateTest(), ActorRef.noSender());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
