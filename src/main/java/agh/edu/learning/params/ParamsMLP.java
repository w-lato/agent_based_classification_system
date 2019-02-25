package agh.edu.learning.params;

import agh.edu.learning.DataSplitter;
import agh.edu.learning.custom.MLP;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO when to initialize classes_num and features_num
 *
 * opt_algo
 * num_of_layers
 * num_of_inputs
 * num_of_iterations
 * batch_size
 */
public class ParamsMLP implements Params
{
    private final int classes_num;
    private final int features_num;

    public ParamsMLP(int classes_num, int features_num)
    {
        this.classes_num = classes_num;
        this.features_num = features_num;
    }

    @Override
    public Classifier clasFromStr(String params)
    {
        String[] p = params.split(",");
        MultiLayerConfiguration conf = getLayer( p );
        int batch_siz = Integer.valueOf( p[0] );
        int num_of_iter = Integer.valueOf( p[3] );
        MLP mlp = new MLP( conf, batch_siz, num_of_iter );
        return mlp;
    }

    @Override
    public List<String> getParamsCartProd()
    {
        int[] batch_sizes = {50, 150, 500, 1000, 1500, 5000};
        int[] num_of_layers = {2, 3, 4, 5};
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
                .layer(1,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
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
                .layer(1,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn( hid_inp )
                        .nOut( hid_inp - ((int) (0.3 * hid_inp)))
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(2,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
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

    public static void main(String[] args) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_train.arff");
        Instances instances = source.getDataSet();
        List<Instances> L = DataSplitter.splitIntoTrainAndTest(instances, 0.05);
        Instances train = L.get(0);
        Instances test = L.get(1);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(  System.currentTimeMillis() )
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .l2(1e-4)
                .list()
                .layer(new DenseLayer.Builder()
                        .hasBias(true)
                        .nIn(28 * 28) // Number of input datapoints.
                        .nOut(150) // Number of output datapoints.
                        .activation(Activation.RELU) // Activation function.
                        .weightInit(WeightInit.ONES) // Weight initialization.
                        .build())
//                .layer(new DenseLayer.Builder()
//                        .nIn(1000) // Number of input datapoints.
//                        .nOut(500) // Number of output datapoints.
//                        .activation(Activation.RELU) // Activation function.
//                        .weightInit(WeightInit.XAVIER) // Weight initialization.
//                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(150)
                        .nOut( 10 )
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.ONES)
                        .build())
                .pretrain(false)
                .backprop(true)
                .build();

        // create the MLN
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();


        RecordReader recordReader = new CSVRecordReader(789,",");
//        RecordReader recordReader = new CSVRecordReader();
        recordReader.initialize(new FileSplit(new File("DATA/mnist_train.arff")));
//        recordReader.initialize(new FileSplit(new File("TMP/88295047705900.csv")));

        //Second: the RecordReaderDataSetIterator handles conversion to DataSet objects, ready for use in neural network
        int labelIndex = 784;     //5 values in each row of the iris.txt CSV: 4 input features followed by an integer label (class) index. Labels are the 5th value (index 4) in each row
        int numClasses = 10;     //3 classes (types of iris flowers) in the iris data set. Classes have integer values 0, 1 or 2
        int batchSize = 1500;    //Iris data set: 150 examples total. We are loading all of them into one DataSet (not recommended for large data sets)

        DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader,batchSize,labelIndex,numClasses);
        DataSet allData = iterator.next();
        allData.shuffle();
        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);

        network.addListeners(new ScoreIterationListener( 100 ));

//        network.fit( testAndTrain.getTrain() );
        for(int i=0; i<1000; i++ ) {
//            network.fit(testAndTrain.getTrain());

        }
// using an evaluation class
        System.out.println( testAndTrain.getTest().asList().size() );

        DataSetIterator test_iterator = new RecordReaderDataSetIterator(recordReader,batchSize,labelIndex,numClasses);
        DataSet test_data = iterator.next();

        recordReader.initialize(new FileSplit(new File("DATA/mnist_test.arff")));
        DataSet ds = new DataSet();
        Evaluation eval = new Evaluation(10); //create an evaluation object with 10 possible classes
        for (DataSet dataSet : test_data) { //testAndTrain.getTest()) {
            INDArray output = network.output(dataSet.getFeatures()); //get the networks prediction
            eval.eval(dataSet.getLabels(), output);
            System.out.println( eval.accuracy() );
//            System.out.println( dataSet. );
        }


//        DataSetIterator dsi = new DoublesDataSetIterator( testAndTrain.getTest() );
//        network.evaluate( testAndTrain.getTest() );
    }
}
