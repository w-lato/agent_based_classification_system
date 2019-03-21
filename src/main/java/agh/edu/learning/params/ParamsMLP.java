package agh.edu.learning.params;

import agh.edu.learning.custom.MLP;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

// TODO ask wheter it is  correct way of using batch size?
public class ParamsMLP implements Params
{
    private final int classes_num;
    private final int features_num;

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
        int batch_num = Integer.valueOf( p[0] );
        int num_of_iter = Integer.valueOf( p[3] );
        return new MLP( conf, num_of_iter, batch_num );
    }

    @Override
    public List<String> getParamsCartProd()
    {
        int[] batch_sizes = {50, 150, 500, 1000, 1500, 5000};
        int[] num_of_layers = {2, 3, 4};
        int[] hid_lay_inputs = {100, 300, 500, 1000, 1500};
        int[] num_of_iter = {100, 300, 500, 1000, 1500, 3000};

        List<String> l = new ArrayList<>();
        for (int batch_size : batch_sizes) {
            for (int num_of_layer : num_of_layers) {
                for (int hid_lay_input : hid_lay_inputs) {
                    for (int i : num_of_iter) {
                        l.add(
                                batch_size + ","
                                        + num_of_layer + ","
                                        + hid_lay_input + ","
                                        + i
                        );
                    }
                }
            }
        }
        return l;
    }

    private  MultiLayerConfiguration getLayer(String[] conf)
    {
        switch (conf[1])
        {
            case "2" : return twoLay(conf);
            case "3" : return threeLay(conf);
            case "4" : return fourLay(conf);
            default: return null;
        }
    }

    private MultiLayerConfiguration twoLay(String[] conf)
    {
        int hid_inp = Integer.valueOf(conf[2]);

        return new NeuralNetConfiguration.Builder()
                .seed(  System.currentTimeMillis() )
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .seed( System.currentTimeMillis() )
                .l2(1e-4)
                .list()
                .layer(0,new DenseLayer.Builder()
                        .hasBias(true)
                        .nIn( features_num )
                        .nOut( hid_inp )
                        .activation(Activation.RELU)
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
        int hid_inp = Integer.valueOf(conf[2]);
        return new NeuralNetConfiguration.Builder()
                .seed(  System.currentTimeMillis() )
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .seed( System.currentTimeMillis() )
                .l2(1e-4)
                .list()
                .layer(0,new DenseLayer.Builder()
                        .hasBias(true)
                        .nIn( features_num )
                        .nOut( hid_inp )
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(1,new DenseLayer.Builder()
                        .nIn( hid_inp )
                        .nOut( hid_inp - ((int) (0.3 * hid_inp)))
                        .activation(Activation.SOFTMAX)
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
        int hid_inp = Integer.valueOf(conf[2]);
        return new NeuralNetConfiguration.Builder()
                .seed(  System.currentTimeMillis() )
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .seed( System.currentTimeMillis() )
                .l2(1e-4)
                .list()
                .layer(0,new DenseLayer.Builder()
                        .hasBias(true)
                        .nIn( features_num )
                        .nOut( hid_inp )
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(1,new DenseLayer.Builder()
                        .nIn( hid_inp )
                        .nOut( hid_inp - ((int) (0.3 * hid_inp)))
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(2,new DenseLayer.Builder()
                        .nIn( hid_inp - ((int) (0.3 * hid_inp)))
                        .nOut( hid_inp - ((int) (0.6 * hid_inp)))
                        .activation(Activation.SOFTMAX)
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

}
