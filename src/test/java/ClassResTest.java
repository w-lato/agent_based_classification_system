import agh.edu.agents.enums.S_Type;
import agh.edu.learning.ClassRes;
import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.ParamsMLP;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class ClassResTest
{
    private final int N = 10;
    Instances data;
    Instances train;
    Instances test;

    @Before
    public void setup() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\spambase.arff");
        data = source.getDataSet();
        data.setClassIndex( data.numAttributes() - 1 );
        data.stratify( N );
        train = data.trainCV( N, 0 );
        test = data.testCV( N, 0 );
    }


    @Test
    public void CompareToTest() throws Exception {
        SMO smo  = new SMO();
        smo.buildClassifier( train );

        ParamsMLP p = new ParamsMLP( train );
        MLP mlp = ((MLP) p.clasFromStr(p.getParamsCartProd().get(0)));
        mlp.buildClassifier( train );

        Evaluation e1 = new Evaluation( test );
        Evaluation e2 = new Evaluation( test );
        e1.evaluateModel( smo, test);
        e2.evaluateModel( mlp, test);

        System.out.println("================== SMO ");
        System.out.println( e1.toSummaryString() );
        System.out.println("================== MLP ");
        System.out.println( e2.toSummaryString() );

        ClassRes cr1 = new ClassRes(S_Type.SMO, smo, train);
        ClassRes cr2 = new ClassRes(S_Type.MLP, mlp, train);

        Assert.assertTrue( cr1.compareTo( cr2 ) < 0 );
        Assert.assertTrue( cr1.compareTo( cr1 ) == 0 );
        Assert.assertTrue( cr2.compareTo( cr1 ) > 0 );

        System.out.println( "SMO: acc " + cr1.getAcc() + " F-1 " + cr1.getSumOfFscore() );
        System.out.println( "MLP: acc " + cr2.getAcc() + " F-1 " + cr2.getSumOfFscore() );
    }
}
