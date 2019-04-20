package agh.edu.learning.params;

import agh.edu.learning.custom.MLP;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.*;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;

// TODO 9216 configs(!?0 * 2h
public class ParamsMLP implements Params
{

    private final long seed = System.currentTimeMillis();
    private final int classes_num;
    private final int features_num;

    public static final int ADAM     = 0;
    public static final int MOMENTUM = 1;
    public static final int ADA_GRAD = 2;
    public static final int RMS_PROP = 3;

//    public ParamsMLP(int classes_num, int features_num)
    public ParamsMLP(int classes_num, int features_num)
    {
        this.classes_num = classes_num;
        this.features_num = features_num;
    }

    public ParamsMLP(Instances data)
    {
        classes_num = data.numClasses();
        features_num = data.numAttributes() - 1;
    }

    @Override
    public Classifier clasFromStr(String params)
    {
        String[] p = params.split(",");
        MultiLayerConfiguration conf = getLayer( p );
        int num_of_iter = Integer.valueOf( p[6] );
        return new MLP( conf, num_of_iter, features_num );
    }

    @Override
    public List<String> getParamsCartProd()
    {
        Activation[] activations = {Activation.RELU, Activation.SOFTMAX, Activation.CUBE};
        OptimizationAlgorithm[] opt_algo = {OptimizationAlgorithm.CONJUGATE_GRADIENT,
                                            OptimizationAlgorithm.LBFGS,
                                            OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT};
        int[] updaters = {ADAM,MOMENTUM,ADA_GRAD,RMS_PROP};
        int[] num_of_layers = {2, 3};
        double[] learning_rates = {0.001,0.005,0.1};
        int[] hid_lay_inputs = {100, 300, 500, 1000};
        int[] num_of_iter = {5, 20, 50, 100, 500, 1000};

        List<String> l = new ArrayList<>();
        for(int updater : updaters) {
            for(double l_rate : learning_rates) {
                for(OptimizationAlgorithm opt : opt_algo) {
                    for (Activation activation : activations) {
                        for (int num_of_layer : num_of_layers) {
                            for (int hid_lay_input : hid_lay_inputs) {
                                for (int i : num_of_iter) {
                                    l.add(
                                          updater + ","
                                        + l_rate + ","
                                        + opt + ","
                                        + activation + ","
                                        + num_of_layer + ","
                                        + hid_lay_input + ","
                                        + i
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        Set<String> conflicting_confs = new HashSet<>(
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
        l = l.stream().filter( x-> {
            for (String confilcitng_conf : conflicting_confs)
            {
                if( x.startsWith( confilcitng_conf ) ) return false;
            }
            return true;
        }).collect(Collectors.toList());
        return l;
    }

    private  MultiLayerConfiguration getLayer(String[] conf)
    {
        switch (conf[ 4 ])
        {
            case "2" : return twoLay(conf);
            case "3" : return threeLay(conf);
//            case "4" : return fourLay(conf);
            default: return null;
        }
    }

    private MultiLayerConfiguration twoLay(String[] conf)
    {
        IUpdater updater = createUpdater( conf );
        OptimizationAlgorithm opt_alg = OptimizationAlgorithm.valueOf( conf[2] );
        Activation activation = Activation.valueOf( conf[3] );
        int hid_inp = Integer.valueOf(conf[5]);

        return new NeuralNetConfiguration.Builder()
                .cudnnAlgoMode(ConvolutionLayer.AlgoMode.PREFER_FASTEST)
                .seed(  seed )
                .optimizationAlgo( opt_alg )
                .updater( updater )
//                .l2(1e-4)
                .list()
                .layer(0,new DenseLayer.Builder()
                        .hasBias(true)
                        .nIn( features_num )
                        .nOut( hid_inp )
                        .activation( activation )
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(1,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn( hid_inp )
                        .nOut( classes_num )
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .pretrain(false)
                .backprop(true)
                .build();
    }

    private MultiLayerConfiguration threeLay(String[] conf)
    {
        IUpdater updater = createUpdater( conf );
        OptimizationAlgorithm opt_alg = OptimizationAlgorithm.valueOf( conf[2] );
        Activation activation = Activation.valueOf( conf[3] );
        int hid_inp = Integer.valueOf(conf[5]);

        return new NeuralNetConfiguration.Builder()
                .cudnnAlgoMode(ConvolutionLayer.AlgoMode.PREFER_FASTEST)
                .seed( seed )
                .optimizationAlgo(opt_alg)
                .updater( updater )
//                .l2(1e-4)
                .list()
                .layer(0,new DenseLayer.Builder()
                        .hasBias(true)
                        .nIn( features_num )
                        .nOut( hid_inp )
                        .activation( activation )
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(1,new DenseLayer.Builder()
                        .nIn( hid_inp )
                        .nOut( hid_inp - ((int) (0.3 * hid_inp)))
                        .activation( activation )
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(2,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn( hid_inp - ((int) (0.3 * hid_inp)) )
                        .nOut( classes_num )
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .pretrain(false)
                .backprop(true)
                .build();
    }

    private MultiLayerConfiguration fourLay(String[] conf)
    {
        IUpdater updater = createUpdater( conf );
        OptimizationAlgorithm opt_alg = OptimizationAlgorithm.valueOf( conf[2] );
        Activation activation = Activation.valueOf( conf[3] );
        int hid_inp = Integer.valueOf(conf[5]);

        return new NeuralNetConfiguration.Builder()
                .cudnnAlgoMode(ConvolutionLayer.AlgoMode.NO_WORKSPACE)
                .seed( seed )
                .optimizationAlgo(opt_alg)
                .updater( updater )
//                .l2(1e-4)
                .list()
                .layer(0,new DenseLayer.Builder()
                        .hasBias(true)
                        .nIn( features_num )
                        .nOut( hid_inp )
                        .activation( activation )
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(1,new DenseLayer.Builder()
                        .nIn( hid_inp )
                        .nOut( hid_inp - ((int) (0.3 * hid_inp)))
                        .activation( activation )
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(2,new DenseLayer.Builder()
                        .nIn( hid_inp - ((int) (0.3 * hid_inp)))
                        .nOut( hid_inp - ((int) (0.6 * hid_inp)))
                        .activation( activation )
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(3,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn( hid_inp - ((int) (0.6 * hid_inp)) )
                        .nOut( classes_num )
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .pretrain(false)
                .backprop(true)
                .build();
    }

    private IUpdater createUpdater(String[] conf)
    {
        int up_type       = Integer.valueOf( conf[0] );
        double learn_rate = Double.valueOf(  conf[1] );
        switch (up_type)
        {
            case ADAM     : return new Adam(learn_rate);
            case MOMENTUM : return new Nesterovs(learn_rate);
            case ADA_GRAD : return new AdaGrad(learn_rate);
            case RMS_PROP : return new RmsProp(learn_rate);
            default:        return null;
        }
    }
}
