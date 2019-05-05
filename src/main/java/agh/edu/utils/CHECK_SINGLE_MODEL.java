package agh.edu.utils;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

public class CHECK_SINGLE_MODEL
{
    public static Instances test;
    static {
        try {
            test = new ConverterUtils.DataSource("DATA/mnist_test.arff").getDataSet();
            test.setClassIndex( test.numAttributes() - 1 );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkmodel(String path) throws Exception {
        Classifier model = (Classifier) SerializationHelper.read(path);
        Evaluation e = new Evaluation( test );
        e.evaluateModel( model, test );

        System.out.println( e.toSummaryString() );
        for (int i = 0; i < 10; i++) {
            System.out.println(i + " : " + e.fMeasure(i));
        }
    }

    public static void main(String[] args) throws Exception {
        checkmodel("EXP/TIME_SMO_8_0.5_12/SMO_1.model");
        checkmodel("EXP/TIME_SMO_8_0.5_12/SMO_2.model");
        checkmodel("EXP/TIME_SMO_8_0.5_12/SMO_3.model");
        checkmodel("EXP/TIME_SMO_8_0.5_12/SMO_4.model");
        checkmodel("EXP/TIME_SMO_8_0.5_12/SMO_5.model");
        checkmodel("EXP/TIME_SMO_8_0.5_12/SMO_6.model");
        checkmodel("EXP/TIME_SMO_8_0.5_12/SMO_7.model");
        checkmodel("EXP/TIME_SMO_8_0.5_12/SMO_8.model");
        System.out.println("============================================");
//        checkmodel("D:\\MNIST_MODELS\\TIME_COMP\\SMO\\SMO_50");
        System.out.println("END?");
    }
}
