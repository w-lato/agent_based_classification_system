package agh.edu;

import agh.edu.agents.Master;
import agh.edu.learning.DataSplitter;
import agh.edu.messages.M;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.sun.xml.internal.ws.api.policy.ModelGenerator;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.output.prediction.CSV;
import weka.classifiers.functions.SMO;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;


/**
 * http://weka.sourceforge.net/doc.dev/weka/classifiers/Classifier.html
 *
 * https://github.com/renatopp/arff-datasets/tree/master/classification
 *
 * Accuracy of NaiveBayes: 79.00%
 * ---------------------------------
 * Accuracy of BayesNet: 88.72%
 * ---------------------------------
 * Accuracy of J48: 88.00%
 * ---------------------------------
 * Accuracy of PART: 91.09%
 * ---------------------------------
 * Accuracy of DecisionTable: 87.76%
 * ---------------------------------
 * Accuracy of DecisionStump: 75.29%
 * ---------------------------------
 * Accuracy of HoeffdingTree: 60.75%
 * ---------------------------------
 * Accuracy of LMT: 88 .96%
 * ---------------------------------
 * Accuracy of JRip: 90.61%
 * ---------------------------------
 * Accuracy of ZeroR: 60.60%
 * ---------------------------------
 *
 *
 *  Diabetic data
 * Accuracy of NaiveBayes: 56.82%
 * ---------------------------------
 * Accuracy of BayesNet: 63.25%
 * ---------------------------------
 * Accuracy of J48: 62.90%
 * ---------------------------------
 * Accuracy of PART: 63.25%
 * ---------------------------------
 * Accuracy of DecisionTable: 61.77%
 * ---------------------------------
 * Accuracy of DecisionStump: 56.73%
 * ---------------------------------
 * Accuracy of HoeffdingTree: 58.21%
 * ---------------------------------
 *
 *
 *
 *
 * Accuracy of NaiveBayes: 94.67%
 * ---------------------------------
 * Accuracy of BayesNet: 91.33%
 * ---------------------------------
 * Accuracy of J48: 94.00%
 * ---------------------------------
 * Accuracy of PART: 90.67%
 * ---------------------------------
 * Accuracy of DecisionTable: 92.67%
 * ---------------------------------
 * Accuracy of DecisionStump: 36.67%
 * ---------------------------------
 * Accuracy of HoeffdingTree: 94.67%
 * ---------------------------------
 * Accuracy of LMT: 94.67%
 * ---------------------------------
 * Accuracy of JRip: 93.33%
 * ---------------------------------
 * Accuracy of ZeroR: 0.00%
 * ---------------------------------
 */
public class MAIN
{
    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
        Instances[][] split = new Instances[2][numberOfFolds];

        for (int i = 0; i < numberOfFolds; i++)
        {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }

        return split;
    }

    public static Evaluation classify(Classifier model,
                                      Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation evaluation = new Evaluation(trainingSet);

        model.buildClassifier(trainingSet);
        evaluation.evaluateModel(model, testingSet);

        return evaluation;
    }

    public static double calculateAccuracy(FastVector predictions) {
        double correct = 0;

        for (int i = 0; i < predictions.size(); i++) {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if (np.predicted() == np.actual()) {
                correct++;
            }
        }

        return 100 * correct / predictions.size();
    }


    public static double calculateAccuracy(ArrayList<Prediction> predictions) {
        double correct = 0;

        for (int i = 0; i < predictions.size(); i++) {
            NominalPrediction np = (NominalPrediction) predictions.get(i);
            if (np.predicted() == np.actual()) {
                correct++;
            }
        }

        return 100 * correct / predictions.size();
    }

    public static void main(String[] args)
    {
//        ActorSystem system = ActorSystem.create("testSystem");
//        ActorRef m = system.actorOf( Master.props() ,"master" );
//
//
//        ActorRef m2 = system.actorOf( Master.props() ,"master1" );
//        m2.tell( new M.Classify( "007" ), m );
//        System.out.println( system.child("master") );
//        System.out.println( system.child("master") );
//        System.out.println(m.path() );
//
//        m.tell( new Master.Init(10,1,1,0.2), m);
//        m.tell(new Master.GetList(), m);
//
//
//
//        System.out.println("KILL TEST");
//        m.tell( new Master.Kill(6), m);
//        m.tell(new Master.GetList(), m);



        try {
            // Read all the instances in the file (ARFF, CSV, XRFF, ...)
//            DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\messidor_features.arff");
//            DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\car.arff");
//            DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\iris.arff");
//            DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\spambase.arff");
            DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
//            DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\pendigits.arff");
            Instances instances = source.getDataSet();

            // Make the last attribute be the class
            instances.setClassIndex(instances.numAttributes() - 1);
//instances.setClassIndex(0);
            // Print header and instances.
//            System.out.println("\nDataset:\n");
//            System.out.println(instances);
//
//            // Print header and instances.
//            System.out.println("\nDataset:\n");
//            System.out.println(instances);

// Do 10-split cross validation
            Instances[][] split = crossValidationSplit(instances, 10);

            // Separate split into training and testing arrays
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
                    new SMO()
            };
// Run for each model
            List<Instances> L = DataSplitter.splitIntoTrainAndTest( instances,0.005);
            Instances train  = L.get(0);
            Instances test   = L.get(1);
            source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_test.arff");
            test = source.getDataSet();
            test.setClassIndex( test.numAttributes() - 1 );

            System.out.println( train.classIndex() );
            System.out.println( test.classIndex() );
            System.out.println( train.numAttributes() );
            System.out.println( train.attribute( train.numAttributes() - 1 ) );
            System.out.println( train.attribute( train.numAttributes() - 2 ) );
//            for (int i = 0; i < train.size(); i++) {
//                System.out.println( train.get(i).value(train.numAttributes() - 1) );
//            }
//train.setClassIndex(0);
//test.setClassIndex(0);
            for (int j = 0; j < models.length; j++)
            {
//                // Collect every group of predictions for current model in a FastVector
//                FastVector predictions = new FastVector();
//                // For each training-testing split pair, train and test the classifier
//                for (int i = 0; i < trainingSplits.length; i++)
//                {
//                    Evaluation validation = classify(models[j], trainingSplits[i], testingSplits[i]);
//                    predictions.appendElements(validation.predictions());
//
//                    // Uncomment to see the summary for each training-testing pair.
////                    System.out.println(models[j].toString());
//                }
//                // Calculate overall accuracy of current classifier on all splits
//                double accuracy = calculateAccuracy(predictions);
//
//                // Print current classifier's name and accuracy in a complicated,
//                // but nice-looking way.
//                System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
//                        + String.format("%.2f%%", accuracy)
//                        + "\n---------------------------------");

                Evaluation validation = new Evaluation(train);
                long s = System.currentTimeMillis();
                models[j].buildClassifier( train );
                s = System.currentTimeMillis()  - s;

                long s1 = System.currentTimeMillis();
                validation.evaluateModel( models[j], test );
                s1 = System.currentTimeMillis()  - s1;


                double acc = calculateAccuracy( validation.predictions() );

//                validation.predictions().forEach(x->{
//                    System.out.println( x.predicted() + " " +  x.actual() );
//                });

                System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
                        + String.format("%.2f%%", acc)
                        +" build : " + s + "   eval: " + s1
                        + "\n---------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/**

 Accuracy of SMO: 93.34% build : 715552   eval: 1180 - 0.7 split
 ---------------------------------

 Accuracy of SMO: 91.98% build : 68915   eval: 2698 - 0.3
 ---------------------------------


 0.9 split, and mnist_test
 Accuracy of SMO: 93.87% build : 1543239   eval: 668
 ---------------------------------

 mnist test + mnist_train 0-9
 ive_ref-win-x86_64.dll
 Accuracy of SMO: 93.87% build : 1530885   eval: 816

0.8
 Accuracy of SMO: 94.00% build : 1087280   eval: 743
 ---------------------------------
0.7
 Accuracy of SMO: 93.73% build : 707064   eval: 664
 ---------------------------------
0.6
 Accuracy of SMO: 93.56% build : 451800   eval: 704
 ---------------------------------

0.5
 Accuracy of SMO: 93.44% build : 276947   eval: 683
 ---------------------------------
0.4
 Accuracy of SMO: 92.75% build : 126806   eval: 905
 ---------------------------------

 0.3
 Accuracy of SMO: 92.65% build : 68365   eval: 986
 ---------------------------------

 0.2
 Accuracy of SMO: 91.66% build : 28064   eval: 693
 ---------------------------------

 0.1
 Accuracy of SMO: 91.25% build : 10228   eval: 670
 ---------------------------------
0.05
 Accuracy of SMO: 90.17% build : 3533   eval: 624
 ---------------------------------

 0.02
 Accuracy of SMO: 87.77% build : 1880   eval: 682
 ---------------------------------
0.01
 Accuracy of SMO: 86.41% build : 1385   eval: 656
 ---------------------------------
0.005
 Accuracy of SMO: 81.79% build : 1213   eval: 622
 ---------------------------------

 3533	2500
 10228	5500
 28064	11000
 68365	16500
 126806	22000
 276947	27500
 451800	32250
 707064	37750
 1087280	43000
 1530885	48000

 */