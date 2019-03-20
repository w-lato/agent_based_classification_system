package agh.edu.learning.params;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.NormalizedPolyKernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ParamsSMO implements Params
{
    private final int POLY = 0;
    private final int NORM_POLY = 1;
    private final int RBF = 2;

    private String conf;

    @Override
    public List<String> getParamsCartProd() {
        List<String> l = new ArrayList<>();
        Double[] rbf_vals = {0.0001, 0.001, 0.003, 0.005, 0.007,0.009, 0.01, 0.03, 0.05,0.07, 0.09,0.1, 0.3,0.5,0.7,0.9,1.0 };

        // 3 types of kernels
        for (int i = 0; i < 3; i++)
        {
            String par = String.valueOf( i );
            if( i == POLY)
            {
                // 15 possible exp values
                for (double j = 0.0; j < 15; j+=1.0)
                {
                    // lower_order - BOOLEAN
                    for (int k = 0; k < 2; k++)
                    {
                       l.add(i + "," + j + "," + (k==1));
                    }
                }
            }
            if( i == NORM_POLY )
            {
                // 15 possible exp values
                for (double j = 0.0; j < 15; j+=1.0)
                {
                    if( Double.compare(j, 1.0) == 0 ) continue;
                    // lower_order - BOOLEAN
                    for (int k = 0; k < 2; k++)
                    {
                        l.add(i + "," + j + "," + (k==1));
                    }
                }
            }
            if( i == RBF )
            {
                // gamma values
                for (int j = 0; j < rbf_vals.length; j++)
                {
                    l.add(i + "," + rbf_vals[j]);
                }
            }
        }
        return l;
    }

    @Override
    public Classifier clasFromStr(String params)
    {
        SMO smo = new SMO();
        smo.setNumFolds(10);
        String[] parms = params.split(",");
        if(parms[0].equals("0"))
        {
            PolyKernel kernel = new PolyKernel();
            kernel.setExponent(  Double.valueOf(parms[1])  );
            kernel.setUseLowerOrder( Boolean.valueOf( parms[2] ) );
            smo.setKernel(kernel);
            return smo;
        }
        if(parms[0].equals("1"))
        {
            NormalizedPolyKernel kernel = new NormalizedPolyKernel();
            kernel.setExponent(  Double.valueOf(parms[1])  );
            kernel.setUseLowerOrder( Boolean.valueOf( parms[2] ) );
            smo.setKernel(kernel);
            return smo;
        }
        if(parms[0].equals("2"))
        {
            RBFKernel kernel = new RBFKernel();
            kernel.setGamma( Double.valueOf(parms[1]) );
            smo.setKernel( kernel );
            return smo;
        }
        return null;
    }

    public Classifier genRandomParams(Random gen) {
        conf = "";
        SMO smo = new SMO();
        smo.setNumFolds(10);
        int kerID = gen.nextInt(3);
        conf += kernelIDToStr( kerID );

        switch (kerID)
        {
            case POLY: return setupPolyKernel( smo, gen );
            case NORM_POLY: return setupNormPolyKernel( smo, gen );
            case RBF: return setupRBFKernel( smo, gen );
            default: return null;
        }
    }

    private Classifier setupPolyKernel(SMO smo, Random gen)
    {
        PolyKernel kernel = new PolyKernel();
        double exp = gen.nextDouble() * 15;
        boolean lower_order = gen.nextBoolean();
        kernel.setExponent(exp);
        kernel.setUseLowerOrder(lower_order);
        smo.setKernel(kernel);

        conf += ",lower_order:" + lower_order;
        conf += ",exp:" + exp;
        return smo;
    }
// batch size -- TODO - ilosc danych
    private Classifier setupNormPolyKernel(SMO smo, Random gen)
    {
        NormalizedPolyKernel kernel = new NormalizedPolyKernel();
        double exp = gen.nextDouble() * 15;
        while( Double.compare(exp, 1.0) == 0  ) exp = gen.nextDouble() * 15;
        boolean lower_order = gen.nextBoolean();
        kernel.setExponent( exp );
        kernel.setUseLowerOrder( lower_order );
        smo.setKernel( kernel );

        conf += ",lower_order:" + lower_order;
        conf += ",exp:" + exp;
        return smo;
    }

    private Classifier setupRBFKernel(SMO smo, Random gen)
    {
        double gamma = 0.0001 + gen.nextDouble() * (0.1 -0.0001);
        RBFKernel kernel = new RBFKernel();
        kernel.setGamma( gamma );
        smo.setKernel( kernel );
//        smo.set
        conf += ",gamma:" + gamma;
        return smo;
    }

    private String kernelIDToStr(int id)
    {
        switch (id)
        {
            case 0: return "PolyKernel";
            case 1: return "NormalizedPolykernel";
            case 2: return "RBFKernel";
            default: return "???";
        }
    }

    // TODO from conf string to classifier
    public static void main(String[] args) throws Exception
    {
//        System.load("C:\\Users\\wlato\\Documents\\IdeaProjects\\IdeaProjects\\masters_thesis\\DLL\\libopenblas.dll");

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_train.arff");
        Instances instances = source.getDataSet();
        Instances train = instances.trainCV(20,0);
        Instances test = instances.testCV(20,0);


        do {
            test.remove(0);
        } while (test.size() >= 1000);

        ParamsSMO paramsSMO = new ParamsSMO();
        Random r = new Random();

        for (int i = 0; i < 10; i++)
        {
            SMO smo = new SMO();
            smo.buildClassifier( train );
            long s = System.currentTimeMillis();
            Evaluation evaluation = new Evaluation( train );
            evaluation.evaluateModel( smo, test );
//            evaluation.crossValidateModel( smo, test, 10, new Random( System.currentTimeMillis() ) );
            System.out.println( "$$ " + (System.currentTimeMillis() - s) );
            System.out.println(evaluation.toSummaryString());
        }
    }
}
