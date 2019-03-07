package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.learning.ClassRes;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import weka.classifiers.Classifier;
import weka.core.Instances;


// TODO after some number of classified instances send eval grade to
// TODO aggregator
public class ClassSlave extends AbstractActor
{
    private ActorRef learner;
    private ActorRef aggr;

    private S_Type type;

    private Classifier model;
    private String conf;
    private ClassRes cr;

    private Instances data;

    // TODO it has to know aggregator


    public static Props props( ClassSetup sp )
    {
        return Props.create(ClassSlave.class, ()-> new ClassSlave(sp) );
    }

    public ClassSlave(ClassSetup sp)
    {
        learner = sp.learner;
        aggr = sp.aggr;
        type = sp.type;
        data = sp.data;
    }


    private void handleNewBest(BestClass bc)
    {
        model = bc.model;
        conf = bc.conf;
        cr = bc.results;
    }

    public final class Model
    {
        Classifier classifier;

        public Model(Classifier classifier) {
            this.classifier = classifier;
        }
    }

    public final class ClassSetup
    {
        ActorRef aggr;
        ActorRef learner;
        Instances data;
        S_Type type;

        public ClassSetup(ActorRef aggr, ActorRef learner, Instances data, S_Type type)
        {
            this.aggr = aggr;
            this.learner = learner;
            this.data = data;
            this.type = type;
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
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BestClass.class,this::handleNewBest)
                .match(
                        Integer.class,
                        i -> {
                            getSender().tell(i + System.currentTimeMillis(), getSelf());
                        })
                .build();
    }

}
