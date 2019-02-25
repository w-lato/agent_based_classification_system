package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsADA;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.meta.AdaBoostM1;

public class ADAParamsTest
{
    String s1;
    Params p;
    AdaBoostM1 ada;

    @Before
    public void setup() throws Exception
    {
        p = new ParamsADA();
    }

    @Test
    public void classFromStringTest()
    {
        s1 = p.getParamsCartProd().get(1);
        ada = ((AdaBoostM1) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong kernel type", 1,  ada.getWeightThreshold());
        Assert.assertEquals( "Wrong kernel type", ada.getUseResampling(), Boolean.TRUE);

    }
}
