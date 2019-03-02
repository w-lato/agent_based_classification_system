package agh.edu.learning.custom;

import agh.edu.learning.DataSplitter;
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
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MLP extends MultiLayerNetwork implements Classifier
{
    private int batch_siz;
    private int num_of_iter;

    private Path tmp_path;
    private RecordReader rr;
    private DataSetIterator dsi;


    public MLP(MultiLayerConfiguration conf, int batch_siz, int num_of_iter)
    {
        super(conf);
        this.init();
        tmp_path = Paths.get("TMP/" + String.valueOf(System.nanoTime()) + ".csv");
        rr = new CSVRecordReader();
        this.batch_siz = batch_siz;
        this.num_of_iter = num_of_iter;
    }


    @Override
    public void buildClassifier(Instances data) throws Exception
    {
        String s = Arrays.stream(data.toString().split("\n"))
                .filter(x -> !(x.startsWith("@") || x.isEmpty()))
                .collect( Collectors.joining( "\n" ) );

//        System.out.println( s );
        Files.write(tmp_path, s.getBytes());

        int labelIndex = data.numAttributes();     //5 values in each row of the iris.txt CSV: 4 input features followed by an integer label (class) index. Labels are the 5th value (index 4) in each row
        int numClasses = data.numClasses();     //3 classes (types of iris flowers) in the iris data set. Classes have integer values 0, 1 or 2

        rr.initialize( new FileSplit(tmp_path.toFile()));
        dsi = new RecordReaderDataSetIterator(rr,batch_siz,labelIndex-1,numClasses);
//        dsi.getLabels().forEach( System.out::println );

        DataSet allData = dsi.next();
        for (int i = 0; i < num_of_iter; i++)
        {
            this.fit( allData );
        }
//        Files.write(tmp_path , "".getBytes());
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
        MLP mlp = new MLP( conf, 1500, 500);
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



//        mlp.setConf( conf );
    }
}
