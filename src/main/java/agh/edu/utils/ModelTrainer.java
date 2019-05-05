package agh.edu.utils;

import agh.edu.agents.enums.S_Type;
import agh.edu.aggregation.ClassGrade;
import agh.edu.learning.ClassRes;
import agh.edu.learning.custom.MLP;
import agh.edu.learning.params.ParamsFactory;
import agh.edu.learning.params.ParamsLog;
import agh.edu.learning.params.ParamsMLP;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ModelTrainer
{

    public static void gradeTheModel(Classifier model, Instances data) throws Exception {
        Evaluation eval = new Evaluation(data);
        eval.evaluateModel( model, data );
        System.out.println( "----" );
        double grade = 0.0;
        double acc = eval.correct() / eval.predictions().size();
        System.out.println( acc );

        grade += 0.5 * acc;
        double f1 = 0.0;
        for (int i = 0; i < 10; i++)
        {
            f1 += eval.fMeasure(i);
            System.out.print( eval.fMeasure ( i )+ ", " );
        }
        grade += f1 * 0.5;
        System.out.println("----");
        for (int i = 0; i < 10; i++)
        {
            System.out.print( eval.areaUnderROC( i )+ ", " );
        }
        System.out.println(" GRADE : " + grade);
        System.out.println( eval.toSummaryString() );
        ClassRes cr = new ClassRes( S_Type.SMO, model, data );

        ClassGrade cg =  new ClassGrade( cr, "/SMO_100_DEFAULT_35.conf" );
        System.out.println( cg.toString() );
    }

    public static void main(String[] args) throws Exception {
        String p =  "D:\\MNIST_MODELS\\TRAIN_100";
        String data_path = "DATA\\mnist_train.arff";
        System.out.println(Files.exists(Paths.get(p)));


        Instances data = ConverterUtils.DataSource.read( data_path );
        data.setClassIndex( data.numAttributes() - 1 );

        ParamsMLP params = new ParamsMLP(data);
        String conf = params.getParamsCartProd().get(0);
        long start = System.currentTimeMillis();
        System.out.println( "============================ " + conf );
        Classifier c = params.clasFromStr(conf);

        S_Type model_type = S_Type.MLP;
        c.buildClassifier( data );
        start = System.currentTimeMillis() - start;
        SerializationHelper.write( p + "/" + model_type +"_100_" + conf + "_" + (start/60000) +".model", c );
        gradeTheModel( c, data );

//        data = data.testCV( 10,0 );
//        Classifier model = (Classifier) SerializationHelper.read(  p + "/SMO_100_DEFAULT_35.model" );
//        Classifier model = (Classifier) SerializationHelper.read(  p + "/MLP_100_1000,2,300,1000_111.model" );
//        Classifier model = (Classifier) SerializationHelper.read(  p + "/ADA_100_100,true_0.model" );
//        Classifier model = (Classifier) SerializationHelper.read(  p + "/RF_100_true,true_1.model" );
//        Classifier model = (Classifier) SerializationHelper.read(  p + "/NA_100_true,false_0.model" );

//        Evaluation eval = new Evaluation(data);
//        eval.evaluateModel( model, data );
//        System.out.println( "----" );
//        double grade = 0.0;
//        double acc = eval.correct() / eval.predictions().size();
//        System.out.println( acc );
//
//        grade += 0.5 * acc;
//        double f1 = 0.0;
//        for (int i = 0; i < 10; i++)
//        {
//            f1 += eval.fMeasure(i);
//            System.out.print( eval.fMeasure ( i )+ ", " );
//        }
//        grade += f1 * 0.5;
//        System.out.println("----");
//        for (int i = 0; i < 10; i++)
//        {
//            System.out.print( eval.areaUnderROC( i )+ ", " );
//        }
//        System.out.println(" GRADE : " + grade);
//        System.out.println( eval.toSummaryString() );
//        ClassRes cr = new ClassRes( S_Type.SMO, model, data );

//        ClassGrade cg =  new ClassGrade( cr, "/SMO_100_DEFAULT_35.conf" );
//        System.out.println( cg.toString() );

//
//        S_Type model_type = S_Type.LOG;
//        ParamsLog params = (ParamsLog) ParamsFactory.getParams( model_type );
//        List<String> confs = params.getParamsCartProd();
//
//        for (String conf : confs)
//        {
//            long start = System.currentTimeMillis();
//
//
////        Classifier c = DefaultClassifierFactory.getClassifier( model_type );
////        ParamsMLP params = new ParamsMLP( data.numClasses(), data.numAttributes()-1 );
////        String conf = "1000,2,300,1000";
////        String conf = "true,false";
////            String conf = "true,10.0,10";
//            System.out.println( "============================ " + conf );
//            Classifier c = params.clasFromStr(conf);
//
//            c.buildClassifier( data );
//            start = System.currentTimeMillis() - start;
//            //SerializationHelper.write( p + "/" + model_type +"_100_" + conf + "_" + (start/60000) +".model", c );
//            gradeTheModel( c, data );
//        }


    }
}
