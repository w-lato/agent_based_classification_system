package agh.edu.agents;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.aggregation.ClassPred;
import agh.edu.aggregation.ResultsHolder;
import agh.edu.learning.ClassRes;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;

import java.util.List;
import java.util.Map;

// TODO BETTER Way to identify the actor in reference
public class Aggregator extends AbstractActorWithStash {

    ActorRef coordinator;

    // TODO some kind of classification strategy
    ClassStrat strat;
    Map<ActorRef,ClassGrade> perf;
    Map<Integer, ResultsHolder> results;



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
            results.get( id ).appendPredsAndProbs( pr, slave, perf );
        } else {
            ResultsHolder rh = new ResultsHolder( id, strat );
            rh.appendPredsAndProbs( pr, slave, perf );
            results.put( id, rh );
        }
        List<Integer> l = ClassPred.getPreds ( strat, perf, results.get(id).getProbs() );
        // TODO classify each row of data and send the results

    }


    public static final class PartialRes
    {
        private final Integer ID;
        private final ClassRes cr;

        public PartialRes(Integer ID, ClassRes cr) {
            this.ID = ID;
            this.cr = cr;
        }

        public ClassRes getCr() {
            return cr;
        }
    }

    public static final class ClassGrade
    {
        private final double[] fscore;
        private final double[] AUROC;
        private final double acc;
        private final double acc_wgt;
        private final double fmeas_wgt;

        public double[] getFscore() { return fscore; }
        public double[] getAUROC() { return AUROC; }
        public double getAcc() { return acc; }
        public double getAcc_wgt() { return acc_wgt; }
        public double getFmeas_wgt() { return fmeas_wgt; }

        public ClassGrade(double[] fscore, double[] AUROC, double acc, double acc_wgt, double fmeas_wgt)
        {
            int N = fscore.length;
            this.fscore = new double[ N ];
            this.AUROC = new double[ N ];
            this.acc_wgt = acc_wgt;
            this.fmeas_wgt = fmeas_wgt;

            this.acc = acc;
            for (int i = 0; i < N; i++) {
                this.fscore[ i ] = fscore[ i ];
                this.AUROC[ i ] = AUROC[ i ];
            }
        }

        public ClassGrade(ClassRes cr)
        {
            this( cr.getFscore(), cr.getAUROC(), cr.getAcc(), cr.getAcc_wgt(),cr.getFmeas_wgt() );
        }
    }
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()


                .matchAny( x -> System.out.println("?? " + x ) )
                .build();
    }
}
