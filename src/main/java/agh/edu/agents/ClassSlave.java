package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class ClassSlave extends AbstractActor
{
    private ActorRef learner;
    private ActorRef aggr;
    private S_Type type;
    private Classifier model;
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

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        Integer.class,
                        i -> {
                            getSender().tell(i + System.currentTimeMillis(), getSelf());
                        })
                .build();
    }

}
