package agh.edu.agents;

import agh.edu.learning.DataSplitter;
import akka.actor.AbstractActor;
import weka.core.Instances;
import java.util.List;

public class Splitter extends AbstractActor
{
    private List<Instances> train;
    private Instances eval;

    private void onSplit( RunConf c )
    {
        // split data into N parts
        int N = c.agents.length;
        List<Instances> x = DataSplitter.splitIntoTrainAndTest( c.train, c.split_ratio );
        train = DataSplitter.split( x.get(0), N, c.split_meth, c.ol_ratio );
        this.eval = x.get(1);

        //
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES




    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                .match(  RunConf.class, this::onSplit)
                .matchAny(o -> { System.out.println("Master received unknown message: " + o); })
                .build();
    }

}
