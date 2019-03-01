package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.learning.DataSplitter;
import agh.edu.learning.DefaultClassifierFactory;
import agh.edu.learning.Evaluator;
import agh.edu.learning.ParamsFactory;
import agh.edu.learning.params.Params;
import akka.actor.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.*;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static agh.edu.MAIN.crossValidationSplit;
import static agh.edu.learning.DataSplitter.calculateAccuracy;

// TODO - from one  change some things
public class Learner extends AbstractActorWithTimers {

    // TODO some kind of evaluator to get acc and other measures
    double best_acc;
    Evaluation eval;


    private Random r;
    private List<Instances> TEN_FOLDS;
    private S_Type type;
    String best_conf = "";
    private String curr_conf = "default";
    private Params params;

    private List<String> configs;
    private Classifier best;
    private Classifier current;
    private Instances data;

    private static Object OPT_KEY = "OPTIMIZE";

    /**
     * Builds defualt classifier from obtained data and saves it
     * as current and best classifier.
     *
     */
    public Learner(S_Type type, Instances data) throws Exception
    {
        eval = new Evaluation(data);
        this.data = data;
        if( type.equals( S_Type.MLP ) )
        {
            params = ParamsFactory.getMLP( data.numClasses(), data.numAttributes() - 1 );
            configs = params.getParamsCartProd();
            setupNewconf( configs.remove(0) );
        } else {
            params = ParamsFactory.getParams( type );
            configs = params.getParamsCartProd();
            current = DefaultClassifierFactory.getClassifier(type);
        }
        best = current;
        this.type = type;

        // eval
        createNFolds( 10, data );
        handleEval();

        System.out.println("Learner created");
        getTimers().startSingleTimer(OPT_KEY, "OPT_START", Duration.ofSeconds(1));
    }

//    static public Props props() {
//        return Props.create(AbstractLearner.class, () -> new AbstractLearner());
//    }

    private void setupNewconf(String conf) throws Exception {
        current = params.clasFromStr( conf );
        curr_conf = conf;
        current.buildClassifier( data );
    }

    private void handleEval() throws Exception {
        eval.toClassDetailsString();
        try {
            eval.crossValidateModel( current,  data,10 , new Random(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        eval.
//        if( best_acc == null ||  )
    }

    private void createNFolds(int N, Instances data)
    {
        TEN_FOLDS = new ArrayList<>();
        for (int i = 0; i < N; i++)
        {
            TEN_FOLDS.add( data.trainCV( N, i ) );
        }
    }

    private void onOptimizationStart()
    {
        if( configs.isEmpty() ) return;


//        System.out.println("VOTING SET TO: " + sv.method);
//        this.vote_method = sv.method;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
//                .matchEquals("OPT_START", m ->
//                {
//                    System.out.println("Tick received");
//                    getTimers().startSingleTimer(TEST_KEY, "start", Duration.ofSeconds(2));
//                    // Do someting
//                })
//                .matchEquals("Stop", m -> {
//                    System.out.println("cyclic event stopped");
//                    getTimers().cancelAll();
//                })
                .matchEquals("Start", this::onOptimizationStart)
//                .matchEquals("Start", this::onStop)

//                {
//                    System.out.println("job started");
//                    for (long i = 0; i < Long.MAX_VALUE; i += 20) {
//                        if (i % 2000000000.0 == 0) System.out.println("progress... ");
//                        if (i == 10000000000.0) break;
//                    }
////                    System.out.println("cyclic event stopped");
//                    getTimers().startSingleTimer(TEST_KEY, "Start", Duration.ofSeconds(2));
//                })
                .matchAny(m -> {
                    System.out.println("?? : " + m.getClass());
//                    getTimers().startSingleTimer(TEST_KEY, "Stop", Duration.ofSeconds(2));
                })
                .build();
    }

    private <P> void onOptimizationStart(P p) {
    }



    public static void main(String[] args) throws Exception {


//        ActorSystem system = ActorSystem.create("testSystem");
//        ActorRef learner = system.actorOf(Learner.props());
//
//        Thread.sleep(2000);
//        learner.tell("Stop", ActorRef.noSender());
//
//        Classifier c = new SMO();
//        Kernel k = new Kernel()


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
//                    new NaiveBayes(),
//                    new BayesNet(),
                //new J48(), // a decision tree
                //new PART(),
//                    new DecisionTable(),//decision table majority classifier
//                    new DecisionStump(), //one-level decision tree
//                    new HoeffdingTree(),
//                    new LMT(),
//                    new JRip(),
//                    new ZeroR(),
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
        List<Instances> L = DataSplitter.splitIntoTrainAndTest(instances, 0.005);
        Instances train = L.get(0);
        Instances test = L.get(1);
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


            double acc = calculateAccuracy(validation.predictions());
            System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
                    + String.format("%.2f%%", acc)
                    + " build : " + s + "   eval: " + s1
                    + "\n---------------------------------");

        }
    }
}
