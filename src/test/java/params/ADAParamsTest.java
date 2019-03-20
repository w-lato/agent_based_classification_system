package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsADA;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;

import java.util.List;

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

    @Test
    public void testGetFromStr()
    {
        List<String> l = p.getParamsCartProd();
        for (int i = 0; i < l.size(); i++)
        {
            String cur = l.get(i);
            String[] aux = cur.split(",");
            Integer weight_thresh = Integer.valueOf( aux[0] );
            Boolean use_resampling = Boolean.valueOf( aux[1] );
            ada = (AdaBoostM1) p.clasFromStr( cur );

            Assert.assertEquals((int) weight_thresh, ada.getWeightThreshold());
            Assert.assertEquals(use_resampling, ada.getUseResampling());
        }
    }
}
