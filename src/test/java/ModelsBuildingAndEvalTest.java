import agh.edu.agents.enums.S_Type;
import agh.edu.learning.ClassRes;
import agh.edu.learning.ParamsFactory;
import agh.edu.learning.params.Params;
import org.junit.Assert;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelsBuildingAndEvalTest
{
    private int N;
    Instances data;
    Instances train;
    Instances test;
    S_Type[] typeSet;
    Set<Double> possible_class_outcomes;


    public void setup(String path) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( path );
        data = source.getDataSet();
        if( data.size() > 30000 ) N = 50;
        else  N = 10;
        data.setClassIndex( data.numAttributes() - 1 );
        data.stratify( N );
        train = data.testCV( N, 0 );
        test = data.testCV( N, 1 );

        printClassesNum( train );
        printClassesNum( test );

        typeSet = new S_Type[]{S_Type.MLP, S_Type.ADA, S_Type.IBK, S_Type.LOG, S_Type.NA, S_Type.RF, S_Type.SMO };
//        typeSet = new S_Type[]{S_Type.SMO,  S_Type.LOG};
        possible_class_outcomes = new HashSet<>();
        for (double i = 0.0; i < data.numClasses(); i+=1.0) possible_class_outcomes.add( i );
    }

    private void printClassesNum(Instances d)
    {
        System.out.println("SIZE: "+ d.size());
        for (int i = 0; i < d.numClasses(); i++) {
            int cl = i;
            int ctr = (int) d.stream().filter(x -> x.classValue() == cl).count();
            System.out.print( i + " : " +  ctr + ", " );
        }
        System.out.println();
    }

    public void buildTests(String path) throws Exception {
        setup( path );
        for (S_Type t : typeSet)
        {
            Params p = ParamsFactory.getParams( t, data );
            List<String> l = p.getParamsCartProd();

            Classifier c = p.clasFromStr( l.get( 0 ) );
            c.buildClassifier( train );

            double res = c.classifyInstance( test.get( 0 ) );
            double[] res_prob = c.distributionForInstance( test.get( 0 ) );
            Assert.assertTrue( possible_class_outcomes.contains( res ) );
            for (double v1 : res_prob)
            {
                Assert.assertTrue(Double.compare(v1, 1.0) <= 1);
                Assert.assertTrue(Double.compare(v1, 0.0) >= 0);
            }

            ClassRes cr = new ClassRes(t,c,test);
            double acc = cr.getAcc();
            Assert.assertTrue( acc <= 100 );
            Assert.assertTrue( acc >= 0 );

            double[] f1 = cr.getFscore();
            for (double v : f1)
            {
                Assert.assertTrue( Double.compare( v, 1.0 ) <= 1 );
                Assert.assertTrue( Double.compare( v, 0.0 ) >= 0 );
            }
            System.out.println( t + " : acc " + acc  +" f1 " + cr.getSumOfFscore());
        }
    }

    @Test
    public void checkSomeDataSets() throws Exception {
        System.out.println("SPAMBASE");
        System.out.println("=================================================");
        buildTests( "DATA\\spambase.arff" );

        System.out.println("MNIST_TRAIN");
        System.out.println("=================================================");
        buildTests( "DATA\\mnist_train.arff" );

        System.out.println("MESS. FEAT");
        System.out.println("=================================================");
        buildTests( "DATA\\messidor_features.arff" );
    }

}
