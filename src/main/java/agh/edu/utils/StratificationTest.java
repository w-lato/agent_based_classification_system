package agh.edu.utils;

import agh.edu.agents.Aggregator;
import agh.edu.agents.experiment.ArffMaker;
import agh.edu.agents.experiment.Loader;
import agh.edu.aggregation.ClassGrade;
import agh.edu.learning.params.ParamsSMO;
import akka.actor.ActorRef;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StratificationTest
{
    public static void main(String[] args) throws Exception
    {
//        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_train.arff");
//        Instances instances = source.getDataSet();
//        instances.setClassIndex( instances.numAttributes() - 1);
//
//        long ctr = instances.stream().filter(x -> x.classValue() == 0.0).count();
//        System.out.println( ctr );
//
//        int N = 10;
//        instances.stratify( N );
//        for (int i = 0; i < N; i++)
//        {
//            Instances part = instances.testCV(N, i );
//            ctr = part.stream().filter( x -> x.classValue() == 0.0 ).count();
//            System.out.println( i +   " "  + ctr );
//        }

//        ArffMaker.aggResToArff( "EXP/SLAVE_ONLY_TEST_1/AGG/Q_30.res","EXP/SLAVE_ONLY_TEST_1/AGG/Q_30.arff" );

//        Path p = Paths.get("EXP/SLAVE_ONLY_TEST_1/AGG/agg.conf");
//        List<String> l = Files.readAllLines( p );
//        String exp_id = l.get(0);
//        Map<String, ClassGrade> m = new HashMap<>();
//        if( l.size() > 1 )
//        {
//            for (int i = 1; i < l.size(); i++)
//            {
//                String[] aux = l.get(i).split("@");
//                m.put( exp_id + "/" +aux[0], ClassGrade.fromString( aux[1] ) );
//            }
//        }

        Instances data = ConverterUtils.DataSource.read("D:\\masters_thesis\\masters_thesis\\EXP\\SLAVE_ONLY_TEST_1\\AGG\\Q_30_TRAIN.arff");
        data.setClassIndex( data.numAttributes() - 1 );
        SMO smo = new SMO();
        smo.buildClassifier( data );


        Instances test = ConverterUtils.DataSource.read("D:\\masters_thesis\\masters_thesis\\EXP\\SLAVE_ONLY_TEST_1\\AGG\\Q_5_TRAIN.arff");
        test.setClassIndex( test.numAttributes() - 1 );
        Evaluation evaluation = new Evaluation( test );
        evaluation.evaluateModel( smo, test );
        System.out.println( evaluation.toSummaryString() );


//        data.forEach( x-> {
//            for (int i = 0; i < x.numAttributes(); i++)
//            {
//                System.out.print( x.value( i ) + ", " );
//            }
//            System.out.println();
//        } );
        //        data.setClassIndex( data.numAttributes() - 1 );
//
//        SMO smo = new SMO();
//        ParamsSMO params = new ParamsSMO();
//        for (String s : params.getParamsCartProd())
//        {
//            System.out.println( "=========== " +s );
//            smo = (SMO)params.clasFromStr( s );
//            smo.buildClassifier( data );
//
//            Evaluation evaluation = new Evaluation( data );
//
//            Instances test = ConverterUtils.DataSource.read("D:\\masters_thesis\\masters_thesis\\EXP\\SLAVE_ONLY_TEST_1\\AGG\\Q_5_TRAIN.arff");
//            test.setClassIndex( test.numAttributes() - 1 );
//            evaluation.crossValidateModel( smo, test,10 , new Random(1));
//            System.out.println(  evaluation.toSummaryString() );
//        }
    }
}
