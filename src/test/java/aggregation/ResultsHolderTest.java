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
        l1.add( new double[]{11.0,0.0,0.0} );
        l1.add( new double[]{21.0,0.0,0.0} );

        List l2 = new ArrayList();
        l2.add( new double[]{0.0,1.0,0.0} );
        l2.add( new double[]{0.0,81.0,0.0} );
        l2.add( new double[]{0.0,1.0,0.0} );

        List l3 = new ArrayList();
        l3.add( new double[]{0.0,0.50,91.0} );
        l3.add( new double[]{0.0,0.50,1.0} );
        l3.add( new double[]{0.0,0.50,1.0} );

        m.put("M_1", l1);
        m.put("M_2", l2);
        m.put("M_3", l3);

        Integer id = 0;
        ResultsHolder rh = new ResultsHolder(id,m);
        String s = rh.toString();
        String should_be = "#0\n" + "M_1:M_2:M_3\n" +
                "[1.0, 0.0, 0.0]:[0.0, 1.0, 0.0]:[0.0, 0.5, 91.0]\n" +
                "[11.0, 0.0, 0.0]:[0.0, 81.0, 0.0]:[0.0, 0.5, 1.0]\n" +
                "[21.0, 0.0, 0.0]:[0.0, 1.0, 0.0]:[0.0, 0.5, 1.0]";

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

        // add new model
        List l4 = new ArrayList();
        l4.add( new double[]{9.0,6.50,3.0} );
        l4.add( new double[]{8.0,5.50,2.0} );
        l4.add( new double[]{7.0,4.50,1.0} );
        m.put("M_4",l4);
        ResultsHolder updated = new ResultsHolder(1,m);
        s = updated.toString();
        should_be = "#1\n" + "M_1:M_2:M_3:M_4\n" +
                "[1.0, 0.0, 0.0]:[0.0, 1.0, 0.0]:[0.0, 0.5, 91.0]:[9.0, 6.5, 3.0]\n" +
                "[11.0, 0.0, 0.0]:[0.0, 81.0, 0.0]:[0.0, 0.5, 1.0]:[8.0, 5.5, 2.0]\n" +
                "[21.0, 0.0, 0.0]:[0.0, 1.0, 0.0]:[0.0, 0.5, 1.0]:[7.0, 4.5, 1.0]";
        Assert.assertEquals( should_be,s );
    }
}
