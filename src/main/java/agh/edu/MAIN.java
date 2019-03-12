package agh.edu;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.SMO;
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