package agh.edu.utils;

import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.ParamsMLP;
import agh.edu.learning.params.ParamsSMO;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static weka.core.converters.ConverterUtils.DataSource.read;

public class TRAINING_TIMES
{
    private static final String mlp_path = "D:\\MNIST_MODELS\\TIME_COMP\\MLP";
    private static final String smo_path = "D:\\MNIST_MODELS\\TIME_COMP\\SMO";
    public static Instances test;
    public  static  List<Integer> s;
    static {
        try {
            test = read("DATA/mnist_test.arff");
            test.setClassIndex( test.numAttributes() - 1 );

            s = new ArrayList<>();
//            s.add( 1 );
//            s.add( 2 );
////            s.add( 1 ); // 5%
////            s.add( 2 );
//            s.add( 3 );
//            s.add( 4 );
////            s.add( 5 );
//            s.add( 6 );
////            s.add( 7 );
//            s.add( 8 );
////            s.add( 9 );
            s.add( 10 );
////            s.add( 11 );
            s.add( 12 );
////            s.add( 13 );
            s.add( 14 );
////            s.add( 15 );
            s.add( 16 );
////            s.add( 17 );
            s.add( 18 );
////            s.add( 19 );
            s.add( 20 );



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void trainWithN(int n, Instances data, Classifier model, String model_name
    ) throws Exception
    {

        int parts = 20;
        data.stratify( parts );
        Instances TO_TRAIN = data.testCV( parts, 0 );
        for (int i = 1; i < n; i++)
        {
            TO_TRAIN.addAll(data.testCV(parts, i));
        }

        System.out.println( "SIZE: " + TO_TRAIN.size() );

        long s = System.currentTimeMillis();
        model.buildClassifier( TO_TRAIN );

        System.out.println( "TIME: " + (System.currentTimeMillis() - s) / 1000 );
//        SerializationHelper.write( mlp_path+ "\\" + model_name, model );
        SerializationHelper.write( smo_path+ "\\" + model_name, model );
        Evaluation e = new Evaluation(test);
        e.evaluateModel( model, test );
        System.out.println( e.toSummaryString() );
        for (int i = 0; i < 10; i++)
        {
            System.out.println( i + " : " + e.fMeasure( i ));
        }
        System.out.println();
    }

    public static void main(String[] args) throws Exception
    {
        Instances ORIG = read("DATA/mnist_train.arff");
        ORIG.setClassIndex( ORIG.numAttributes() - 1 );
//        ParamsMLP p = new ParamsMLP(ORIG);
        ParamsSMO p = new ParamsSMO();

        ORIG.stratify(200);
        Instances half_percent = ORIG.testCV( 200, 0 );
        SMO mlp = (SMO) p.clasFromStr("1,3.0,false");

//        long start = System.currentTimeMillis();
//        mlp.buildClassifier( half_percent );
//        System.out.println("TIME: " + (System.currentTimeMillis() - start) + " :  SIZE: "+ half_percent.size());
//        Evaluation e = new Evaluation(test);
//        e.evaluateModel( mlp,test );
//        System.out.println( e.toSummaryString() );
//        for (int i = 0; i < 10; i++)
//        {
//            System.out.println( i + " : " + e.fMeasure( i ));
//        }
//        System.out.println();
//
//
//        ORIG.stratify(100);
//        Instances one_percent = ORIG.testCV( 100, 0 );
//        mlp = (SMO) p.clasFromStr("1,3.0,false");
//
//        start = System.currentTimeMillis();
//        mlp.buildClassifier( one_percent );
//        System.out.println("TIME: " + (System.currentTimeMillis() - start) + " :  SIZE: "+ one_percent.size());
//        e = new Evaluation(one_percent);
//        e.evaluateModel( mlp,test );
//        System.out.println( e.toSummaryString() );
//        for (int i = 0; i < 10; i++)
//        {
//            System.out.println( i + " : " + e.fMeasure( i ));
//        }
//        System.out.println();
//
//
//        ORIG.stratify(43);
//        Instances three_percent = ORIG.testCV( 43, 0 );
//        mlp = (SMO) p.clasFromStr("1,3.0,false");
//
//        start = System.currentTimeMillis();
//        mlp.buildClassifier( three_percent );
//        System.out.println("TIME: " + (System.currentTimeMillis() - start) + " :  SIZE: "+ three_percent.size());
//        e = new Evaluation(test);
//        e.evaluateModel( mlp,test );
//        System.out.println( e.toSummaryString() );
//        for (int i = 0; i < 10; i++)
//        {
//            System.out.println( i + " : " + e.fMeasure( i ));
//        }
//        System.out.println();
//
//


        for (Integer i : s)
        {
            mlp = (SMO) p.clasFromStr("1,3.0,false");
            trainWithN( i, ORIG, mlp, "SMO_" + (i*5) );
        }



//        MLP mlp = (MLP) SerializationHelper.read("D:\\MNIST_MODELS\\TRAIN_100\\MLP_100_1000,2,300,1000_111.model");
//        Evaluation e = new Evaluation( test );
//        e.evaluateModel( mlp, test );
//
//        System.out.println( e.toSummaryString() );
//        for (int i = 0; i < 10; i++)
//        {
//            System.out.println( e.fMeasure( i ) );
//        }
    }
}
