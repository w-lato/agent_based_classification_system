package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsIBk;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.classifiers.lazy.IBk;

import java.util.List;

public class IBKParamsTest
{
    String s1;
    static Params p;
    IBk ibk;

    @BeforeClass
    public static void setup() throws Exception {
        p = new ParamsIBk();
    }

    @Test
    public void classFromStringTest()
    {
        s1 = p.getParamsCartProd().get(1);
        ibk = ((IBk) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong kernel type", 100,  ibk.getWindowSize());
        Assert.assertEquals( "Wrong kernel type", ibk.getKNN(), 1);
        Assert.assertEquals( "Wrong kernel type", ibk.getMeanSquared(), Boolean.TRUE);
    }

    @Test
    public void testGetFromStr()
    {
        List<String> l = p.getParamsCartProd();
        for (int i = 0; i < l.size(); i++)
        {
            String cur = l.get(i);
            System.out.println( cur );
            String[] aux = cur.split(",");
            Integer window_siz = Integer.valueOf( aux[0] );
            Integer knn = Integer.valueOf( aux[1] );
            Boolean isMeanSquared = Boolean.valueOf( aux[2] );
            ibk = (IBk) p.clasFromStr( cur );

            Assert.assertEquals((int) window_siz, ibk.getWindowSize());
            Assert.assertEquals((int) knn, ibk.getKNN());
            Assert.assertEquals( isMeanSquared, ibk.getMeanSquared() );
        }
    }
}
