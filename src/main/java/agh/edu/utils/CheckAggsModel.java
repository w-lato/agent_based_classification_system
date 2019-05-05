package agh.edu.utils;

import agh.edu.agents.experiment.ArffMaker;
import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.ParamsLog;
import agh.edu.learning.params.ParamsMLP;
import agh.edu.learning.params.ParamsSMO;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.List;

public class CheckAggsModel
{
    public static void main(String[] args) throws Exception {
        Instances TEST_RAW = ConverterUtils.DataSource.read("DO_NOT_DEL/Q_39_TEST_RAW.arff");
        TEST_RAW.setClassIndex( TEST_RAW.numAttributes() - 1 );

        Instances TEST_WEIGHT = ConverterUtils.DataSource.read("DO_NOT_DEL/Q_48_TEST_NORM_GRADE.arff");
        TEST_WEIGHT.setClassIndex( TEST_WEIGHT.numAttributes() - 1 );
//        Instances TEST_LOG = ConverterUtils.DataSource.read("DO_NOT_DEL/Q_39_TEST_LOG.arff");
//        TEST_LOG.setClassIndex( TEST_LOG.numAttributes() - 1 );

        Instances TRAIN_RAW = ConverterUtils.DataSource.read("DO_NOT_DEL/Q_38_TRAIN_RAW.arff");
        TRAIN_RAW.setClassIndex( TRAIN_RAW.numAttributes() - 1 );
        Instances TRAIN_WEIGHT = ConverterUtils.DataSource.read("DO_NOT_DEL/Q_46_TRAIN_NORM_GRADE.arff");
        TRAIN_WEIGHT.setClassIndex( TRAIN_WEIGHT.numAttributes() - 1 );
//        Instances TRAIN_LOG = ConverterUtils.DataSource.read("DO_NOT_DEL/Q_38_TRAIN_LOG.arff");
//        TRAIN_LOG.setClassIndex( TRAIN_LOG.numAttributes() - 1 );


        System.out.println("====================== RAW ");
        SMO raw = new SMO();
        raw.buildClassifier( TRAIN_RAW );
        Evaluation eval_raw = new Evaluation( TEST_RAW );

        eval_raw.evaluateModel( raw, TEST_RAW );
        System.out.println( eval_raw.toSummaryString() );
        System.out.println();
//
        System.out.println("====================== WEIGHT ");
        SMO weight = new SMO();
        weight.buildClassifier( TRAIN_WEIGHT );

        Evaluation eval_weight = new Evaluation( TEST_WEIGHT );
        eval_weight.evaluateModel( weight, TEST_WEIGHT );
        System.out.println( eval_weight.toSummaryString() );
        System.out.println();


        // NaN exceptions
//        System.out.println("====================== LOG");
//        SMO log = new SMO();
//        log.buildClassifier( TRAIN_LOG );
//
//        Evaluation eval_log = new Evaluation( TEST_LOG );
//        eval_log.evaluateModel( raw, TEST_LOG );
//        System.out.println( eval_log.toSummaryString() );
//        System.out.println();



        ParamsLog paramsLOG = new ParamsLog(  );
        List<String> confs = paramsLOG.getParamsCartProd();

        // 0,0.001,CONJUGATE_GRADIENT,RELU,2,300,500 lasts more than 3h...
        for (String conf : confs)
        {
            try{
                System.out.println("====================== " + conf);
                Logistic log = (Logistic) paramsLOG.clasFromStr( conf );
                log.buildClassifier( TRAIN_WEIGHT );

                Evaluation e = new Evaluation( TEST_WEIGHT );
                e.evaluateModel( log, TEST_WEIGHT );
                System.out.println( e.toSummaryString() );
                System.out.println();
            } catch (Exception e)
            {
                System.out.println( "!!!  " + conf );
            }
        }


//        ParamsSMO paramsSMO = new ParamsSMO(  );
//        List<String> confs = paramsSMO.getParamsCartProd();
//
//        // 0,0.001,CONJUGATE_GRADIENT,RELU,2,300,500 lasts more than 3h...
//        for (String conf : confs)
//        {
//            try{
//                System.out.println("====================== " + conf);
//                SMO smo = (SMO) paramsSMO.clasFromStr( conf );
//                smo.buildClassifier( TRAIN_WEIGHT );
//
//                Evaluation e = new Evaluation( TEST_WEIGHT );
//                e.evaluateModel( smo, TEST_WEIGHT );
//                System.out.println( e.toSummaryString() );
//                System.out.println();
//            } catch (Exception e)
//            {
//                System.out.println( "!!!  " + conf );
//            }
//        }


//        ParamsMLP paramsMLP = new ParamsMLP( TRAIN_WEIGHT );
//        List<String> confs = paramsMLP.getParamsCartProd();
//
//        // 0,0.001,CONJUGATE_GRADIENT,RELU,2,300,500 lasts more than 3h...
//        for (String conf : confs)
//        {
//            try{
//                System.out.println("====================== " + conf);
//                MLP mlp = (MLP) paramsMLP.clasFromStr( conf );
//                mlp.buildClassifier( TRAIN_WEIGHT );
//
//                Evaluation e = new Evaluation( TEST_WEIGHT );
//                e.evaluateModel( mlp, TEST_WEIGHT );
//                System.out.println( e.toSummaryString() );
//                System.out.println();
//            } catch (Exception e)
//            {
//                System.out.println( "!!!  " + conf );
//            }
//        }
    }
}
