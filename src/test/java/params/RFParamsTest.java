package params;

import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsRF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.trees.RandomForest;

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
}
