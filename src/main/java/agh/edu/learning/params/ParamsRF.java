package agh.edu.learning.params;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 *  brkTies - boole
 *  attImport - bool
 *  numOfFeatures - int (defualt 0) ??
 *  batchSize - int (default 100) ??
 */
public class ParamsRF implements Params
{
    @Override
    public Classifier clasFromStr(String params)
    {
        String[] p = params.split(",");
        RandomForest rf = new RandomForest();

//        rf.setSeed((int) System.currentTimeMillis());
        rf.setSeed( 1 ); // TODO to make it more predictable
        rf.setBreakTiesRandomly( Boolean.valueOf( p[0] ) );
        rf.setComputeAttributeImportance( Boolean.valueOf( p[1] ) );
        return rf;
    }

    @Override
    public List<String> getParamsCartProd()
    {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                l.add( (i==1) + "," + (j==1) );
            }
        }
        return l;
    }

    public static void main(String[] args) throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_train.arff");
        Instances instances = source.getDataSet();
        Instances train = instances.trainCV(20,0);
        Instances test = instances.testCV(20,0);


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


        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 10; k++) {
                    long s = System.currentTimeMillis();
                    RandomForest smo = new RandomForest();

                    boolean brkTies = i==0;
                    boolean attImport = j==0;
                    smo.setBreakTiesRandomly( brkTies );
                    smo.setComputeAttributeImportance( attImport );
                    smo.setBatchSize( String.valueOf( k * 100 ) );
                    System.out.println(" batch: " + smo.getBatchSize());
    //                smo.setMaxDepth( 10 ); // default 0 - unlimited
    //                smo.setNumDecimalPlaces( 10 ); //
//                    smo.setNumFeatures( k * 10 );
                    System.out.println( smo.getNumFeatures() );
                    smo.setSeed( i * 100 );

                    smo.buildClassifier( train );
                    String id = "RandomForest:brkTies:"+ brkTies  + ",attImport:" + attImport + ",numFeat:" + k + " ";
                    System.out.println("BUILD: " + id + (System.currentTimeMillis() - s) );
//                    System.out.println( train.size() );

                    s = System.currentTimeMillis();
                    Evaluation evaluation = new Evaluation( train );
                    evaluation.crossValidateModel( smo, test, 10, new Random( System.currentTimeMillis() ) );
//                    System.out.println( "$$ " + (System.currentTimeMillis() - s) );
                    System.out.println(evaluation.toSummaryString());
                }
            }
        }
    }
}
