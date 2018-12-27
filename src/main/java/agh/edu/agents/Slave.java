package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.learning.WekaEval;
import agh.edu.messages.M;
import akka.actor.AbstractActor;
import akka.actor.PoisonPill;
import akka.actor.Props;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;

import java.util.List;

public class Slave extends AbstractActor
{
    S_Type alg;
    WekaEval we;


    static public Props props(S_Type machine_algorithm)
    {
        return Props.create(Slave.class, () -> new Slave(machine_algorithm));
    }


    public Slave(S_Type ML_alg)
    {
        this.alg = ML_alg;
        we = new WekaEval(ML_alg);
    }


    @Override
    public void postStop() {
        System.out.println("KIA " + getSelf());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES


    public static class WekaTrain
    {
        final Instances trainData;
        public WekaTrain(Instances trainData) {
            this.trainData = trainData;
        }
    }

    public static class WekaEvaluate
    {
        final Instances testData;
        public WekaEvaluate(Instances testData) {
            this.testData = testData;
        }
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

                // TRAIN
                .match(WekaTrain.class, M ->
                {
                    we = new WekaEval( alg );
                    we.train( M.trainData );
                    System.out.println( "Training ended, size =  " + M.trainData.size() + ", " + self() );
                    getSender().tell( "SLAVE TRAINED" , self());
                })

                .match(WekaEvaluate.class, M ->
                {
                    System.out.println( "EVAL stopped" );
                    List<Prediction> data = we.eval( M.testData );
                    getSender().tell( new Master.EvalFinished(
                            data,
                            self()
                    ), self());
                })

                .match(PoisonPill.class, M ->
                {
                    getContext().stop( self() );
                })

                .matchAny(o -> System.out.println("Slave received unknown message: " + o))
                .build();
    }
}
