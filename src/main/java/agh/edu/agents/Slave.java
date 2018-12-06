package agh.edu.agents;

import agh.edu.messages.M;
import akka.actor.AbstractActor;
import akka.actor.Props;

public class Slave extends AbstractActor
{

    int alg;

    static public Props props(int machine_algorithm)
    {
        return Props.create(Slave.class, () -> new Slave(machine_algorithm));
    }


    public Slave(int ML_alg)
    {
        this.alg = ML_alg;
    }

    @Override
    public AbstractActor.Receive createReceive()
    {
        return receiveBuilder()
                .match(M.Classify.class, x ->
                {
                    System.out.println( "Classify received " + sender() + " " + getSelf() );
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

    @Override
    public void postStop() {
        System.out.println("KIA " + getSelf());
    }

}
