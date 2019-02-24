package agh.edu.learning.params;

import agh.edu.learning.ClassRes;
import agh.edu.learning.DataSplitter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.*;

public class ParamsNB implements Params
{
    private final int true_false  = 1;
    private final int false_true  = 2;
    private final int true_true   = 3;

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
        String[] parms = params.split(",");
        naiveBayes.setUseKernelEstimator( Boolean.valueOf( parms[0] ) );
        naiveBayes.setUseSupervisedDiscretization( Boolean.valueOf( parms[1] ) );
        return naiveBayes;
    }

    public String getConf() {
        return conf;
    }

public Classifier genRandomParams(Random gen) {
        conf = "NaiveBayes:";
        NaiveBayes naiveBayes = new NaiveBayes();
        for (int i = 0; i < 4; i++)
        {
            if(!used_configs.get(i))
            {
                used_configs.put(i,true);
                boolean kerEst  = i == true_false || i == true_true;
                boolean dupDisc = i == false_true || i == true_true;

                naiveBayes.setUseKernelEstimator(kerEst);
                naiveBayes.setUseSupervisedDiscretization(dupDisc);
                conf += ",kernelEstimator:"+ kerEst + ",superDiscret:" + dupDisc;
                return naiveBayes;
            }
        }
        return null;
    }

    @Override
    public List<String> getParamsCartProd() {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                l.add( (i==1) + "," + (j==1) );
            }
        }
        return l;
    }

    public static void main(String[] args) throws Exception
    {

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
        Instances instances = source.getDataSet();
        List<Instances> L = DataSplitter.splitIntoTrainAndTest(instances, 0.05);
        Instances train = L.get(0);
        Instances test = L.get(1);

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
            ClassRes cr = new ClassRes(test, classes, probs);
            System.out.println("TEST 1: " + id + (System.currentTimeMillis() - s) + " ACC: " + cr.getAcc());
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
