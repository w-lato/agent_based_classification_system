package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.learning.ClassRes;
import akka.actor.*;
import weka.classifiers.Classifier;
import weka.core.Instances;
import agh.edu.agents.Aggregator.PartialRes;
import agh.edu.agents.Aggregator.ClassGrade;


public class ClassSlave  extends AbstractActorWithStash
{
//    private ActorRef learner;
    // TODO setup aggr using constructor
    private ActorRef aggr;

    private S_Type type;

    private Classifier model = null;
    private String conf;
    private String model_id;
    private ClassRes cr;




    public static Props props(ClassSetup sp)
    {
        return Props.create(ClassSlave.class, ()-> new ClassSlave(sp) );
    }

    public ClassSlave(ClassSetup sp)
    {
        model_id = sp.model_id;
        aggr = sp.aggr;
        type = sp.type;
    }


    private void handleNewBest(BestClass bc)
    {
        conf = bc.conf;
        cr = bc.results;
        aggr.tell( new ClassGrade( cr, model_id ), self());
        if( model == null )
        {
            model = bc.model;
            unstashAll();
        } else {
            model = bc.model;
        }
    }

    private void handleQuery(Query q) throws Exception
    {
        if( model == null)
        {
            stash();
        } else {
            ClassRes to_send = new ClassRes( type, model, q.batch );
            aggr.tell( new PartialRes( q.id, to_send, model_id ), self() );
        }
    }

    public static final class ClassSetup
    {
        ActorRef aggr;
        S_Type type;
        String model_id;

        public ClassSetup(ActorRef aggr, S_Type type, String model_id)
        {
            this.aggr = aggr;
            this.type = type;
            this.model_id = model_id;
        }
    }

    public static final class Query
    {
        final Integer id;
        final Instances batch;

        public Query(Integer id, Instances batch) {
            this.id = id;
            this.batch = batch;
        }
    }

    public static final class BestClass
    {
        final Classifier model;
        final String conf;
        final ClassRes results; // ??

        public BestClass(Classifier model, String conf, ClassRes results) {
            this.model = model;
            this.conf = conf;
            this.results = results;
        }

        public String getConf() { return conf; }
        public ClassRes getResults() { return results; }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(  BestClass.class,this::handleNewBest)
                .match(  Query.class,this::handleQuery)
                .match(  PoisonPill.class, x -> getContext().stop(self()))
                .build();
    }

    public static void main(String[] args)
    {
//        ActorSystem system = ActorSystem.create("testSystem");
//        ActorRef m = system.actorOf( ClassSlave.props(new ClassSetup(ActorRef.noSender(), S_Type.SMO)) ,"master" );
//
//        for (int i = 0; i < 10; i++) {
//            m.tell( new Query(i,null),ActorRef.noSender() );
//        }
//        m.tell("UNSTASH", ActorRef.noSender());
//        m.tell( new Query(10,null),ActorRef.noSender() );
    }
}
