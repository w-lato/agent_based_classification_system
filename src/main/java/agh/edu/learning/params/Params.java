package agh.edu.learning.params;

import weka.classifiers.Classifier;

import java.util.Random;

public interface Params
{
    public Classifier clasFromStr(String params);
    public String toString();
    public Classifier genRandomParams(Random gen);
}
