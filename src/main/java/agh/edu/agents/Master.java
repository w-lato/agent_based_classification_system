package agh.edu.agents;

import agh.edu.messages.M;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Master extends AbstractActor
{
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



    public Master(String message, ActorRef printerActor) {
        this.message = message;
        this.printerActor = printerActor;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(WhoToGreet.class, wtg -> {
                    this.greeting = message + ", " + wtg.who;
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

    public static void main(String[] args)
    {
        ActorSystem system = ActorSystem.create("testSystem");
        ActorRef m = system.actorOf( Master.props() ,"master" );


        ActorRef m2 = system.actorOf( Master.props() ,"master1" );
        m2.tell( new M.Classify( "007" ), m );
        System.out.println( system.child("master") );
        System.out.println( system.child("master") );
        System.out.println(m.path() );


    }
}
