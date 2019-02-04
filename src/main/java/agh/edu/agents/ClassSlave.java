package agh.edu.agents;

import akka.actor.ActorPath;
import akka.actor.ActorRef;

public class ClassSlave extends ActorRef
{
    private ActorRef learner;

//    static public Props props()
//    {
//        return Props.create(ClassSlave.class, ClassSlave::new);
//    }

    @Override
    public ActorPath path() {
        return null;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    public ClassSlave() {}

    public static void main(String[] args)
    {

    }
}
