package params;

import agh.edu.learning.DataSplitter;
import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsMLP;
import org.deeplearning4j.nn.api.Layer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.List;

public class MLPParamsTest
{
    Instances train;
    Instances test;

    String s1;
    Params p;
    MLP mlp;

    @Before
    public void setup() throws Exception {
        p = new ParamsMLP(10,28*28);

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_train.arff");
        Instances instances = source.getDataSet();
        List<Instances> L = DataSplitter.splitIntoTrainAndTest(instances, 0.01);
        train = L.get(0);
        test = L.get(1);
    }

    @Test
    public void classFromStringTest()
    {
        s1 = p.getParamsCartProd().get(0);
        mlp = (MLP) p.clasFromStr( s1 );
        Layer l0 = mlp.getLayer(0);
        Layer l1 = mlp.getLayer(1);

        Assert.assertNotNull("Layer 0 is null", l0);
        Assert.assertNotNull("Layer 1 is null", l1);

        s1 = p.getParamsCartProd().get(31);
        mlp = (MLP) p.clasFromStr( s1 );
        l0 = mlp.getLayer(0);
        l1 = mlp.getLayer(1);
        Layer l2 = mlp.getLayer(2);

        Assert.assertNotNull("Layer 0 is null", l0);
        Assert.assertNotNull("Layer 1 is null", l1);
        Assert.assertNotNull("Layer 2 is null", l2);

    }


    @Test
    public void classifierBuildTest() throws Exception {
        // 3 layers test
        mlp = ((MLP) p.clasFromStr("50,4,100,100"));
        mlp.buildClassifier( train );
    }
}
