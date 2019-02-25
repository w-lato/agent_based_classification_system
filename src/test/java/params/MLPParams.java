package params;

import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsMLP;
import org.deeplearning4j.nn.api.Layer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MLPParams
{
    String s1;
    Params p;
    MLP mlp;

    @Before
    public void setup() throws Exception {
        p = new ParamsMLP(10,28*28);
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

}
