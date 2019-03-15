package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.agents.experiment.Saver;
import agh.edu.learning.ClassRes;
import agh.edu.learning.DefaultClassifierFactory;
import agh.edu.learning.params.ParamsFactory;
import agh.edu.learning.params.Params;
import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.*;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static agh.edu.MAIN.crossValidationSplit;

// tODO inform Slave about the number of processed data instances
// TODO remove souts
// TODO does config has to be chosen randomly?
// TODO used instances
public class Learner extends AbstractActorWithTimers {

    private String save_id;
    private ActorRef parent;

    private ClassRes best_cr;
    private String best_conf = "default";
    private Classifier best;

    private Random r;
    private S_Type type;

    private String curr_conf;
    private Params params;

    private List<String> configs;
    private Map<String,String> used_configs;
    private Classifier current;

    private Instances data;

    private static Object OPT_KEY = "OPTIMIZE";

    /**
     * Builds defualt classifier from obtained data and saves it
     * as current and as best classifier.
     *
     */
    public Learner(String save_path, S_Type type, Instances data, ActorRef parent) throws Exception
    {
        used_configs = new HashMap<>();
        this.save_id = save_path + "/" + type + "_" + Saver.getIntID();
        this.parent = parent;
        this.data = data;
        r = new Random(System.currentTimeMillis());
        this.type = type;

        params = ParamsFactory.getParams( type, data );
        configs = params.getParamsCartProd();

        if( type.equals( S_Type.MLP ) )
        {
            params = ParamsFactory.getMLP( data.numClasses(), data.numAttributes() - 1 );
            configs = params.getParamsCartProd();
            current = params.clasFromStr( configs.remove( 0 ) );
        } else {
            params = ParamsFactory.getParams( type );
            configs = params.getParamsCartProd();
            current = DefaultClassifierFactory.getClassifier(type);
        }
        best = current;

        // eval
        System.out.println("  CURRENT : " + current);
        current.buildClassifier( data );
        best_cr = new ClassRes( type,best,data );
        used_configs.put( best_conf,Saver.gradeToString( best_cr ) );
        Saver.saveModel( save_id,current, best_cr,type,data,used_configs);
        parent.tell( new ClassSlave.BestClass( best, best_conf, best_cr),self());

        System.out.println("Learner created");
        getTimers().startSingleTimer(null, "NEW_CONF", Duration.ofSeconds(2));
        getTimers().startSingleTimer(null, "SAVE_MODEL", Duration.ofMinutes(2));
    }

    static public Props props(String save_id, S_Type type, Instances data, ActorRef parent) {
        return Props.create(Learner.class, () -> new Learner(save_id, type, data, parent));
    }


    public void handleEval(S_Type type, Classifier model, String conf) throws Exception
    {
        ClassRes new_cr = new ClassRes( type, model, data );
        used_configs.put(curr_conf,Saver.gradeToString( new_cr ));
        if( new_cr.compareTo( best_cr ) > 0 )
        {
            best_cr = new_cr;
            best = current;
            best_conf = conf;
            parent.tell( new ClassSlave.BestClass( best, best_conf, best_cr), self());
        }
    }


    private void onOptimizationStart(String s)
    {

        if( configs.isEmpty() ) {
            self().tell( PoisonPill.getInstance(), ActorRef.noSender() );
            return;
        }
        try
        {
            System.out.println(" ---------- OPT " + type);
//            curr_conf = configs.remove( r.nextInt( configs.size() ) );
            curr_conf = configs.remove( 0 );
            current = params.clasFromStr( curr_conf );
            current.buildClassifier( data );
            handleEval( type, current, curr_conf);

            System.out.println(curr_conf + " :  " + best_conf + " ");
            getTimers().startSingleTimer(null, "NEW_CONF", Duration.ofSeconds(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSaveModel(String s) throws Exception
    {
        System.out.println(save_id + "  ::  SAVED");
        Saver.saveModel( save_id, best, best_cr, type, data, used_configs);
        getTimers().startSingleTimer(null, "SAVE_MODEL", Duration.ofMinutes(2));
    }


    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("NEW_CONF", this::onSaveModel)
                .matchEquals("SAVE_MODEL", this::onOptimizationStart)
                .match(PoisonPill.class, x -> getContext().stop(self()))
                .matchAny(m -> {
                    System.out.println("?? : " + m.getClass());
                })
                .build();
    }

}
