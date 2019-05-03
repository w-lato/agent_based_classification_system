
package agh.edu.agents;

import agh.edu.agents.Aggregator.PartialRes;
import agh.edu.agents.enums.S_Type;
import agh.edu.aggregation.ClassGrade;
import agh.edu.learning.ClassRes;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ClassSlave  extends AbstractActorWithStash
{
    private ActorRef aggr;

    private S_Type type;

    private Classifier model = null;
    private String conf;
    private String model_id;
    private ClassRes cr;

    public static Props props(ClassSetup sp)
    {
        return Props.create(ClassSlave.class, ()-> new ClassSlave(sp) );
    }
    public static Props props(ClassSetup sp, Classifier model) { return Props.create(ClassSlave.class, ()-> new ClassSlave(sp, model) ); }

    public ClassSlave(ClassSetup sp)
    {
        model_id = sp.model_id;
        aggr = sp.aggr;
        type = sp.type;
    }

    private ClassSlave(ClassSetup sp, Classifier model)
    {
        this.model = model;
        model_id = sp.model_id;
        aggr = sp.aggr;
        type = sp.type;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //

    private void handleNewBest(BestClass bc)
    {
        conf = bc.conf;
        cr = bc.results;
        aggr.tell( new ClassGrade( cr, model_id ), self());
        if( model == null )
        {
            model = bc.model;
            unstashAll();
        } else {
            model = bc.model;
        }
    }

    private void handleQuery(Query q) throws Exception
    {
        if( model == null)
        {
            stash();
        }
        else
        {
            Instances to_process = q.batch;
            Evaluation eval = new Evaluation( to_process );
            eval.evaluateModel( model, to_process );
            List<double[]> probs = new ArrayList<>();
            for (Instance instance : q.batch)
            {
                double[] doubles = model.distributionForInstance(instance);
                probs.add(doubles);
            }
            aggr.tell( new PartialRes( q.id, model_id, probs ), self() );

            unstashAll();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES

    public static final class ClassSetup
    {
        ActorRef aggr;
        S_Type type;
        String model_id;

        public ClassSetup(ActorRef aggr, S_Type type, String model_id)
        {
            this.aggr = aggr;
            this.type = type;
            this.model_id = model_id;
        }
    }

    public static final class Query
    {
        final Integer id;
        final Instances batch;

        public Query(Integer id, Instances batch) {
            this.id = id;
            this.batch = batch;
        }
    }

    public static final class BestClass
    {
        final Classifier model;
        final String conf;
        final ClassRes results; // ??

        public BestClass(Classifier model, String conf, ClassRes results) {
            this.model = model;
            this.conf = conf;
            this.results = results;
        }

        public String getConf() { return conf; }
        public ClassRes getResults() { return results; }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()

                .match(  BestClass.class,    this::handleNewBest)
                .match(  Query.class,        this::handleQuery)
                .match(  PoisonPill.class,   x -> getContext().stop(self()))
                .build();
    }

    @Override
    public void postStop()  {
        super.postStop();
        System.out.println( "LEARNER STOPPED: " + model_id );
    }
}
