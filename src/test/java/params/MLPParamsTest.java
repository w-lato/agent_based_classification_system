package params;

import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.Params;
import agh.edu.learning.params.ParamsMLP;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.activations.Activation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


// TODO conf which produces -1 class when used to classification:
//  1,0.001,STOCHASTIC_GRADIENT_DESCENT,CUBE,2,500,20
//  99 : 3,0.1,CONJUGATE_GRADIENT,RELU,2,500,20 : 0
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

        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_test.arff");
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

        for( String it : p.getParamsCartProd() )
        {
            String num_of_layers =  it.split(",")[4];
            if( num_of_layers.equals("3") )
            {
                s1 = it;
                break;
            }
        }
        mlp = (MLP) p.clasFromStr( s1 );
        l0 = mlp.getLayer(0);
        l1 = mlp.getLayer(1);
        Layer l2 = mlp.getLayer(2);

        Assert.assertNotNull("Layer 0 is null", l0);
        Assert.assertNotNull("Layer 1 is null", l1);
        Assert.assertNotNull("Layer 2 is null", l2);
    }


    @Test
    public void
    classifierBuildTest() throws Exception {
        // 2 layers test

        Activation[] activations = {Activation.RELU, Activation.SOFTMAX, Activation.CUBE};
        OptimizationAlgorithm[] opt_algo = {OptimizationAlgorithm.CONJUGATE_GRADIENT,OptimizationAlgorithm.LBFGS,
                OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT};
        int[] updaters = {ParamsMLP.ADAM,ParamsMLP.MOMENTUM,ParamsMLP.ADA_GRAD,ParamsMLP.RMS_PROP};
        double[] learning_rates = {0.001,0.005,0.1};

        List<String> s = new ArrayList<>();
        for (double learning_rate : learning_rates) {
            for (Activation activation : activations) {
                for (OptimizationAlgorithm optAlgo : opt_algo) {
                    for (int updater : updaters) {
                        s.add(
                                updater +"," +
                                learning_rate + "," +
                                optAlgo + "," +
                                activation + "," +
                                "2,500,20" );
                    }
                }
            }
        }
        Set<String> confilcitng_confs = new HashSet<>(
                Arrays.asList(
                        "1,0.1,STOCHASTIC_GRADIENT_DESCENT,CUBE",
                        "0,0.1,LBFGS,SOFTMAX",
                        "3,0.1,LBFGS,SOFTMAX",
                        "1,0.005,STOCHASTIC_GRADIENT_DESCENT,CUBE",
                        "3,0.1,LBFGS,SOFTMAX",
                        "0,0.1,LBFGS,RELU",
                        "1,0.001,STOCHASTIC_GRADIENT_DESCENT,CUBE",
                        "0,0.001,CONJUGATE_GRADIENT,CUBE",
                        "0,0.005,CONJUGATE_GRADIENT,CUBE",
                        "0,0.001,LBFGS,CUBE",
                        "0,0.005,LBFGS,CUBE",
                        "0,0.1,CONJUGATE_GRADIENT,CUBE"
                )
        );
        s = s.stream().filter( x-> {
            for (String confilcitng_conf : confilcitng_confs)
            {
                if( x.startsWith( confilcitng_conf ) ) return false;
            }
            return true;
        }).collect(Collectors.toList());

        Set<Double> possible_classes = IntStream.range(0,train.numClasses()).mapToObj(Double::valueOf).collect(Collectors.toSet());
        List<Double> l = new ArrayList<>();
        Set<String> not_working = new HashSet<>();
//        for (int i = 526; i < s.size(); i++)
        for (int i = 0; i < s.size(); i++)
        {
            try
            {
                l.clear();

                mlp = ((MLP) p.clasFromStr(s.get(i)));
                mlp.buildClassifier( train );

                train.forEach( x->{
                    try {
                        l.add( mlp.classifyInstance( x ) );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                long countr = l.stream()
//                    .peek(System.out::println)
                        .filter( x-> !possible_classes.contains(x) ).count();
                System.out.println( i + " : " + s.get(i) + " : " + countr );
                if( countr > 0 ) not_working.add( s.get(i) +",");
                Assert.assertEquals(0, countr);
            }
            catch (Exception e)
            {
//                System.out.println( "FAILED: " + i );
                not_working.add( s.get(i) +",");
            }
        }
        System.out.println("---------------------------------------");
        not_working.forEach(System.out::println);
    }

    @Test
    public void testGetFromStr()
    {
        List<String> l = p.getParamsCartProd();
        for (int i = 0; i < l.size(); i++)
        {
            String cur = l.get(i);
            System.out.println( i + " %%%% " + cur);
            String[] aux = cur.split(",");
            Integer batch_siz = train.numAttributes() - 1;
            Integer num_of_iter = Integer.valueOf( aux[6] );

            mlp = (MLP) p.clasFromStr( cur );

            Assert.assertEquals((int)batch_siz, mlp.getBatch_size());
            Assert.assertEquals((int)num_of_iter, mlp.getNum_of_iter());
        }
    }
}
