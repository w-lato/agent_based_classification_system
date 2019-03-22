package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsLog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.Logistic;

import java.util.List;

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

    @Test
    public void testGetFromStr()
    {
        List<String> l = p.getParamsCartProd();
        for (int i = 0; i < l.size(); i++)
        {
            String cur = l.get(i);
            String[] aux = cur.split(",");
            Boolean use_conj = Boolean.valueOf( aux[0] );
            Double ridge = Double.valueOf( aux[1] );
            Integer max_ints = Integer.valueOf( aux[2] );

            lr = (Logistic) p.clasFromStr( cur );

            Assert.assertEquals(use_conj, lr.getUseConjugateGradientDescent());
            Assert.assertEquals(ridge, lr.getRidge(), 0.001);
            Assert.assertEquals((int)max_ints, lr.getMaxIts());
        }
    }
}
