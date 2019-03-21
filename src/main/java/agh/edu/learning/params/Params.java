package agh.edu.learning.params;

import weka.classifiers.Classifier;

import java.util.List;
import java.util.Random;

public interface Params
{
    public Classifier clasFromStr(String params);
    public List<String> getParamsCartProd();
}
