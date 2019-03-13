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
    public void testOLSplit() throws IOException {
        int n = 10;
        double[] OL = {0.05,0.1,0.15,0.2,0.4,0.5};

        for (double v : OL) {
            List<Instances> l = Splitter.OLsplit( data, v, n );
            for (int i = 1; i < n; i++) {
                List<String> s1 = Arrays.stream(l.get(i - 1).toString().split("\n")).filter(x -> !(x.startsWith("@") || x.isEmpty())).collect(Collectors.toList());
                List<String> s2 = Arrays.stream(l.get(i).toString().split("\n")).filter(x -> !(x.startsWith("@") || x.isEmpty())).collect(Collectors.toList());
                int orig_siz = s2.size();
                s2.retainAll(s1);
//            System.out.println( orig_siz + " : " + s2.size() );
                Assert.assertTrue(orig_siz > 0);
                Assert.assertTrue(orig_siz >= (v * s2.size()));
            }
        }
    }

    @Test
    //todo fractions like 0.15 does not work like it should
    public void testFillSplit() throws IOException {
        int n = 3;
        double[] OL = {0.5};

        for (double v : OL) {
            List<Instances> l = Splitter.fillSplit( data, n, v );
            for (int i = 1; i < n; i++) {
                List<String> s1 = Arrays.stream(l.get(i - 1).toString().split("\n")).filter(x -> !(x.startsWith("@") || x.isEmpty())).collect(Collectors.toList());
                List<String> s2 = Arrays.stream(l.get(i).toString().split("\n")).filter(x -> !(x.startsWith("@") || x.isEmpty())).collect(Collectors.toList());
                int orig_siz = s2.size();
                s2.retainAll(s1);
            System.out.println( orig_siz + " : " + s2.size() + " " + l.get(i).size());
                Assert.assertTrue(orig_siz > 0);
                Assert.assertTrue(orig_siz > (s2.size()));
                int siz = l.get(i).size();
//                Assert.assertTrue( siz > (data.size() * v - 100) &&  siz < (data.size() * v + 100) );
            }
        }
    }

}
