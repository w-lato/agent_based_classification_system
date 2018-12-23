import agh.edu.learning.DataSplitter;
import agh.edu.learning.WekaEval;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class WekaEvalTest {
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
        List<Instances> s = DataSplitter.splitIntoTrainAndTest( rows, 0.7 );
        List<Instances> d = DataSplitter.split( s.get(0),10 , DataSplitter.OVERLAP_DIVIDE, 0.1 );
        List<WekaEval> l = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            l.add( new WekaEval(WekaEval.SMO) );
            l.get(i).train( d.get(i) );
        }


        // EVALUATE WITH TESTS DATA
        List<List<Prediction>> res = new ArrayList<>();
        for (int i = 0; i < l.size(); i++)
        {
            res.add( l.get(i).eval( s.get(1) ) );
        }

        for (int i = 0; i < res.size(); i++)
        {
            int ctr = 0;
            for (int i1 = 0; i1 < res.get(i).size(); i1++)
            {
                 if( res.get(i).get(i1).actual() == res.get(i).get(i1).predicted() )ctr++;
            }
            assertTrue( (100 * ctr / res.get(i).size() ) > 60 );
        }
    }
}
