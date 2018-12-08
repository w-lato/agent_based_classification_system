package agh.edu.agents;

import agh.edu.learning.WekaEval;
import agh.edu.messages.M;
import akka.actor.AbstractActor;
import akka.actor.Props;
import weka.core.Instances;

public class Slave extends AbstractActor
{

    int alg;
    WekaEval we;

    static public Props props(int machine_algorithm)
    {
        return Props.create(Slave.class, () -> new Slave(machine_algorithm));
    }


    public Slave(int ML_alg)
    {
        this.alg = ML_alg;
        we = new WekaEval(ML_alg);
    }

    public class WekaTrain
    {
        final Instances trainData;
        public WekaTrain(Instances trainData) {
            this.trainData = trainData;
        }
    }

    public class WekaEvaluate
    {
        final Instances testData;
        public WekaEvaluate(Instances testData) {
            this.testData = testData;
        }
    }

    // good practice to stop agent
    public class PoisonPill {}

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

                .match(WekaTrain.class, M ->
                {
                    this.we.train( M.trainData );
                    System.out.println( "Training stopped" );
                })

                .match(WekaEvaluate.class, M ->
                {
                    this.we.eval( M.testData );
                    System.out.println( "Training stopped" );
                })

                .match(PoisonPill.class, M ->
                {
                    getContext().stop( self() );
                })

                .build();
    }



    @Override
    public void postStop() {
        System.out.println("KIA " + getSelf());
    }

}
