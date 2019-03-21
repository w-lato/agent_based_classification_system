package agh.edu.learning.params;

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

        for (int thresh : threshs) {
            for (int j = 0; j < 2; j++) {
                l.add(thresh + "," + (j == 1));
            }
        }
        return l;
    }
}
