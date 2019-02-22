package agh.edu.learning.params;

import agh.edu.learning.DataSplitter;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParamsADA  implements Params
{
    @Override
    public Classifier clasFromStr(String params) {
        AdaBoostM1 ada = new AdaBoostM1();
        String[] p = params.split(",");
        ada.setWeightThreshold( Integer.valueOf( p[0] ) );
        ada.setUseResampling( Boolean.valueOf( p[1] ) );
        return ada;
    }

    @Override
    public List<String> getParamsCartProd() {
        List<String> l = new ArrayList<>();
        int[] threshs= {1,10,100,1000,10000};
        for (int i = 0; i < threshs.length; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                l.add( threshs[i] + "," + (j==1) );
            }
        }
        return l;
    }

    public static void main(String[] args) throws Exception {
        System.load("C:\\Users\\wlato\\Documents\\IdeaProjects\\IdeaProjects\\masters_thesis\\DLL\\libopenblas.dll");

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_train.arff");
        Instances instances = source.getDataSet();
        List<Instances> L = DataSplitter.splitIntoTrainAndTest(instances, 0.05);
        Instances train = L.get(0);
        Instances test = L.get(1);

        do {
            test.remove(0);
        } while (test.size() >= 1000);

        ParamsSMO paramsSMO = new ParamsSMO();
        Random r = new Random();

        for (int i = 1000; i < 30000; i += 1000)
        {
            AdaBoostM1 smo = new AdaBoostM1();
            smo.setWeightThreshold( i );
            smo.setUseResampling( false );
            smo.buildClassifier( train );
            long s = System.currentTimeMillis();
            Evaluation evaluation = new Evaluation( train );
            evaluation.crossValidateModel( smo, test, 10, new Random( System.currentTimeMillis() ) );
            System.out.println( "$$ " + (System.currentTimeMillis() - s) );
            System.out.println(evaluation.toSummaryString());
        }
    }
}
