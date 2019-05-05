package agh.edu.utils;

import agh.edu.learning.params.ParamsSMO;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

public class TRAIN_SMO_SEQUENTIALLY
{
    public static Instances getXPerc(Instances data, int perc)
    {
        int hundred = 100;
        data.stratify(hundred);
        Instances to_return = data.testCV(hundred,0);
        if( perc == 1 ) return to_return;
        else {
            for (int i = 1; i < perc; i++)
            {
                to_return.addAll( data.testCV(hundred, i) );
            }
            return to_return;
        }
    }

    public static void main(String[] args) throws Exception
    {
        Instances data = ConverterUtils.DataSource.read("DATA/mnist_train.arff");
        Instances test = ConverterUtils.DataSource.read("DATA/mnist_test.arff");
        data.setClassIndex( data.numAttributes() - 1 );
        test.setClassIndex( data.numAttributes() - 1 );

//        int[] percs = new int[]{1, 2, 5, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        int[] percs = new int[]{90, 100};
        String[] confs = new String[]{
                "1,2.0,false",
                "1,2.0,true",
                "1,3.0,true",
                "1,4.0,false",
                "1,4.0,true",
                "1,5.0,false",
                "1,5.0,true",
        };

        ParamsSMO p = new ParamsSMO();
        for (int i = 0; i < percs.length; i++)
        {
            for (int i1 = 0; i1 < confs.length; i1++)
            {
                String curr_conf = confs[i1];
                int curr_perc = percs[i];
                Instances to_train = getXPerc( data, curr_perc);
                SMO smo  = (SMO) p.clasFromStr( curr_conf );

                long st = System.currentTimeMillis();
                smo.buildClassifier( to_train );
                System.out.println(curr_conf + " TRAINED AFTER:  " + percs[i] + " : " + (System.currentTimeMillis() - st)  );
                // SAVE MODEL
                SerializationHelper.write("D:\\MNIST_MODELS\\SMO_TRAINIGS_SEQ\\"+curr_perc+"_"+curr_conf, smo );

//                // TEST
//                st = System.currentTimeMillis();
//                Evaluation evaluation = new Evaluation( test );
//                evaluation.evaluateModel(smo, test);
//                System.out.println(curr_conf + " TESTED AFTER:  " + (System.currentTimeMillis() - st)  );
//                System.out.println( evaluation.toSummaryString());
//                System.out.println("==================");
            }
        }
    }
}
