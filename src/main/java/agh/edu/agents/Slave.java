package agh.edu.agents;

import agh.edu.learning.MLA;
import agh.edu.messages.M;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Slave extends AbstractActor
{

    MLA alg;

    static public Props props(String message, ActorRef printerActor) {
        return Props.create(Master.class, () -> new Master(message, printerActor));
    }


    @Override
    public AbstractActor.Receive createReceive()
    {
        return receiveBuilder()
                .match(M.Classify.class, x ->
                {
                    System.out.println( "Classify received" );
                })

                .match(M.Learn.class, x ->
                {
                    System.out.println( "LEarn received" );
                })

                .match(M.SetAlgorithm.class, x ->
                {
                    System.out.println( "Set received" );
                })

                .build();
    }
}
