package agh.edu.agents;

import agh.edu.learning.ClassRes;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import weka.classifiers.evaluation.Prediction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO BETTER Way to identify the actor in refference
public class Aggregator extends AbstractActorWithStash {

    // TODO some kind of classification strategy
    Map<ActorRef,ClassGrade> perf;
    Map<Integer,ResultsHolder> results;



    private void handleClassUpdate(ClassGrade cg) { perf.put( getSender(), cg ); }

    private void handleClassResult(ClassRes cr)
    {

    }

    private void handlePartialRes(PartialRes pr)
    {
        int id = pr.ID;
        ActorRef slave = sender();
        if( results.containsKey( id ) )
        {
            results.get( id ).appendResults( pr, slave );
        } else {
            ResultsHolder rh = new ResultsHolder( id );
            rh.appendResults( pr, slave );
            results.put( id, rh );
        }
        // TODO classify each row of data and send the results
    }

    private void handleClassification(QueryResults qr)
    {
        // TODO
    }


    public static class ResultsHolder
    {
        private final Integer ID;
        private final StringBuilder class_order;
        private final List<StringBuilder> preds;
        private final List<StringBuilder> probs;

        public ResultsHolder(Integer ID)
        {
            this.ID = ID;
            class_order = new StringBuilder();
            preds = new ArrayList<>();
            probs = new ArrayList<>();
        }

        public void appendResults(PartialRes pr, ActorRef ref)
        {
            class_order.append(ref).append(",");
            List<Prediction> preds_to_add = pr.cr.getPreds();
            List<double[]> probs_to_add = pr.cr.getProbs();

            if( preds.isEmpty() )
            {
                for (int i = 0; i < preds_to_add.size(); i++)
                {
                    preds.add( new StringBuilder( preds_to_add.get(i).toString() ) );
                    probs.add( new StringBuilder( arrToStr( probs_to_add.get(i) ) ) );
                }
            } else {
                for (int i = 0; i < preds_to_add.size(); i++)
                {
                    preds.get(i).append( preds_to_add.get(i).toString());
                    probs.get(i).append( arrToStr( probs_to_add.get(i) ) );
                }
            }
        }

        private String arrToStr(double[] arr)
        {
            StringBuilder s = new StringBuilder();
            for (double v : arr) {
                s.append(v).append(",");
            }
            return s.append(";").toString();
        }
    }


    public static final class PartialRes
    {
        private final Integer ID;
        private final ClassRes cr;

        public PartialRes(Integer ID, ClassRes cr) {
            this.ID = ID;
            this.cr = cr;
        }
    }

    public static final class ClassGrade
    {
        private final double[] fscore;
        private final double[] AUROC;
        private final double acc;

        public ClassGrade(double[] fscore, double[] AUROC, double acc) {
            int N = fscore.length;
            this.fscore = new double[ N ];
            this.AUROC = new double[ N ];

            this.acc = acc;
            for (int i = 0; i < N; i++) {
                this.fscore[ i ] = fscore[ i ];
                this.AUROC[ i ] = AUROC[ i ];
            }
        }

        public ClassGrade(ClassRes cr)
        {
            this( cr.getFscore(), cr.getAUROC(), cr.getAcc() );
        }
    }

    public static final class QueryResults
    {
        private final Integer ID;
        private final List<ActorRef> class_order;
        private final List<Short> results;


        public QueryResults(Integer ID, List<ActorRef> class_order, List<Short> results)
        {
            this.ID = ID;
            this.class_order = new ArrayList<>(class_order);
            this.results = new ArrayList<>(results);
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()


                .matchAny( x -> System.out.println("?? " + x ) )
                .build();
    }
}
