package params;

import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsNB;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.bayes.NaiveBayes;

import javax.swing.text.StyledEditorKit;
import java.util.List;

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

    @Test
    public void testGetFromStr()
    {
        List<String> l = p.getParamsCartProd();
        for (int i = 0; i < l.size(); i++)
        {
            String cur = l.get(i);
            String[] aux = cur.split(",");
            Boolean use_kernel = Boolean.valueOf( aux[0] );
            Boolean use_discretization = Boolean.valueOf( aux[1] );
            nb = (NaiveBayes) p.clasFromStr( cur );

            Assert.assertEquals(use_kernel, nb.getUseKernelEstimator());
            Assert.assertEquals(use_discretization, nb.getUseSupervisedDiscretization());
        }
    }
}
