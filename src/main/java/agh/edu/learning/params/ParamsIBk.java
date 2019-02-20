package agh.edu.learning.params;

import agh.edu.learning.DataSplitter;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.neighboursearch.LinearNNSearch;

import java.util.List;
import java.util.Random;

/**
 * 1.Which parameters to randomly choose?
 * 2.How to deal with big delays?
 * 3. Time of single test of classifiers combinations? 10 mins.? 30 mins? More?
 * 4. What are filters? (classfier.setFilter( new Remove() ) ??
 */
public class ParamsIBk
{
    public static void main(String[] args) throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_train.arff");
        Instances instances = source.getDataSet();
        List<Instances> L = DataSplitter.splitIntoTrainAndTest(instances, 0.05);
        Instances train = L.get(0);
        Instances test = L.get(1);

        do {
            test.remove(0);
        } while (test.size() >= 1000);

        RandomForest rf = new RandomForest();
        String opt[] = rf.getOptions();
        for (int i = 0; i < opt.length; i++) {
            System.out.println( opt[i] );
        }
        System.out.println( rf.getNumFeatures() );
        rf.buildClassifier( train );
        System.out.println( rf.getNumFeatures() );

        LinearNNSearch as = new LinearNNSearch();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 10; k++) {
                    long s = System.currentTimeMillis();
                    IBk smo = new IBk();
                    System.out.println( smo.getKNN() );
                    boolean cross = i == 0;
                    boolean meanSq = j == 0;
                    smo.buildClassifier( train );

                    smo.setWindowSize( 10 ); // set window size
//                    smo.
//                    smo.setMeanSquared( meanSq ); //

                    System.out.println(" Squared:  " + smo.getMeanSquared());
                    System.out.println(" Cross:  " + smo.getCrossValidate());
                    System.out.println(" Cross:  ");

                    for (String option : smo.getOptions()) {
                        System.out.println( option );
                    }

//                    smo.setCrossValidate( cross );
//                    smo.setMeanSquared( meanSq );
//                    smo.set

//                    String id = "RandomForest:brkTies:"+ brkTies  + ",attImport:" + attImport + ",numFeat:" + k + " ";
//                    System.out.println("BUILD: " + id + (System.currentTimeMillis() - s) );
                    System.out.println( train.size() );
                    System.out.println( smo.getKNN() );

                    s = System.currentTimeMillis();
                    Evaluation evaluation = new Evaluation( train );
                    evaluation.crossValidateModel( smo, test, 10, new Random( System.currentTimeMillis() ) );
                    System.out.println( "$$ " + (System.currentTimeMillis() - s) );
                    System.out.println(evaluation.toSummaryString());
                }
            }
        }
    }
}
