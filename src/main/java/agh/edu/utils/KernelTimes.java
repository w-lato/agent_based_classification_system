package agh.edu.utils;

import agh.edu.learning.ClassRes;
import agh.edu.learning.DataSplitter;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.NormalizedPolyKernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;

public class KernelTimes
{
    public static void main(String[] args) throws Exception
    {
        List<Kernel> kernels = new ArrayList<>();
        for (int i = 0; i < 15; i++)
        {
            Kernel k = new PolyKernel();
            ((PolyKernel) k).setExponent( i );
            kernels.add( k );
        }
        for (int i = 0; i < 15; i++)
        {
            Kernel k = new NormalizedPolyKernel();
            ((PolyKernel) k).setExponent( i );
            kernels.add( k );
        }
        double[] arr = {0.0001,0.0005,0.001,0.005,0.01,0.1,1.0};
        for (int i = 0; i < arr.length; i++) {
            Kernel k = new RBFKernel();
            ((RBFKernel) k).setGamma(arr[i]);
            kernels.add( k );
        }

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
        Instances instances = source.getDataSet();
        List<Instances> L = DataSplitter.splitIntoTrainAndTest(instances, 0.07);
        Instances train = L.get(0);

        source = new ConverterUtils.DataSource("C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_test.arff");
        Instances test = source.getDataSet();
        test.setClassIndex( test.numAttributes() - 1 );
        while (true)
        {
            test.remove(0);
            if( test.size() == 1000 )break;
        }

        List<Double> classes = new ArrayList();
        List<double[]> probs = new ArrayList();
        for (int i = 0; i < 15; i++)
        {
            long s = System.currentTimeMillis();
            SMO smo = new SMO();
            smo.setKernel( kernels.get(i) );
            smo.buildClassifier( train );
            String id = " PolyKernel: " + i  + " ";
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
        for (int i = 15; i < 30; i++)
        {
            long s = System.currentTimeMillis();
            SMO smo = new SMO();
            smo.setKernel( kernels.get(i) );
            smo.buildClassifier( train );
            String id = " NormPolyKernel: " + (i-15)  + " ";
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
        for (int i = 30; i < 30 + arr.length; i++)
        {
            long s = System.currentTimeMillis();
            SMO smo = new SMO();
            smo.setKernel( kernels.get(i) );
            smo.buildClassifier( train );
            String id = " RBF: " + arr[i-30]  + " ";
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
