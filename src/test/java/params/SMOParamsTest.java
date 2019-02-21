package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsSMO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.NormalizedPolyKernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;

public class SMOParamsTest
{
    String s1;
    Params p;
    SMO smo;

    @Before
    public void setup() throws Exception {
        p = new ParamsSMO();
    }

    @Test
    public void classFromStringTest()
    {
        s1 = "0,5.5," + Boolean.TRUE;
        smo = ((SMO) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong kernel type", smo.getKernel().getClass(), PolyKernel.class);
        Assert.assertEquals( "Wrong exp. value", Double.valueOf( smo.getKernel().getOptions()[1] ), 5.5, 0.001);
        Assert.assertEquals( "Wrong exp. value", smo.getKernel().getOptions()[2] ,"-L" );

        s1 = "1,12.5," + Boolean.FALSE;
        smo = ((SMO) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong kernel type", smo.getKernel().getClass(), NormalizedPolyKernel.class);
        Assert.assertEquals( "Wrong exp. value", Double.valueOf( smo.getKernel().getOptions()[1] ), 12.5, 0.001);
        Assert.assertNotEquals( "Wrong exp. value", smo.getKernel().getOptions()[2] ,"-L" );

        s1 = "2,0.001";
        smo = ((SMO) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong kernel type", smo.getKernel().getClass(), RBFKernel.class);
        Assert.assertEquals( "Wrong exp. value", ((RBFKernel) smo.getKernel()).getGamma(), 0.001, 0.001);
    }
}
