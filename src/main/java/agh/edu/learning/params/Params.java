package agh.edu.learning.params;

import weka.classifiers.Classifier;

import java.util.List;
import java.util.Random;

public interface Params
{
    public Classifier clasFromStr(String params);
//    public String getConf();
//    public Classifier genRandomParams(Random gen);
    public List<String> getParamsCartProd();
}
