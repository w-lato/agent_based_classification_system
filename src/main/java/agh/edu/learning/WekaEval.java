package agh.edu.learning;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.SMO;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.util.ArrayList;

public class WekaEval
{
//    public static final int WEKA = 0;
//    public static final int SPARK = 1;
//    public static final int OTHER = 2;
    public static final int J48  = 0;
    public static final int PART = 1;
    public static final int SMO  = 2;

    private int class_type;
    private Evaluation evaluation;
    Classifier model;

    public WekaEval(int class_type)
    {
        this.class_type = class_type;
        switch( class_type )
        {
            case J48:  model = new J48();  return;
            case PART: model = new PART(); return;
            case SMO:  model = new SMO();  return;
            default: return;
        }
    }

    public int getType() {
        return class_type;
    }

    public void setType(int type) {
        this.class_type = type;
    }

    public void setModel(Classifier model) {
        this.model = model;
    }

    public void train( Instances trainingSet )
    {
        try {
            if( evaluation != null )
            {
                evaluation.setPriors( trainingSet );
            } else {
                evaluation = new Evaluation(trainingSet);
            }
           model.buildClassifier( trainingSet );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Prediction> eval(Instances test)
    {
//        System.out.println( evaluation );
//        System.out.println( model );
        try {
            evaluation.evaluateModel( model, test );
            return evaluation.predictions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
