package agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.agents.experiment.Loader;
import agh.edu.agents.experiment.Saver;
import agh.edu.learning.ClassRes;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LoaderTest
{
    private static Instances data;

    @BeforeClass
    public static void setup() throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\mnist_train.arff");
        data = source.getDataSet();
        data.setClassIndex( data.numAttributes() - 1);
        int n = 20;
        data.stratify(n);
        data = source.getDataSet().testCV(n,0);
        data.setClassIndex( data.numAttributes() - 1);
    }


    @Test
    public void testLoader() throws Exception
    {
        S_Type type = S_Type.SMO;
        SMO smo = new SMO();
        smo.buildClassifier( data );
        ClassRes cr = new ClassRes( type, smo, data );
        Map<String,String> m = new HashMap<>();
        m.put("A","9123");
        m.put("B","0001");
        m.put("C","5670");
        m.put("D","2000");

        String exp_dir = Saver.setupNewExp("TEST_DIR") + "/some_id";
        Saver.saveModel( exp_dir, smo, cr, type, data, m );

        // test
        Path A = Paths.get( exp_dir + ".conf" );
        S_Type loaded_type = Loader.getType( A );
        Map<String,String> loaded_map = Loader.getConfigs( A );
        Assert.assertEquals( type, loaded_type );
        m.forEach((k,v) -> { Assert.assertEquals( loaded_map.get( k ), v ); });

        // test loaded model
        SMO loaded_smo = (SMO) Loader.getModel( exp_dir + ".model" );
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( exp_dir+".arff");
        Instances read_instances = source.getDataSet();
        read_instances.setClassIndex( read_instances.numAttributes() - 1 );
        ClassRes read_cr = new ClassRes( loaded_type, loaded_smo, read_instances );
        Assert.assertEquals(0, cr.compareTo(read_cr));

        // clean
        SaverTest.cleanWorkspace();
    }
}
