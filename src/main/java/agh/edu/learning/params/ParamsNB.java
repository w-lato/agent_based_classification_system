package agh.edu.learning.params;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.*;

public class ParamsNB implements Params
{
    String conf;
    Map<Integer, Boolean> used_configs;

    public ParamsNB()
    {
        used_configs = new HashMap<>();
        for (int i = 0; i < 4; i++)
        {
            used_configs.put(i, false);
        }
    }


    @Override
    public Classifier clasFromStr(String params)
    {
        NaiveBayes naiveBayes = new NaiveBayes();
        String[] p = params.split(",");
        naiveBayes.setUseKernelEstimator( Boolean.valueOf( p[0] ) );
        naiveBayes.setUseSupervisedDiscretization( Boolean.valueOf( p[1] ) );
        return naiveBayes;
    }

    public String getConf() {
        return conf;
    }


    // true,true is impossible - settings are conflicting with each other
    // false,false is default
    @Override
    public List<String> getParamsCartProd() {
        List<String> l = new ArrayList<>();
        l.add("false,true");
        l.add("true,false");
        return l;
    }

    public static void main(String[] args) throws Exception
    {

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
        Instances instances = source.getDataSet();

        Instances train = instances.trainCV(20,0);
        Instances test = instances.testCV(20,0);

        while (true)
        {
            test.remove(0);
            if( test.size() < 1000 ) break;
        }
        List<Double> classes = new ArrayList();
        List<double[]> probs = new ArrayList();
        for (int i = 0; i < 4; i++)
        {
            long s = System.currentTimeMillis();
            NaiveBayes smo = new NaiveBayes();
            if( i == 0 )
            {
                smo.setUseKernelEstimator(false);
                smo.setUseSupervisedDiscretization(false);
            } else if( i==1) {
                smo.setUseKernelEstimator(true);
                smo.setUseSupervisedDiscretization(false);
            } else if( i == 2) {
                smo.setUseKernelEstimator(true);
                smo.setUseSupervisedDiscretization(false);
            } else {
                smo.setUseKernelEstimator(true);
                smo.setUseSupervisedDiscretization(true);
            }
            smo.buildClassifier( train );
            String id = " NaiveBayes: " + i  + " ";
            System.out.println("BUILD: " + id + (System.currentTimeMillis() - s) );


            System.out.println( train.size() );
            s =  System.currentTimeMillis();
            for (int i1 = 0; i1 < test.size(); i1++)
            {
                classes.add(  smo.classifyInstance( test.get(i1) )  );
                probs.add( smo.distributionForInstance( test.get(i1) ) );
            }
//            ClassRes cr = new ClassRes(test, classes, probs);
//            System.out.println("TEST 1: " + id + (System.currentTimeMillis() - s) + " ACC: " + cr.getAcc());
            classes.clear();
            probs.clear();

            s = System.currentTimeMillis();
            Evaluation evaluation = new Evaluation(train);
            evaluation.evaluateModel(smo, test);
            System.out.println( "$$ " + (System.currentTimeMillis() - s) );
            System.out.println(evaluation.toSummaryString());
        }

    }
}
