package agents;

import agh.edu.agents.experiment.Splitter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SplitterTest
{
    static int N = 50;
    static Instances data;

    @BeforeClass
    public static void setup() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\mnist_train.arff");
        data = source.getDataSet();
        data.randomize( new Random(1));
        data.setClassIndex( data.numAttributes() - 1);
        data.stratify( N );
    }

    @Test
    public void testEqualSplit() throws IOException {
        int n = 10;
        List<Instances> l = Splitter.equalSplit( data, n );
        for (int i = 1; i < n; i++)
        {
            List<String> s1 = Arrays.stream(l.get(i - 1).toString().split("\n")).filter(x -> !(x.startsWith("@") || x.isEmpty())).collect(Collectors.toList());
            List<String> s2 = Arrays.stream(l.get(i).toString().split("\n")).filter(x -> !(x.startsWith("@") || x.isEmpty())).collect(Collectors.toList());
            s2.retainAll( s1 );
            Assert.assertTrue( s2.isEmpty()  );
        }
    }

    @Test
    public void testOLSplit() throws IOException
    {
        int n = 10;
        double[] OL = {0.01,0.02,0.03,0.05,0.1,0.15,0.2,0.3,0.4,0.5,0.6,0.7};

        for (double v : OL)
        {
            System.out.println( v );
            List<Instances> l = Splitter.OLsplit( data, v, n );
            for (int i = 0; i < l.size(); i++)
            {
                Assert.assertEquals(l.get(i).size(), (data.size() * v), 10.0);
            }

            // check whether each batch is unique (within list)
            for (int i = 1; i < n; i++)
            {
                // count overlapping instances
                List<String> s1 = Arrays.stream(l.get(i - 1).toString().split("\n")).filter(x -> !(x.startsWith("@") || x.isEmpty())).collect(Collectors.toList());
                List<String> s2 = Arrays.stream(l.get(i).toString().split("\n")).filter(x -> !(x.startsWith("@") || x.isEmpty())).collect(Collectors.toList());
                int orig_siz = s2.size();
                s2.retainAll(s1);

//                System.out.println( orig_siz + " : " + s2.size() );
                Assert.assertTrue(orig_siz > 0);
                Assert.assertTrue(orig_siz >= (v * s2.size()));
            }
        }
    }


}
