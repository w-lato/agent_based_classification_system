package agh.edu.utils;

import agh.edu.agents.experiment.ArffMaker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

public class CheckModel
{
    public static void main(String[] args) throws Exception
    {


        ArffMaker.aggResToArff("EXP/SLAVE_ONLY_TEST_1/AGG/Q_WEIGHT_48.res","EXP/SLAVE_ONLY_TEST_1/AGG/Q_48.arff");
        Instances xd  = ConverterUtils.DataSource.read("EXP/SLAVE_ONLY_TEST_1/AGG/Q_48_TRAIN.arff");



    }
}
