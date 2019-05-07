package agh.edu.utils;

import agh.edu.aggregation.ResultsHolder;
import agh.edu.learning.params.ParamsRF;
import agh.edu.learning.params.ParamsSMO;
import org.apache.commons.math.analysis.interpolation.SmoothingBicubicSplineInterpolator;
import sun.font.FontRunIterator;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.Reorder;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SPEED_LIMITS
{
    public static Instances removeAttsWithOneVal(Instances data) throws Exception {
        Set<Integer> to_keep = new HashSet<>();
        for (int i = 0; i < data.numAttributes(); i++)
        {
            Attribute att = data.attribute( i );
            if( att.numValues() > 1 || att.isNumeric()) to_keep.add( i );
        }
        Remove removeFilter = new Remove();

        // set class att at the end
        int[] arr = new int[ to_keep.size() ];
        int i = 0;
        for( int it : to_keep ) arr[i++] = it;
        removeFilter.setAttributeIndicesArray(  arr  );
        removeFilter.setInvertSelection(true);
        removeFilter.setInputFormat( data );
        Instances filtered = Filter.useFilter(data, removeFilter);
        int class_idx = 1;
        for (int j = 0; j < filtered.numAttributes(); j++)
        {
            if ( filtered.attribute( j ).name().equals("sm_segments:maxspeed") )
            {
                class_idx = j + 1;
                break;
            }
        }

        // set max speed as the last
        Reorder reorder = new Reorder();
        String order = "first-"+ (class_idx-1)+"," + (class_idx+1) +"-last," + class_idx;
        reorder.setAttributeIndices(order);
        reorder.setInputFormat( filtered );
        return Filter.useFilter( filtered, reorder );
    }


    public static void main(String[] args) throws Exception
    {
//        List<Path> paths = Files.walk(Paths.get("EXP/TIME_SMO_8_0.1_8/AGG")).filter(x->x.getFileName().toString().contains(".res") && !x.getFileName().toString().contains("RAW_11")).collect(Collectors.toList());
//        paths.sort( Path::compareTo );
//        paths.forEach(System.out::println);
//
//        List<Path> arf_paths = Files.walk(Paths.get("EXP/TIME_SMO_8_0.1_8/AGG"))
//                .filter(x->x.getFileName().toString().contains(".arff") && !x.getFileName().toString().contains("Q_11"))
//                .sorted( Path::compareTo )
//                .peek( System.out::println )
//                .collect(Collectors.toList());
//
//        List<Instances> arf_inst = new ArrayList<>();
//        for (int i = 0; i < arf_paths.size(); i++)
//        {
//            arf_inst.add( ConverterUtils.DataSource.read( arf_paths.get(i).toAbsolutePath().toString() ) );
//            arf_inst.get( arf_inst.size() - 1 ).setClassIndex( arf_inst.get( arf_inst.size() - 1 ).numAttributes() - 1 );
//        }
//
//        String[] order = "EXP/TIME_SMO_8_0.1_8/SMO_2:EXP/TIME_SMO_8_0.1_8/SMO_1:EXP/TIME_SMO_8_0.1_8/SMO_4:EXP/TIME_SMO_8_0.1_8/SMO_3:EXP/TIME_SMO_8_0.1_8/SMO_5:EXP/TIME_SMO_8_0.1_8/SMO_6:EXP/TIME_SMO_8_0.1_8/SMO_8:EXP/TIME_SMO_8_0.1_8/SMO_7".split(":");
//        Arrays.sort(order, String::compareTo);
//        for (int i = 0; i < order.length; i++)
//        {
//            System.out.println( order[i] );
//        }
//
//        List<Instances> insta = new ArrayList<>();
//        for (int i = 0; i < paths.size(); i++)
//        {
//            List<String> file_content = Files.readAllLines( paths.get(i) );
//            ResultsHolder res = ResultsHolder.fromString( String.join( "\n",file_content) );
//            Instances res_inst = res.toStackTrainSet( order );
//            res_inst.setClassIndex( res_inst.numAttributes() - 1 );
//            Set<Integer> ctr = new HashSet<>();
//            for (int i1 = 0; i1 < res_inst.size(); i1++)
//            {
//                int class_val = Integer.valueOf( arf_inst.get(i).get(i1).classAttribute().value((int) arf_inst.get(i).get(i1).classValue()) );
////                System.out.println( class_val );
//                ctr.add(class_val );
//                res_inst.get( i1 ).setClassValue( class_val );
//            }
//
//            System.out.println( i +  "  NUM " + ctr.size() );
//            ctr.clear();
//            insta.add( res_inst );
//        }
//
//
//        Instances all = insta.get(0);
//        for (int i = 1; i < insta.size(); i++)
//        {
//            all.addAll( insta.get(i) );
//        }
//
//        Files.write( Paths.get("D:\\test_arff.txt"), all.toString().getBytes() );

        // split into train and test
//        List<String> file_content = Files.readAllLines( Paths.get("EXP\\TIME_SMO_8_0.1_8\\AGG\\Q_RAW_11.res") );
//        ResultsHolder res = ResultsHolder.fromString( String.join( "\n",file_content) );
//        Instances test = res.toStackTrainSet( order );
//        test.setClassIndex( test.numAttributes() - 1 );
//
//        Instances mnist_test = ConverterUtils.DataSource.read("DATA/mnist_test.arff");
//        mnist_test.setClassIndex( mnist_test.numAttributes() - 1 );
//        for (int i = 0; i < mnist_test.size(); i++)
//        {
//            test.get(i).setClassValue( mnist_test.get(i).classValue() );
//        }
//        Instances mnist_test = ConverterUtils.DataSource.read("DATA/mnist_test.arff");



//        Files.write( Paths.get( "D:\\PST_MALOPOLSKIE_TRAIN.arff" ),test.trainCV(10,0).toString().getBytes() );
//        Files.write( Paths.get( "D:\\PST_MALOPOLSKIE_TEST.arff" ),test.testCV(10,0).toString().getBytes() );

//        // buld and eval
//        long st = System.currentTimeMillis();
//        System.out.println( st );
////        ParamsSMO p = new ParamsSMO();
////        SMO smo = (SMO) p.clasFromStr("0,3.0,false");
//
////        System.out.println( all.size() );
//
//        Instances test = ConverterUtils.DataSource.read(   "DATA/TEST_SET_FOR_AGG_SMO_8_01.arff");
////        Instances test = ConverterUtils.DataSource.read(   "D:\\test_arff.arff");
//        test.setClassIndex( test.numAttributes() - 1 );
//        Instances train = ConverterUtils.DataSource.read(   "DATA/TRAIN_SET_FOR_AGG_SMO_8_01.arff");
////        Instances train = ConverterUtils.DataSource.read(   "D:\\train_arff.arff");
//        train.setClassIndex( train.numAttributes() - 1 );
//
////
////        ParamsRF p = new ParamsRF();
////        RandomForest smo = (RandomForest) p.clasFromStr("true,false");//new RandomForest();
//        ParamsSMO p = new ParamsSMO();
//        SMO smo = (SMO) p.clasFromStr("0,3.0,false");
//        smo.buildClassifier( train );
//
//        System.out.println( System.currentTimeMillis() - st );
//        st = System.currentTimeMillis();
//
//        Evaluation eval = new Evaluation(test);
//        eval.evaluateModel( smo, test );
//        System.out.println( eval.toSummaryString() );
//        System.out.println( System.currentTimeMillis() - st);
//        ParamsSMO paramsSMO = new ParamsSMO();
//        List<String> confs = paramsSMO.getParamsCartProd();
//
//        PART p = new PART();
//
//        for (int i = 2; i < confs.size(); i++)
//        {
//            String curr = confs.get(i);
//            System.out.println( curr );
//            SMO smo = (SMO) paramsSMO.clasFromStr( curr );
//            smo.buildClassifier( train );
//
//            System.out.println( System.currentTimeMillis() - st );
//            st = System.currentTimeMillis();
//
//            Evaluation eval = new Evaluation(test);
//            eval.evaluateModel( smo, test );
//            System.out.println( eval.toSummaryString() );
//            System.out.println( System.currentTimeMillis() - st);
//        }


        Instances test = ConverterUtils.DataSource.read(   "DATA/PST_MALOPOLSKIE_TEST.arff");


//        Instances test = ConverterUtils.DataSource.read(   "D:\\test_arff.arff");
//        Instances train = ConverterU
//        tils.DataSource.read(   "DATA/PST_MALOPOLSKIE_TRAIN.arff");
        Instances train = ConverterUtils.DataSource.read(   "D:\\FILTERED_SPEED_DATA\\FILTERED_PST-W-TCoCrN15N50A (województwo mazowieckie).arff");
        System.out.println( train.size() );
//        train = removeAttsWithOneVal( train );

        System.out.println( train.size() );


//        Files.write( Paths.get("D:\\MAZOWIECKIE_ALL_FILTERED_WITH_NUM_VAL_AND_REMOVED_ONES.arff"), train.toString().getBytes() );
//        train.addAll( train_1 );
//        train = removeAttsWithOneVal( train );
        train = ConverterUtils.DataSource.read(   "D:\\masters_thesis\\masters_thesis\\DATA\\MAZOWIECKIE_TRAIN.arff");
        System.out.println( train.numAttributes() );
        train.setClassIndex( train.numAttributes() - 1 );
        int n = 10;
        train.stratify( n );
        test = train.testCV( n,0 );
        train = train.testCV( n,1 );
//        Files.write( Paths.get("D:\\MAZOWIECKIE_TRAIN.arff"), train.toString().getBytes() );
//        Files.write( Paths.get("D:\\MAZOWIECKIE_TEST.arff"), test.toString().getBytes() );
//        Instances train = ConverterUtils.DataSource.read(   "D:\\train_arff.arff");


        System.out.println( test.numAttributes() );
        train.setClassIndex( train.numAttributes() - 1 );
        test.setClassIndex( train.numAttributes() - 1 );

//
//        ParamsRF p = new ParamsRF();
//        RandomForest smo = (RandomForest) p.clasFromStr("true,false");//new RandomForest();
//        ParamsSMO p = new ParamsSMO();
//        SMO smo = (SMO) p.clasFromStr("0,3.0,false");
        System.out.println( train.size() );
        SMO smo = new SMO();
        smo.buildClassifier( train );


        Evaluation eval = new Evaluation(test);
        eval.evaluateModel( smo, test );
        for (int i = 0; i < train.numClasses(); i++)
        {
            System.out.println( i + " :: " + eval.fMeasure( i ) );
        }
        System.out.println( eval.toSummaryString() );

    }
}
