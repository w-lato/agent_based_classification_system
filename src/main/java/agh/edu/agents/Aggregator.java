package agh.edu.agents;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.experiment.Saver;
import agh.edu.aggregation.ClassGrade;
import agh.edu.aggregation.ClassPred;
import agh.edu.aggregation.ResultsHolder;
import agh.edu.learning.ClassRes;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;

import java.io.IOException;
import java.util.*;

// TODO method which sets class strat & a method which will use all possible strategies
// TODO load agent's grades - now we have to eval model and then send it to Learner and to aggregator...
// TODO what we can do with STRAT - we should test every strategy to see which is the best one
// TODO save query results in each file - besides the
public class Aggregator extends AbstractActorWithStash
{
    String exp_id; // EXP/exp_id
    ActorRef master;
    Classifier stacking_model = null;
    String[] order = null;


//    ClassStrat strat;
    Map<String, ClassGrade> perf;
    Map<Integer, ResultsHolder> results;

    private Aggregator()
    {
        perf = new HashMap<>();
        results = new HashMap<>();
    }

    public Aggregator(ActorRef coordinator,String exp_id)
    {
        this();
        this.master = coordinator;
        this.exp_id = exp_id;
    }

    public Aggregator(ActorRef coordinator,String exp_id, Map<String,ClassGrade> m, Classifier model, String[] order)
    {
        this();
        this.master = coordinator;
        this.exp_id = exp_id;
        this.perf = m;
        this.stacking_model = model;
        this.order = order;
        System.out.println( "AGG ## READY" );
    }


    static public Props props(ActorRef master, String exp_id)
    {
        return Props.create(Aggregator.class, () -> new Aggregator(master,exp_id));
    }

    static public Props props(AggSetup setup)
    {
        return Props.create(Aggregator.class, () -> new Aggregator(setup.master, setup.exp_id, setup.m, setup.model, setup.order));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    HANDLERS


    private void handleGradeUpdate(ClassGrade cg) throws IOException {
        perf.put( cg.getModel_id(), cg );
        Saver.saveAgg( exp_id, perf );
        System.out.println(cg.getModel_id() +  " :: " + System.currentTimeMillis() +" :: " + cg.toString() );
    }


    private void handlePartialRes(PartialRes pr) throws Exception
    {
        int id = pr.ID;
        String model_id = pr.model_id;

        if( results.containsKey( id ) )
        {
            results.get( id ).appendProbs( model_id, pr.preds, perf );
        }
        else {
            ResultsHolder rh = new ResultsHolder( id );
            rh.appendProbs( model_id,pr.preds,perf );
            results.put( id, rh );
        }
        Saver.saveAgg( exp_id, perf );
        Saver.saveAggResults( exp_id, perf, results );
        System.out.println( "  :$: QUERY  " + id + "  RES. APPENDED from:  " + model_id + " : " +System.currentTimeMillis()  );
//        List<Integer> l = ClassPred.getPreds ( strat, perf, results.get(id).getProbs() );
        // TODO classify each row of data and send the results
        ClassPred.saveClassChangesOverTime( exp_id, id, perf, results.get(id).getProbs());

        // classify using the stacking model
        if( stacking_model != null &&
                order.length == results.get(id).getProbs().keySet().size() )
        {
            // create .arff
            Instances to_classify = results.get(id).toStackTrainSet( order );

            // save this arff
            Saver.saveStackData(exp_id,id,Arrays.toString(order),to_classify);

            // classify and save results
            Evaluation eval = new Evaluation( to_classify );
            eval.evaluateModel( stacking_model, to_classify );
            Saver.saveStackingPreds( exp_id, id, "", eval.predictions() );
        }
        // if all agents responed then clear query
        if( perf.keySet().size() == results.get(id).getProbs().keySet().size()  )
        {
            System.out.println( "QUERY " + id + " REMOVED AT: " + System.currentTimeMillis());
            results.remove( id );
        }
    }

//    private Instances createDataSetFromProbs(int query_id)
//    {
//        Map<String, List<double[]>> res = results.get(query_id).getProbs();
//        String first_key = res.keySet().iterator().next();
//        int N = res.get( first_key ).size();
//        int num_classes = res.get( first_key ).get(0).length;
//
//        // create attributes
//        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
//        for (int i = 0; i < order.length; i++)
//        {
//            for (int j = 0; j < num_classes; j++)
//            {
//                attributes.add( new Attribute(order[i]+"_"+j) );
//            }
//        }
//        attributes.add( new Attribute("class"));
//
//        // create instances
//        Instances to_ret = new Instances("QUERY_"+query_id+"_STACKING_SET", attributes, 0);
//        to_ret.setClassIndex( to_ret.numAttributes() - 1 );
//
//        // over all instances
//        for (int i = 0; i < N; i++)
//        {
//            double[] aux = new double[ order.length * num_classes + 1 ];
//            // over the order of models
//            for (int j = 0; j < order.length; j++)
//            {
//                double[] class_prob = res.get( order[j] ).get(i);
//                for (int k = 0; k < num_classes; k++)
//                {
//                    aux[ j*k + k ] = class_prob[k];
//                }
//            }
//            aux[ aux.length -1 ] = -1;// class val. is unknown
//            to_ret.add( new DenseInstance(1.0,aux) );
//        }
//        return to_ret;
//    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES


    public static final class PartialRes
    {
        private final Integer ID;
        private final String model_id;
        private List<double[]> preds;

        public PartialRes(Integer ID, String model_id, List<double[]> preds)
        {
            this.model_id = model_id;
            this.ID = ID;
            this.preds = preds;
        }
    }

    public static final class AggSetup
    {
        private ActorRef master;
        private String exp_id;
        private Map<String,ClassGrade> m;
        private Classifier model = null;
        private String[] order = null;

        public AggSetup(ActorRef master, String exp_id, Map<String, ClassGrade> m, Classifier model, String[] order)
        {
            this(master, exp_id, m);
            this.model = model;
            if( order != null )
            {
                this.order = new String[order.length];
                System.arraycopy(order,0,this.order,0,order.length);
            }
        }

        public AggSetup(ActorRef master, String exp_id, Map<String, ClassGrade> m) {
            this.master = master;
            this.exp_id = exp_id;
            this.m = m;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()

                .match(    PartialRes.class,    this::handlePartialRes)
                .match(    ClassGrade.class,    this::handleGradeUpdate)
                .match(    PoisonPill.class,    x -> getContext().stop(self()))
                .matchAny(                      x -> System.out.println(" AGG RECEIVED UNKNOWN MESSAGE?? " + x ) )
                .build();
    }
}
