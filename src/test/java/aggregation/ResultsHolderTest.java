package aggregation;

import agh.edu.aggregation.ResultsHolder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ResultsHolderTest
{
    @Test
    public void testToAndFromString()
    {
        LinkedHashMap<String, List<double[]>> m = new LinkedHashMap<>();
        List l1 = new ArrayList();
        l1.add( new double[]{1.0,0.0,0.0} );
        l1.add( new double[]{1.0,0.0,0.0} );
        l1.add( new double[]{1.0,0.0,0.0} );

        List l2 = new ArrayList();
        l2.add( new double[]{0.0,1.0,0.0} );
        l2.add( new double[]{0.0,1.0,0.0} );
        l2.add( new double[]{0.0,1.0,0.0} );

        List l3 = new ArrayList();
        l3.add( new double[]{0.0,0.50,1.0} );
        l3.add( new double[]{0.0,0.50,1.0} );
        l3.add( new double[]{0.0,0.50,1.0} );

        m.put("M_1", l1);
        m.put("M_2", l2);
        m.put("M_3", l3);

        Integer id = 0;
        ResultsHolder rh = new ResultsHolder(id,m);
        String s = rh.toString();
        String should_be = "#0\n" + "M_1:M_2:M_3\n" +
                "[1.0, 0.0, 0.0]:[0.0, 1.0, 0.0]:[0.0, 0.5, 1.0]\n" +
                "[1.0, 0.0, 0.0]:[0.0, 1.0, 0.0]:[0.0, 0.5, 1.0]\n" +
                "[1.0, 0.0, 0.0]:[0.0, 1.0, 0.0]:[0.0, 0.5, 1.0]";

        Assert.assertEquals( should_be, s );

        ResultsHolder from_str = ResultsHolder.fromString( s );
        Assert.assertEquals( rh.getID(), from_str.getID()  );
        Assert.assertEquals( rh.toString(), from_str.toString()  );
        from_str.getProbs().entrySet().forEach(  x ->{
            for (int i = 0; i < x.getValue().size(); i++)
            {
            Assert.assertArrayEquals( rh.getProbs().get( x.getKey() ).get(i), x.getValue().get(i),0.0001 );
            }
        });
    }
}
