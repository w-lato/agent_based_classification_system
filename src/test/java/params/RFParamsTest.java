package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsRF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.RandomForest;

import java.util.List;

/**
 * weight of enighbours  (when K > 1) ??
 * num of neighbours ??
 *
 *
 */
public class RFParamsTest
{
    String s1;
    Params p;
    RandomForest rf;

    @Before
    public void setup() throws Exception {
        p = new ParamsRF();
    }

    @Test
    public void classFromStringTest()
    {
        s1 = Boolean.FALSE +"," + Boolean.TRUE;
        rf = ((RandomForest) p.clasFromStr(s1));
        Assert.assertEquals( "Wrong BrkTies", rf.getBreakTiesRandomly() , Boolean.FALSE);
        Assert.assertEquals( "Wrong Att importance", rf.getComputeAttributeImportance() , Boolean.TRUE);
    }

    @Test
    public void testGetFromStr()
    {
        List<String> l = p.getParamsCartProd();
        for (int i = 0; i < l.size(); i++)
        {
            String cur = l.get(i);
            String[] aux = cur.split(",");
            Boolean break_ties = Boolean.valueOf( aux[0] );
            Boolean compute_attribute = Boolean.valueOf( aux[1] );
            rf = (RandomForest) p.clasFromStr( cur );

            Assert.assertEquals(break_ties, rf.getBreakTiesRandomly());
            Assert.assertEquals(compute_attribute, rf.getComputeAttributeImportance());
        }
    }
}
