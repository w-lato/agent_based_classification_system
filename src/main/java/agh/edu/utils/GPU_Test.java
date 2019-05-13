package agh.edu.utils;

import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.ParamsMLP;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
//import org.nd4j.jita.conf.CudaEnvironment;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GPU_Test
{
//    private static final Logger log = LoggerFactory.getLogger(MultiGpuLenetMnistExample.class);
//
//    public static void main(String[] args) throws Exception {
//        // PLEASE NOTE: For CUDA FP16 precision support is available
//        Nd4j.setDataType(DataBuffer.Type.HALF);
//
//        // temp workaround for backend initialization
//
//        CudaEnvironment.getInstance().getConfiguration()
//                // key option enabled
//                .allowMultiGPU(true)
//
//                // we're allowing larger memory caches
//                .setMaximumDeviceCache(2L * 1024L * 1024L * 1024L)
//
//                // cross-device access is used for faster model averaging over pcie
//                .allowCrossDeviceAccess(true);
//
//        int nChannels = 1;
//        int outputNum = 10;
//
//        // for GPU you usually want to have higher batchSize
//        int batchSize = 128;
//        int nEpochs = 10;
//        int seed = 123;
//
//        log.info("Load data....");
//        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize,true,12345);
//        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize,false,12345);
//
//        log.info("Build model....");
//        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
//                .seed(seed)
//                .l2(0.0005)
//                .weightInit(WeightInit.XAVIER)
//                .updater(new Nesterovs.Builder().learningRate(.01).build())
//                .biasUpdater(new Nesterovs.Builder().learningRate(0.02).build())
//                .list()
//                .layer(0, new ConvolutionLayer.Builder(5, 5)
//                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
//                        .nIn(nChannels)
//                        .stride(1, 1)
//                        .nOut(20)
//                        .activation(Activation.IDENTITY)
//                        .build())
//                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
//                        .kernelSize(2,2)
//                        .stride(2,2)
//                        .build())
//                .layer(2, new ConvolutionLayer.Builder(5, 5)
//                        //Note that nIn need not be specified in later layers
//                        .stride(1, 1)
//                        .nOut(50)
//                        .activation(Activation.IDENTITY)
//                        .build())
//                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
//                        .kernelSize(2,2)
//                        .stride(2,2)
//                        .build())
//                .layer(4, new DenseLayer.Builder().activation(Activation.RELU)
//                        .nOut(500).build())
//                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
//                        .nOut(outputNum)
//                        .activation(Activation.SOFTMAX)
//                        .build())
//                .setInputType(InputType.convolutionalFlat(28,28,1)) //See note below
//                .backprop(true).pretrain(false).build();
//        MultiLayerNetwork model = new MultiLayerNetwork(conf);
//        model.init();
//
////        // ParallelWrapper will take care of load balancing between GPUs.
////        ParallelWrapper wrapper = new ParallelWrapper.Builder(model)
////                // DataSets prefetching options. Set this value with respect to number of actual devices
////                .prefetchBuffer(24)
////
////                // set number of workers equal to number of available devices. x1-x2 are good values to start with
////                .workers(2)
////
////                // rare averaging improves performance, but might reduce model accuracy
////                .averagingFrequency(3)
////
////                // if set to TRUE, on every averaging model score will be reported
////                .reportScoreAfterAveraging(true)
////
////                .build();
//
//        log.info("Train model....");
//        model.setListeners(new ScoreIterationListener(100));
//        long timeX = System.currentTimeMillis();
//
//        // optionally you might want to use MultipleEpochsIterator instead of manually iterating/resetting over your iterator
//        //MultipleEpochsIterator mnistMultiEpochIterator = new MultipleEpochsIterator(nEpochs, mnistTrain);
//
//        for( int i=0; i<nEpochs; i++ ) {
//            long time1 = System.currentTimeMillis();
//
//            // Please note: we're feeding ParallelWrapper with iterator, not model directly
////            wrapper.fit(mnistMultiEpochIterator);
//            wrapper.fit(mnistTrain);
//            long time2 = System.currentTimeMillis();
//            log.info("*** Completed epoch {}, time: {} ***", i, (time2 - time1));
//        }
//        long timeY = System.currentTimeMillis();
//
//        log.info("*** Training complete, time: {} ***", (timeY - timeX));
//
//        log.info("Evaluate model....");
//        Evaluation eval = new Evaluation(outputNum);
//        while(mnistTest.hasNext()){
//            DataSet ds = mnistTest.next();
//            INDArray output = model.output(ds.getFeatures(), false);
//            eval.eval(ds.getLabels(), output);
//        }
//        log.info(eval.stats());
//        mnistTest.reset();
//
//        log.info("****************Example finished********************");
//    }


    public static void main(String[] args) throws Exception {
        Instances TEST_RAW = ConverterUtils.DataSource.read("DATA/mnist_test.arff");
        TEST_RAW.setClassIndex( TEST_RAW.numAttributes() - 1 );


        Instances TRAIN_RAW = ConverterUtils.DataSource.read("DATA/mnist_train.arff");
        TRAIN_RAW.setClassIndex( TRAIN_RAW.numAttributes() - 1 );


        System.out.println("====================== RAW ");
        ParamsMLP params = new ParamsMLP( TRAIN_RAW );

        List<String> confs = params.getParamsCartProd();
        TRAIN_RAW.stratify(100);
        TRAIN_RAW = TRAIN_RAW.testCV(100,0);

        TEST_RAW.stratify(20);
        TEST_RAW = TEST_RAW.testCV(20,0);
        System.out.println("TRAIN SIZE: " + TRAIN_RAW.size());
        System.out.println("TEST SIZE: " + TEST_RAW.size());
        System.out.println( confs.size() );

        String[] arr = new String[]{
                "3,0.1,LBFGS,CUBE,3,500,20",
                "3,0.1,LBFGS,CUBE,3,500,50",
                "3,0.1,LBFGS,CUBE,3,500,100",
                "3,0.1,LBFGS,CUBE,2,500,5",
                "3,0.1,LBFGS,CUBE,2,1000,5",
                "3,0.1,LBFGS,CUBE,3,500,500",
                "3,0.1,LBFGS,CUBE,2,1000,500",
                "3,0.1,LBFGS,CUBE,2,500,100",
                "3,0.1,LBFGS,CUBE,2,500,500",
                "3,0.1,LBFGS,CUBE,2,1000,100",
                "3,0.1,LBFGS,CUBE,2,1000,1000",
                "3,0.1,LBFGS,CUBE,2,1000,20",
                "3,0.1,LBFGS,CUBE,2,1000,50",
                "3,0.1,LBFGS,CUBE,3,500,1000",
                "3,0.1,LBFGS,CUBE,2,500,50",
                "3,0.1,LBFGS,CUBE,2,500,1000",
                "3,0.1,LBFGS,CUBE,2,500,20"
        };
//        List<String> cpu_confs_to_do = Arrays.asList( arr );
//
        arr = new String[]{
                "0,0.1,LBFGS,CUBE,2,1000,100",
                "3,0.1,LBFGS,CUBE,2,1000,5",
                "3,0.1,LBFGS,CUBE,2,1000,500",
                "0,0.1,LBFGS,CUBE,2,1000,20",
                "0,0.1,LBFGS,CUBE,2,1000,500",
                "3,0.1,LBFGS,CUBE,2,1000,100",
                "3,0.1,LBFGS,CUBE,2,1000,1000",
                "2,0.1,LBFGS,CUBE,2,1000,50",
                "3,0.1,LBFGS,CUBE,2,1000,20",
                "3,0.1,LBFGS,CUBE,2,1000,50",
                "0,0.1,LBFGS,CUBE,2,1000,50",
                "2,0.1,LBFGS,CUBE,2,1000,5",
                "2,0.1,LBFGS,CUBE,2,1000,20",
                "0,0.1,LBFGS,CUBE,2,1000,5",
                "0,0.1,LBFGS,CUBE,2,1000,1000",
                "2,0.1,LBFGS,CUBE,2,1000,500",
                "2,0.1,LBFGS,CUBE,2,1000,100",
                "2,0.1,LBFGS,CUBE,2,1000,1000"
        };
//
        List<String> GPU_confs_to_do = Arrays.asList( arr );
        confs = GPU_confs_to_do;
        for (int i = 0; i < confs.size(); i++)
        {
            try {
                MLP raw = (MLP) params.clasFromStr( confs.get( i ) );

                long s = System.currentTimeMillis();
                raw.buildClassifier( TRAIN_RAW );

                System.out.println(i + " : " + confs.get(i) + " : "  + (System.currentTimeMillis() - s) );

                weka.classifiers.Evaluation eval_raw = new Evaluation( TEST_RAW );
                eval_raw.evaluateModel( raw, TEST_RAW );
                System.out.println( eval_raw.toSummaryString() );
                System.out.println(" === ");
            } catch(Exception ex )
            {
                ex.printStackTrace();
            }
        }



    }
}
