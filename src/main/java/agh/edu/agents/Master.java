package agh.edu.agents;

import agh.edu.messages.M;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import weka.classifiers.evaluation.Prediction;

import java.util.ArrayList;
import java.util.List;

public class Master extends AbstractActor
{

    private List<ActorRef> slaves;
    private final String message;
    private final ActorRef printerActor;
    private String greeting = "";

    static public Props props() {
        return Props.create(Master.class);
    }

    static public Props props(String message, ActorRef printerActor) {
        return Props.create(Master.class, () -> new Master(message, printerActor));
    }

    //#greeter-messages
    static public class WhoToGreet {
        public final String who;

        public WhoToGreet(String who) {
            this.who = who;
        }
    }

    static public class Greet {
        public Greet() {
        }
    }
    //#greeter-messages


    public Master() {
        message = "ASED";
        printerActor = null;
        System.out.println("default constructor");
    }


    static public class Init
    {
        public final int numberOfAgents;
        public final int algName;

        public Init(int numberOfAgents, int algName)
        {
            this.numberOfAgents = numberOfAgents;
            this.algName = algName;
        }
    }

    static public class GetList
    {

    }

    static public class Kill
    {
        public final int num;

        public Kill(int num)
        {
            this.num = num;
        }
    }

    public class WekaEvalResp
    {
        final Prediction results;
        public WekaEvalResp(Prediction results) {
            this.results = results;
        }
    }

    public Master(String message, ActorRef printerActor) {
        this.message = message;
        this.printerActor = printerActor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()

                .match(WhoToGreet.class, wtg -> {
                    this.greeting = message + ", " + wtg.who;
                })
                .match(GetList.class, x -> {

                    slaves.forEach( slave -> {
                         slave.tell( new M.Classify("abc"), self() );
                    } );

                    System.out.println( "childs" );
                    int ctr = 0;
                    for (int i = 0; i <1000; i++) {
                        ctr += 2;
                    }

                    getContext().getChildren().forEach( child -> {
                        child.tell( new M.Classify("abc"), self() );
                    });
                })
                .match(Init.class, x -> {

                    slaves = new ArrayList<>();
                    for (int i = 0; i < x.numberOfAgents; i++)
                        slaves.add( getContext().actorOf( Slave.props( x.algName ) )  );
                })


                .match(Kill.class, x -> {

                    for (int i = 0; i < x.num; i++)
                    {
                        getContext().stop( slaves.get( i ) );
                    }
                    slaves.removeIf(ActorRef::isTerminated);
                })
                .match(WekaEvalResp.class, x -> {

                })

                .match(M.Classify.class,x -> {

                    //#greeter-send-message
                    System.out.println( "my parent "  + getContext().getParent() );
                    System.out.println( "message received from" + x.agentId );
//                    printerActor.tell(new Greeting(greeting), getSelf());
                    //#greeter-send-message
                })
                .build();



    }

}
