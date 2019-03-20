package params;

import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsMLP;
import org.deeplearning4j.nn.api.Layer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.rmi.server.InactiveGroupException;
import weka.classifiers.functions.Logistic;
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
        train =  instances.testCV(20,0);
        train.setClassIndex( train.numAttributes() - 1 );
        test = instances.testCV(20,1);
        test.setClassIndex( test.numAttributes() - 1 );
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

    @Test
    public void testGetFromStr()
    {
        List<String> l = p.getParamsCartProd();
        for (int i = 0; i < l.size(); i++)
        {
            String cur = l.get(i);
            String[] aux = cur.split(",");
            Integer batch_siz = Integer.valueOf( aux[0] );
            Integer num_of_iter = Integer.valueOf( aux[3] );
            mlp = (MLP) p.clasFromStr( cur );

            Assert.assertEquals((int)batch_siz, mlp.getBatch_num());
            Assert.assertEquals((int)num_of_iter, mlp.getNum_of_iter());
        }
    }
}
