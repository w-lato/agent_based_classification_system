package agh.edu.messages;

import agh.edu.learning.DataSplitter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.Utils;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class M
{
    // TODO - data format

    static public class Learn
    {
        public final String agentId;
        public final String who;

        public Learn(String who, String agentId)
        {
            this.agentId = agentId;
            this.who = who;
        }
    }

    static public class Classify
    {
        public final String agentId;

        public Classify(String agentId)
        {
            this.agentId = agentId;
        }
    }

    static public class SetAlgorithm
    {
        public final String agentId;
        public final int alg;

        public SetAlgorithm(String agentId, int alg)
        {
            this.alg = alg;
            this.agentId = agentId;
        }
    }


    static public class Greet {
        public Greet() {
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        // load data
        Instances data = null;
        try {
            data = new Instances(
                    new BufferedReader(
                            new FileReader("DATA/spambase.arff")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        data.setClassIndex(data.numAttributes() - 1);

        //split data
        for (double i = 0.1; i < 1; i+=0.01)
        {
            List<Instances> DD = DataSplitter.splitIntoTrainAndTest( data, i );
//        double percent = i;
//        int trainSize = (int) Math.round(data.numInstances() * percent / 100);
//        int testSize = data.numInstances() - trainSize;
//        Instances train = new Instances(data, 0, trainSize);
//        Instances test = new Instances(data, trainSize, testSize);

            Instances train = DD.get(0);
            Instances test = DD.get(1);
            // train classifier
            Classifier cl = new SMO();
            try {
                cl.buildClassifier( train );
                Evaluation eval = new Evaluation( test );
                eval.evaluateModel( cl, train );

                ThresholdCurve tc = new ThresholdCurve();
                int classIndex = 0;
//                Instances result = tc.getCurve(eval.predictions(), classIndex);
                Instances result = tc.getCurve(eval.predictions());
                System.out.println( result.relationName() + " " + result.numInstances() );
                //print results
                System.out.println(i +  " : Area under ROC: " +
                        Utils.doubleToString(
                                ThresholdCurve.getROCArea( result )
                                , 4));
                System.out.println("Accuracy: " + eval.pctCorrect());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
