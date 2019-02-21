package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsNB;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.bayes.NaiveBayes;

public class NBParamsTest
{
    String s1;
    Params p;
    NaiveBayes nb;

    @Before
    public void setup() throws Exception {
        p = new ParamsNB();
    }

    @Test
    public void classFromStringTest()
    {
        s1 = Boolean.FALSE +"," + Boolean.TRUE;
        nb = ((NaiveBayes) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong kernel type", nb.getUseKernelEstimator(), Boolean.FALSE);
        Assert.assertEquals( "Wrong kernel type", nb.getUseSupervisedDiscretization(), Boolean.TRUE);

        s1 = Boolean.TRUE +"," + Boolean.FALSE;
        nb = ((NaiveBayes) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong kernel type", nb.getUseKernelEstimator(), Boolean.TRUE);
        Assert.assertEquals( "Wrong kernel type", nb.getUseSupervisedDiscretization(), Boolean.FALSE);

    }
}
