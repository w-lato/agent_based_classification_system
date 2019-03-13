package agh.edu.learning;

import agh.edu.agents.enums.S_Type;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

public class DefaultClassifierFactory
{
    public static Classifier getClassifier(S_Type type)
    {
        switch (type)
        {
            case SMO: return new SMO();
            case NA: return new NaiveBayes();
            case IBK: return new IBk();
            case LOG: return new Logistic();
            case RF: return new RandomForest();
            case ADA: return new AdaBoostM1();
            default: return null;
        }
    }
}
