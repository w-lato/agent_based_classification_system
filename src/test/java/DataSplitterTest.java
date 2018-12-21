import agh.edu.learning.DataSplitter;
import org.junit.Before;
import org.junit.Test;
import sun.font.FontRunIterator;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class DataSplitterTest {

    ConverterUtils.DataSource source;
    Instances rows;

    @Before
    public void setup() throws Exception {
        source = new ConverterUtils.DataSource( "DATA\\spambase.arff");
        rows = source.getDataSet();
    }

    @Test
    public void simpleSplitTrainAndTest() throws Exception
    {
        List<Instances> l = DataSplitter.splitIntoTrainAndTest( rows, 0.7 );
        assertTrue("Wrong size of train data", l.get(0).size() > (rows.size() * 0.69)  );
        assertTrue("Wrong size of train data", l.get(1).size() > (rows.size() * 0.29)  );
    }

    @Test
    public void equalSplitTest()
    {
        List<Instances> l = DataSplitter.split( rows,10 , DataSplitter.SIMPLE_DIVIDE, 0.0 );

        assertEquals("Data not divided into 10 agents", 10, l.size());
        for (int i = 0; i < l.size(); i++)
        {
            assertEquals(460, l.get(i).size());
        }

        l = DataSplitter.split( rows,4 , DataSplitter.SIMPLE_DIVIDE, 0.0 );
        assertEquals("Data not divided into 10 agents", 4, l.size());
        for (int i = 0; i < l.size(); i++)
        {
            assertEquals(1150, l.get(i).size());
        }
    }

    @Test
    public void overlapDivideTest()
    {
        double OL = 0.1;
        List<Instances> l = DataSplitter.split( rows,10 , DataSplitter.OVERLAP_DIVIDE, OL );
        assertEquals("Data not divided into 10 agents", 10, l.size());

        for (int i = 0; i < l.size() - 1; i++)
        {
            List<String> A = l.get(i).stream().map(Objects::toString).collect(Collectors.toList());
            List<String> B = l.get(i + 1).stream().map(Objects::toString).collect(Collectors.toList());
            Set<String> AA = new HashSet<>(A);

            int ctr = 0;
            for (int i1 = 0; i1 < B.size(); i1++)
            {
                if( AA.contains( B.get(i1) ) ) ctr++;
            }
            System.out.println( A.size() * OL + " " + ctr );
            assertTrue( ctr >= Math.floor(A.size() * OL) );
        }
    }
}
