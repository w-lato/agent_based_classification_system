package agh.edu.learning.params;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.*;

public class ParamsLog implements Params
{
    private String conf;

    @Override
    public Classifier clasFromStr(String params)
    {
        Logistic lr = new Logistic();
        String p[] = params.split(",");

        lr.setUseConjugateGradientDescent( Boolean.valueOf(p[0]) );
        lr.setRidge( Double.valueOf(p[1]) );
        lr.setMaxIts( Integer.valueOf(p[2]) );
        return lr;
    }

    public String getConf() {
        return conf;
    }

    @Override
    public List<String> getParamsCartProd()
    {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < 2; i++)
        {
            for (double j = 0; j < 15.0; j += 0.5)
            {
                for (int k = 0; k < 16; k++)
                {
                    l.add( (i==1) + "," + j  + "," + k);
                }
            }
        }
        return l;
    }

    public static void main(String[] args) throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\wlato\\Documents\\IdeaProjects\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
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

        for (int j = 10; j < 15; j++)
        {
            for (int i = 0; i < 24; i++)
            {
                long s = System.currentTimeMillis();
                Logistic smo = new Logistic();

                boolean conGrad = i >= 12;
                smo.setUseConjugateGradientDescent( conGrad );

                if( i < 12 ) smo.setRidge( i );
                else smo.setRidge( i - 12 );
                smo.setMaxIts(j);
                smo.buildClassifier( train );
                String id = " NaiveBayes:ridge:" + i  + ",its:"+j + ",conGrad:"+conGrad + " ";
                System.out.println("BUILD: " + id + (System.currentTimeMillis() - s) );

                System.out.println( train.size() );
                s =  System.currentTimeMillis();
                for (int i1 = 0; i1 < test.size(); i1++)
                {
                    classes.add(  smo.classifyInstance( test.get(i1) )  );
                    probs.add( smo.distributionForInstance( test.get(i1) ) );
                }
//                ClassRes cr = new ClassRes(test, classes, probs);
//                System.out.println("TEST 1: " + id + (System.currentTimeMillis() - s) + " ACC: " + cr.getAcc());
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
}
