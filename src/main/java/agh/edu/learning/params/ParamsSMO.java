package agh.edu.learning.params;

import weka.classifiers.Classifier;

import java.awt.image.Kernel;
import java.util.Random;

public class ParamsSMO implements Params
{
    Kernel ker;
    int num_of_folds;//


    @Override
    public String toString()
    {
        return super.toString();
    }

    @Override
    public Classifier clasFromStr(String params) {
        return null;
    }

    @Override
    public Classifier genRandomParams(Random gen) {
        return null;
    }
}
