package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.agents.experiment.Saver;
import agh.edu.agents.ClassSlave.BestClass;
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
import java.util.*;
import java.util.stream.Collectors;

import static agh.edu.MAIN.crossValidationSplit;

// tODO inform Slave about the number of processed data instances
// TODO remove souts
// TODO does config has to be chosen randomly?
// TODO used instances
public class Learner extends AbstractActorWithTimers {

    private String save_id; // EXP/exp_dir/type_id
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
        parent.tell( new BestClass( best, best_conf, best_cr),self());

        System.out.println("Learner created");
        getTimers().startSingleTimer(null, "NEW_CONF", Duration.ofSeconds(2));
        getTimers().startSingleTimer(null, "SAVE_MODEL", Duration.ofMinutes(2));
    }

    private Learner(String save_id, Classifier best, S_Type type, Instances data, ActorRef parent, LinkedHashMap<String, String> used_configs) throws Exception
    {
        params = ParamsFactory.getParams( type, data );
        configs = params.getParamsCartProd();
        this.used_configs = used_configs;
        configs = configs.stream().filter( x-> !this.used_configs.containsKey(x) ).collect(Collectors.toList());
        this.best = best;
        this.best_conf = used_configs.get( used_configs.keySet().iterator().next() );
        this.best_cr = new ClassRes( type, best, data );

        if( configs.isEmpty() )
        {
            parent.tell( new BestClass( best, best_conf, best_cr), self());
            self().tell( PoisonPill.getInstance(), ActorRef.noSender() );
            return;
        }
        this.save_id = save_id;
        this.parent = parent;
        this.data = data;
        r = new Random(System.currentTimeMillis());
        this.type = type;

        System.out.println("Learner from load created");
        getTimers().startSingleTimer(null, "NEW_CONF", Duration.ofSeconds(2));
    }

    static public Props props(String save_id, S_Type type, Instances data, ActorRef parent) {
        return Props.create(Learner.class, () -> new Learner(save_id, type, data, parent));
    }

    static public Props props(String save_id, Classifier best, S_Type type, Instances data, ActorRef parent, LinkedHashMap<String, String> used_configs)
    {
        return Props.create(Learner.class, () -> new Learner(save_id, best, type, data, parent, used_configs));
    }


    public void handleEval(S_Type type, Classifier model, String conf) throws Exception
    {
        System.out.println(" %%%% ");
        ClassRes new_cr = new ClassRes( type, model, data );
        used_configs.put(curr_conf,Saver.gradeToString( new_cr ));
        if( new_cr.compareTo( best_cr ) > 0 )
        {
            best_cr = new_cr;
            best = current;
            best_conf = conf;
            parent.tell( new BestClass( best, best_conf, best_cr), self());
        }
    }


    private void onOptimizationStart(String s) throws Exception
    {
        if( configs.isEmpty() )
        {
            System.out.println( "ALL CONFIGS USED" );
            Saver.saveModel( save_id, best, best_cr, type, data, used_configs );
            getContext().stop(self());
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


    public Receive createReceive()
    {
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
