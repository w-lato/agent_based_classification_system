package agh.edu.agents;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.aggregation.ClassGrade;
import agh.edu.aggregation.ClassPred;
import agh.edu.aggregation.ResultsHolder;
import agh.edu.learning.ClassRes;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO method which sets class strat & a method which will use all possible strategies
// TODO load agent's grades - now we have to eval model and then send it to Learner and to aggregator...
public class Aggregator extends AbstractActorWithStash
{
    String exp_id; // EXP/exp_id

    ActorRef master;
    ClassStrat strat;

    Map<String, ClassGrade> perf;
    Map<Integer, ResultsHolder> results;


    public Aggregator(ActorRef coordinator, ClassStrat strat) {
        this.master = coordinator;
        this.strat = strat;
    }

    static public Props props(ActorRef coord, ClassStrat strat) {
        return Props.create(Aggregator.class, () -> new Aggregator(coord,strat));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    HANDLERS


    private void handleClassUpdate(ClassGrade cg) { perf.put( cg.getModel_id(), cg ); }

    private void handleClassResult(ClassRes cr)
    {

    }

    private void handlePartialRes(PartialRes pr)
    {
        int id = pr.ID;
        String model_id = pr.model_id;
        if( results.containsKey( id ) )
        {
            results.get( id ).appendPredsAndProbs( pr, model_id, perf );
        }
        else {
            ResultsHolder rh = new ResultsHolder( id );
            rh.appendPredsAndProbs( pr, model_id, perf );
            results.put( id, rh );
        }
        List<Integer> l = ClassPred.getPreds ( strat, perf, results.get(id).getProbs() );
        // TODO classify each row of data and send the results
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES


    public static final class PartialRes
    {
        private final Integer ID;
        private final String model_id;
        private final ClassRes cr;

        public PartialRes(Integer ID, ClassRes cr, String model_id)
        {
            this.model_id = model_id;
            this.ID = ID;
            this.cr = cr;
        }

        public ClassRes getCr() {
            return cr;
        }
    }
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PoisonPill.class, x -> getContext().stop(self()))
                .matchAny( x -> System.out.println("?? " + x ) )
                .build();
    }
}
