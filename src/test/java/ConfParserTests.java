import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.enums.Split;
import agh.edu.agents.experiment.ConfParser;
import agh.edu.agents.experiment.RunConf;
import agh.edu.agents.experiment.Splitter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class ConfParserTests
{
    String mnist_conf = "CONF/default";
    static Instances train;
    static Instances test;

    @BeforeClass
    public static void setup() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\mnist_train.arff");
        train = source.getDataSet();
        source = new ConverterUtils.DataSource( "DATA\\mnist_test.arff");
        test = source.getDataSet();
    }


    @Test
    public void testConfParser()
    {
        S_Type[] arr = {
                S_Type.SMO,
                S_Type.ADA, S_Type.ADA, S_Type.ADA, S_Type.ADA,
                S_Type.NA,  S_Type.NA,  S_Type.NA,  S_Type.NA,  S_Type.NA,  S_Type.NA,
                S_Type.IBK, S_Type.IBK, S_Type.IBK, S_Type.IBK, S_Type.IBK, S_Type.IBK, S_Type.IBK,
                S_Type.LOG, S_Type.LOG, S_Type.LOG, S_Type.LOG, S_Type.LOG,
                S_Type.MLP, S_Type.MLP, S_Type.MLP,
                S_Type.RF,  S_Type.RF
        };
        RunConf rc = ConfParser.getConfFrom( mnist_conf );

        assert rc != null;
        Assert.assertArrayEquals( arr, rc.getAgents() );
        Assert.assertEquals(rc.getTest().size(), test.size());
        Assert.assertEquals(rc.getTrain().size(), train.size());
        Assert.assertEquals(0, Double.compare(rc.getFill().get(), 0.1));
        Assert.assertEquals( rc.getSplit_meth(), Split.OVERLAP );
        Assert.assertEquals( rc.getClass_method(), ClassStrat.WEIGHTED);
    }

    @Test
    public void testConfWithoutTest() throws Exception {

        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\messidor_features.arff");
        train = source.getDataSet();
        List<Instances> l = Splitter.splitOnTestAndTrain( train, 10 );
        train = l.get(0);
        test = l.get(1);

        S_Type[] arr = {
                S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,S_Type.SMO,
                S_Type.IBK, S_Type.IBK
        };
        RunConf rc = ConfParser.getConfFrom( "CONF/no_test" );

        assert rc != null;
        Assert.assertArrayEquals( arr, rc.getAgents() );
        Assert.assertEquals(rc.getTest().size(), test.size());
        Assert.assertEquals(rc.getTrain().size(), train.size());
        Assert.assertEquals(Boolean.FALSE, rc.getFill().isPresent());
        Assert.assertEquals( rc.getSplit_meth(), Split.OVERLAP );
        Assert.assertEquals( rc.getClass_method(), ClassStrat.MAJORITY);
    }

}
