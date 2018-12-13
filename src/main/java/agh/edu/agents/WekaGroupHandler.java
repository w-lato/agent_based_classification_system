package agh.edu.agents;

import akka.actor.*;
import scala.Array;
import scala.concurrent.duration.FiniteDuration;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WekaGroupHandler extends AbstractActor
{
    ActorRef master;
    List<ActorRef> slaves;
    Map<ActorRef, List<Prediction>> results;
    FiniteDuration timeout;
    Cancellable queryTimer;

    static public Props props(List<ActorRef> slaves, ActorRef master, FiniteDuration timeout)
    {
        return Props.create(WekaGroupHandler.class, () -> new WekaGroupHandler(master, slaves, timeout));
    }

    public WekaGroupHandler(ActorRef master, List<ActorRef> slaves, FiniteDuration timeout)
    {
        this.master = master;
        this.slaves = slaves;
        this.timeout = timeout;

        // moved to message receiver;
        queryTimer = getContext().getSystem().scheduler().scheduleOnce(
                timeout, getSelf(), new EndOfTime(), getContext().dispatcher(), getSelf()
        );
    }

    @Override
    public void preStart()
    {
        System.out.println("GROUP HANDLER STARTED");
        for (ActorRef slave : slaves)
        {
            getContext().watch( slave );
        }
    }

    @Override
    public void postStop()
    {
        queryTimer.cancel();
    }

    private void setTimeout(FiniteDuration duration)
    {
        queryTimer = getContext().getSystem().scheduler().scheduleOnce(
                timeout, getSelf(), new EndOfTime(), getContext().dispatcher(), getSelf()
        );
    }

    private void onEval( EvalData m )
    {
        for (int i = 0; i < slaves.size(); i++)
        {
            slaves.get(i).tell( new Slave.WekaEvaluate(m.data), self());
        }
        setTimeout( timeout );
    }

    private void onEvalResp( EvalResp m )
    {
        System.out.println(" SLAVE RESPONDED EVAL ");
        if( results == null ) results = new HashMap<>();
        results.put( getSender(), m.data );

        if( results.size() == slaves.size() )
        {
            System.out.println("  ------- ALL RESPONDED BEFORE END");
            queryTimer.cancel();
            master.tell( new Master.WekaEvalFinished( results ), self());
            getContext().stop( self() );
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES


    public static final class EndOfTime {}
    public static final class EvalData
    {
        final Instances data;
        public EvalData(Instances data)
        {
            this.data = data;
        }
    }

    public static final class EvalResp
    {
        final List<Prediction> data;
        public EvalResp(List<Prediction> data)
        {
            this.data = data;
        }
    }

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                .match(EvalData.class, this::onEval)
                .match(EvalResp.class, this::onEvalResp)
                .match(Terminated.class, t -> {
                    slaves.remove( getSender() );
                })

                .match(EndOfTime.class, t -> {
                    System.out.println("  END ----------------- OF TIME + " + results.size());
                    getContext().stop(getSelf());
                })
                .build();
    }
}
