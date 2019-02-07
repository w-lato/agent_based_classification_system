package agh.edu.learning.params;

import agh.edu.learning.ClassRes;
import agh.edu.learning.DataSplitter;
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

    String conf;

    @Override
    public String getConf() {
        return conf;
    }

    @Override
    public Classifier clasFromStr(String params) {
        return null;
    }

    @Override
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
}
