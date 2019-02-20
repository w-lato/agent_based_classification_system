import agh.edu.learning.ClassRes;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class ClassResTest
{

    ClassRes res;

    @Before
    public void setup() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\spambase.arff");
        Instances rows = source.getDataSet();
    }


    @Test
    public void checkConfMat()
    {}


}
