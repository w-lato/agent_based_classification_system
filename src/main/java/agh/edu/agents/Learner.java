package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
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
import java.util.List;
import java.util.Random;
import static agh.edu.MAIN.crossValidationSplit;

// tODO inform Slave about the number of processed data instances
// TODO remove souts
// TODO does config has to be chosen randomly?
public class Learner extends AbstractActorWithTimers {

    private ActorRef parent;

    private ClassRes best_cr;
    private String best_conf = "default";
    private Classifier best;

    private Random r;
    private S_Type type;

    private String curr_conf;
    private Params params;

    private List<String> configs;
    private Classifier current;

    private Instances data;

    private static Object OPT_KEY = "OPTIMIZE";

    /**
     * Builds defualt classifier from obtained data and saves it
     * as current and as best classifier.
     *
     */
    // TODO start self-optimizaiton and send current model to ClassSlave
    public Learner(S_Type type, Instances data, ActorRef parent) throws Exception
    {
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
        parent.tell( new ClassSlave.BestClass( best, best_conf, best_cr),self());

        System.out.println("Learner created");
        getTimers().startSingleTimer(null, "NEW_CONF", Duration.ofSeconds(2));
    }

    static public Props props(S_Type type, Instances data, ActorRef parent) {
        return Props.create(Learner.class, () -> new Learner(type, data, parent));
    }


    public void handleEval(S_Type type, Classifier model, String conf) throws Exception
    {
        ClassRes new_cr = new ClassRes( type, model, data );
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
            System.out.println(" ---------- OPT ");
//            curr_conf = configs.remove( r.nextInt( configs.size() ) );
            curr_conf = configs.remove( 0 );
            current = params.clasFromStr( curr_conf );
            current.buildClassifier( data );
            handleEval( type, current, curr_conf);

            System.out.println(curr_conf + " :  " + best_conf );
            getTimers().startSingleTimer(null, "NEW_CONF", Duration.ofSeconds(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("NEW_CONF", this::onOptimizationStart)
                .match(PoisonPill.class, x -> getContext().stop(self()))
                .matchAny(m -> {
                    System.out.println("?? : " + m.getClass());
                })
                .build();
    }

    public static void main(String[] args) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
        Instances instances = source.getDataSet();
        instances.setClassIndex(instances.numAttributes() - 1);
        Instances[][] split = crossValidationSplit(instances, 10);
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];


        System.out.println("\nDataset:\n");
        System.out.println(trainingSplits.length);

        System.out.println("\nDataset:\n");
        System.out.println(testingSplits.length);

        // Use a set of classifiers
        Classifier[] models = {
                new SMO(),
                new SMO(),
                new SMO(),
                new SMO(),
                new SMO(),
                new SMO(),
                new SMO(),
                new SMO(),
                new SMO(),
                new SMO(),
                new SMO()
        };
        // Run for each model
        Instances train = instances.trainCV(20,0);
        Instances test = instances.testCV(20,0);

        source = new ConverterUtils.DataSource("C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_test.arff");
        test = source.getDataSet();
        test.setClassIndex(test.numAttributes() - 1);

        System.out.println(train.classIndex());
        System.out.println(test.classIndex());
        System.out.println(train.numAttributes());
        System.out.println(train.attribute(train.numAttributes() - 1));
        System.out.println(train.attribute(train.numAttributes() - 2));
        for (int j = 0; j < models.length; j++) {

            if (j == 0) {
                ((SMO) models[0]).setKernel(new PolyKernel(train, 7, 2.0, false));
//                ((SMO) models[0]).setC(2.0);
//                ((SMO) models[0]).setRandomSeed(123123);
//                ((SMO) models[0]).setToleranceParameter(0.001);
//                ((SMO) models[0]).setEpsilon(0.001);
//                ((SMO) models[0]).setCalibrator( new LinearRegression());
//                ((SMO) models[0]).setCalibrator( new SGD());
//                ((SMO) models[0]).setCalibrator( new MultilayerPerceptron());
                ((SMO) models[0]).setNumFolds(3);

            } else if (j == 1) {
                ((SMO) models[1]).setKernel(new PolyKernel(train, 7, 3.0, false));
            } else if (j == 2) {
                ((SMO) models[2]).setKernel(new PolyKernel(train, 7, 2.0, true));
            } else if (j == 3) {
                ((SMO) models[3]).setKernel(new PolyKernel(train, 7, 3.0, true));
            } else if (j == 4) {
                ((SMO) models[4]).setKernel(new PolyKernel(train, 7, 4.0, false));
            } else if (j == 5) {
                ((SMO) models[5]).setKernel(new PolyKernel(train, 7, 5.0, false));
            } else if (j == 6) {
                ((SMO) models[6]).setKernel(new RBFKernel(train, 7, 0.2));
            } else if (j == 7) {
                ((SMO) models[7]).setKernel(new RBFKernel(train, 7, 0.5));
            } else if (j == 8) {
                ((SMO) models[8]).setKernel(new RBFKernel(train,7,0.01));
            } else if (j == 9) {
                ((SMO) models[9]).setKernel(new RBFKernel(train,7,0.005));
            }
            String[] as = ((SMO) models[j]).getOptions();
            for (int i = 0; i < as.length; i++)
            {
                System.out.println( as[i] );
            }
            Evaluation validation = new Evaluation(train);
            long s = System.currentTimeMillis();
            models[j].buildClassifier(train);
            s = System.currentTimeMillis() - s;

            long s1 = System.currentTimeMillis();
            validation.evaluateModel(models[j], test);
            s1 = System.currentTimeMillis() - s1;


//            double acc = calculateAccuracy(validation.predictions());
//            System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
//                    + String.format("%.2f%%", acc)
//                    + " build : " + s + "   eval: " + s1
//                    + "\n---------------------------------");

        }
    }
}
