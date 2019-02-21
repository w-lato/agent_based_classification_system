package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsLog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.Logistic;

public class LogParamsTest
{
    String s1;
    Params p;
    Logistic lr;

    @Before
    public void setup() throws Exception {
        p = new ParamsLog();
    }

    @Test
    public void classFromStringTest()
    {
        s1 = Boolean.FALSE +",9.0,5";
        lr = ((Logistic) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong kernel type", lr.getUseConjugateGradientDescent(), Boolean.FALSE);
        Assert.assertEquals( "Wrong kernel type", lr.getRidge(), 9.0,0.01);
        Assert.assertEquals( "Wrong kernel type", lr.getMaxIts(), 5);
    }
}
