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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO method which sets class strat & a method which will use all possible strategies
// TODO load agent's grades - now we have to eval model and then send it to Learner and to aggregator...
// TODO what we can do with STRAT - we should test every strategy to see which is the best one
// TODO save query results in each file - besides the
public class Aggregator extends AbstractActorWithStash
{
    String exp_id; // EXP/exp_id
    ActorRef master;


    ClassStrat strat;
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

    public Aggregator(ActorRef coordinator,String exp_id, Map<String,ClassGrade> m)
    {
        this();
        this.master = coordinator;
        this.exp_id = exp_id;
        this.perf = m;
        System.out.println( "AGG ## READY" );
    }


    static public Props props(ActorRef master, String exp_id)
    {
        return Props.create(Aggregator.class, () -> new Aggregator(master,exp_id));
    }

    static public Props props(AggSetup setup)
    {
        return Props.create(Aggregator.class, () -> new Aggregator(setup.master, setup.exp_id, setup.m));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    HANDLERS


    private void handleClassUpdate(ClassGrade cg) { perf.put( cg.getModel_id(), cg ); }

    private void handleClassResult(ClassRes cr)
    {

    }

    private void handlePartialRes(PartialRes pr) throws IOException {
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
        Saver.saveAggResults( exp_id, results );
        System.out.println( "  :$: QUERY  " + id + "  RES. APPENDED from:  " + model_id  );
//        List<Integer> l = ClassPred.getPreds ( strat, perf, results.get(id).getProbs() );
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
                .match(    PoisonPill.class,    x -> getContext().stop(self()))
                .matchAny(                      x -> System.out.println("?? " + x ) )
                .build();
    }
}
