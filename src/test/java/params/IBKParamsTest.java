package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsIBk;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.lazy.IBk;

public class IBKParamsTest
{
    String s1;
    Params p;
    IBk ibk;

    @Before
    public void setup() throws Exception {
        p = new ParamsIBk();
    }

    @Test
    public void classFromStringTest()
    {
        s1 = p.getParamsCartProd().get(1);
        ibk = ((IBk) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong kernel type", 1,  ibk.getWindowSize());
        Assert.assertEquals( "Wrong kernel type", ibk.getKNN(), 1);
        Assert.assertEquals( "Wrong kernel type", ibk.getMeanSquared(), Boolean.TRUE);
    }
}
