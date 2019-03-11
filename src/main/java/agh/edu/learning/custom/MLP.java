package agh.edu.learning.custom;

import agh.edu.learning.DataSplitter;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

// TODO check batch size - where to put it
// TODO check if it is possible to add a layer to existing config
public class MLP extends MultiLayerNetwork implements Classifier, Serializable
{
    private int num_of_iter = 100;
    private int batch_num = 5;

    public int getBatch_num() { return batch_num; }
    public int getNum_of_iter() {
        return num_of_iter;
    }

    MLP(MultiLayerConfiguration conf, int num_of_iter)
    {
        super(conf);
        this.init();
        this.num_of_iter = num_of_iter;
    }

    public MLP(MultiLayerConfiguration conf, int num_of_iter, int batch_num)
    {
        this(conf, num_of_iter);
        this.batch_num = batch_num;
    }

    @Override
    public void buildClassifier(Instances data) throws Exception
    {
        double[] arr = new double[data.numClasses()];
        Arrays.fill( arr,0 );
        arr[((int) data.get(0).classValue())] = 1;
        INDArray qwe = Nd4j.create( arr );
        for (int i = 1; i < data.size(); i++)
        {
            Arrays.fill(arr,0);
            arr[((int) data.get(i).classValue())] = 1;
            qwe = Nd4j.concat( 0, qwe, Nd4j.create( arr ) );
        }
        INDArray xd = data.stream()
                .map( Instance::toDoubleArray )
                .map(x -> Arrays.copyOf(x, x.length - 1))
                .map(Nd4j::create)
                .reduce( (a,b) -> Nd4j.concat(0,a,b))
                .orElse(null);

        List<DataSet> l = new DataSet( xd, qwe ).dataSetBatches( batch_num );
        for (int i = 0; i < num_of_iter; i++)
        {
            for (DataSet dataSet : l) { this.fit(dataSet); }
        }
    }

    @Override
    public double classifyInstance(Instance instance) throws Exception
    {
        int N = instance.numAttributes() - 1;
        double[] src = instance.toDoubleArray();
        double[] arg = new double[ N ];

        System.arraycopy(src,0,arg,0,N);
        INDArray row = Nd4j.create( arg );

        row = output( row );
        int max_indx =-1;
        double max = -1;
        for (int i = 0; i < instance.numClasses(); i++)
        {
            if( max < row.getDouble( i ) )
            {
               max =  row.getDouble( i );
               max_indx = i;
            }
        }
        return max_indx;
    }

    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        int N = instance.numAttributes() - 1;
        double[] src = instance.toDoubleArray();
        double[] arg = new double[ N ];

        System.arraycopy(src,0,arg,0,N);
        INDArray row = Nd4j.create( arg );
        return output( row ).toDoubleVector();
    }

    @Override
    public Capabilities getCapabilities() {
        return null;
    }


    public static void main(String[] args) throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_train.arff");
        Instances instances = source.getDataSet();
        List<Instances> L = DataSplitter.splitIntoTrainAndTest(instances, 0.1);
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
                        .nOut(300) // Number of output datapoints.
                        .activation(Activation.RELU) // Activation function.
                        .weightInit(WeightInit.NORMAL) // Weight initialization.
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(300)
                        .nOut( 200 )
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(200)
                        .nOut( 10 )
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.NORMAL)
                        .build())
                .pretrain(false)
                .backprop(true)
                .build();
        MLP mlp = new MLP( conf, 1500);
        mlp.buildClassifier( train );

        double acc = 0.0;
        for (int i = 0; i < 1000; i++)
        {
            double curr =  mlp.classifyInstance( test.get( i ) );
            if( Double.compare( curr, test.get( i ).classValue() ) == 0 )
            {
                acc++;
            }
        }
        System.out.println("Acc: " + (acc/1000));
    }
}
