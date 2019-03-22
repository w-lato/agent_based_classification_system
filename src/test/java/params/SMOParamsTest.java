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

import java.util.List;

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

    @Test
    public void testGetFromStr()
    {
        List<String> l = p.getParamsCartProd();
        for (int i = 0; i < l.size(); i++)
        {
            String cur = l.get(i);
            String[] aux = cur.split(",");
            int kernel_type = Integer.valueOf( aux[0] );
            smo = (SMO) p.clasFromStr( cur );
            if( kernel_type == 0 )
            {
                double exp = Double.valueOf( aux[1] );
                boolean lower_order = Boolean.valueOf( aux[2] );
                PolyKernel pk = (PolyKernel) smo.getKernel();

                Assert.assertTrue( smo.getKernel() instanceof PolyKernel );
                Assert.assertEquals( exp, pk.getExponent(), 0.0 );
                Assert.assertEquals( lower_order, pk.getUseLowerOrder());
            }
            if( kernel_type == 1 )
            {
                double exp = Double.valueOf( aux[1] );
                boolean lower_order = Boolean.valueOf( aux[2] );

                NormalizedPolyKernel pk = (NormalizedPolyKernel) smo.getKernel();
                Assert.assertTrue( smo.getKernel() instanceof NormalizedPolyKernel );
                Assert.assertEquals( exp, pk.getExponent(), 0.0 );
                Assert.assertEquals( lower_order, pk.getUseLowerOrder());
            }
            if( kernel_type == 2)
            {
                double gamma = Double.valueOf( aux[1] );
                RBFKernel rbf = (RBFKernel) smo.getKernel();
                Assert.assertTrue( smo.getKernel() instanceof RBFKernel );
                Assert.assertEquals( gamma, rbf.getGamma(), 0.0 );
            }
        }
    }
}
