package agh.edu.learning.params;

import agh.edu.learning.DataSplitter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.List;
import java.util.Random;

public class ParamsRF implements Params
{
    String conf;

    @Override
    public Classifier clasFromStr(String params) {
        return null;
    }

    @Override
    public String getConf() {
        return null;
    }

    @Override
    public Classifier genRandomParams(Random gen)
    {
        RandomForest rf = new RandomForest();
        boolean brkTies = gen.nextBoolean();
        boolean attImport = gen.nextBoolean();
        rf.setBreakTiesRandomly( brkTies );
        rf.setComputeAttributeImportance( attImport );
//                smo.setMaxDepth( 10 ); // default 0 - unlimited
//                smo.setNumDecimalPlaces( 10 ); //
        int numOfFeatures = gen.nextInt();
        rf.setNumFeatures( numOfFeatures );
        rf.setSeed( gen.nextInt() );

        conf = "RF:brkTies:" + brkTies + ",attImport:" + attImport + ",numFeatures:" + numOfFeatures;
        return rf;
    }

    public static void main(String[] args) throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
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


        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 10; k++) {
                    long s = System.currentTimeMillis();
                    RandomForest smo = new RandomForest();

                    boolean brkTies = i==0;
                    boolean attImport = j==0;
                    smo.setBreakTiesRandomly( brkTies );
                    smo.setComputeAttributeImportance( attImport );
    //                smo.setMaxDepth( 10 ); // default 0 - unlimited
    //                smo.setNumDecimalPlaces( 10 ); //
                    smo.setNumFeatures( k );
//                    smo.set
                    smo.setSeed( i );

                    smo.buildClassifier( train );
                    String id = "RandomForest:brkTies:"+ brkTies  + ",attImport:" + attImport + ",numFeat:" + k + " ";
                    System.out.println("BUILD: " + id + (System.currentTimeMillis() - s) );
                    System.out.println( train.size() );

//AdaBoostM1 -- TODO dodać
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
