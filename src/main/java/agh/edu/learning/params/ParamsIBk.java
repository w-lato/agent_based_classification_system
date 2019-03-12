package agh.edu.learning.params;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.Which parameters to randomly choose?
 * 2.How to deal with big delays?
 * 3. Time of single test of classifiers combinations? 10 mins.? 30 mins? More?
 * 4. What are filters? (classfier.setFilter( new Remove() ) ??
 *
 * window size - the bigger the better (at leas when it comes for mnist)
 * neighbour search method ??
 * kNN
 * meanSquared
 */
public class ParamsIBk implements Params
{
    @Override
    public Classifier clasFromStr(String params)
    {
        String[] p = params.split(",");
        IBk ibk = new IBk();
        ibk.setWindowSize( Integer.valueOf( p[0] ) );
        ibk.setKNN( Integer.valueOf( p[1] ) );
        ibk.setMeanSquared( Boolean.valueOf( p[2] ) );

        return ibk;
    }

    @Override
    public List<String> getParamsCartProd() {
        List<String> l = new ArrayList<>();
        int[] windows = {1,20,70,150,300,500,700,900,1200};
        for (int i = 0; i < windows.length; i++)
        {
            for (int j = 1; j < 11; j++)
            {
                for (int k = 0; k < 2; k++)
                {
                    l.add( windows[i] + "," + j + "," + (k==1));
                }
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

        System.out.println( train.classIndex() +  " : " + test.classIndex());

        do {
            test.remove(0);
        } while (test.size() >= 1000);

        IBk rf = new IBk();
        String opt[] = rf.getOptions();
        for (int i = 0; i < opt.length; i++) {
            System.out.println( opt[i] );
        }
        rf.buildClassifier( train );
        System.out.println( rf.classifyInstance( test.get(0) ) );

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 10; k++) {
                    long s = System.currentTimeMillis();
                    IBk smo = new IBk();
//                    smo.setNearestNeighbourSearchAlgorithm( new FilteredNeighbourSearch());
                    System.out.println( smo.getKNN() );

                    smo.setKNN( k + 1 );
//                    smo.setWindowSize( k * 300 ); // set window size
                    smo.buildClassifier( train );

                    System.out.println(" Squared:  " + smo.getMeanSquared());
                    System.out.println(" Cross:  " + smo.getCrossValidate());

                    System.out.println( train.size() );
                    System.out.println( smo.getKNN() );

                    s = System.currentTimeMillis();
                    System.out.println( smo.getKNN() + " **"  + " " );

                    Evaluation evaluation = new Evaluation( train );
                    evaluation.evaluateModel( smo, train );
                    //                    evaluation.crossValidateModel( smo, test, 10, new Random( System.currentTimeMillis() ) );
                    System.out.println( "$$ " + (System.currentTimeMillis() - s) );
                    System.out.println(evaluation.toSummaryString());
                }
            }
        }
    }
}
