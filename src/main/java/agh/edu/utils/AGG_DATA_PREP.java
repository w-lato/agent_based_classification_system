package agh.edu.utils;

import agh.edu.aggregation.ResultsHolder;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AGG_DATA_PREP
{
    public static Instances prep_data_for_agg(String path_to_probs, String path_to_arff, String[] order) throws Exception
    {
        Instances data = ConverterUtils.DataSource.read(path_to_arff);
        data.setClassIndex( data.numAttributes() -1 );

        Arrays.sort(order, String::compareTo);
        List<String> file_content = Files.readAllLines( Paths.get(path_to_probs) );
        ResultsHolder res = ResultsHolder.fromString( String.join( "\n",file_content) );
        Instances res_inst = res.toMaxStackSet( order );
        res_inst.setClassIndex( res_inst.numAttributes() - 1 );

        for (int i = 0; i < res_inst.size(); i++) {
//            System.out.println( res_inst.get(i) );

//            int class_val = Integer.valueOf( data.get(i).classAttribute().value((int) data.get(i).classValue()) );
            int class_val = (int) data.get(i).classValue();
            res_inst.get( i ).setClassValue( class_val );
//            System.out.println( class_val );
        }
        return res_inst;
    }

    public static void main(String[] args) throws Exception {
        String[] order = "EXP/MAZOWIECKIE_8_SMO_28/SMO_1:EXP/MAZOWIECKIE_8_SMO_28/SMO_2:EXP/MAZOWIECKIE_8_SMO_28/SMO_3:EXP/MAZOWIECKIE_8_SMO_28/SMO_4:EXP/MAZOWIECKIE_8_SMO_28/SMO_7:EXP/MAZOWIECKIE_8_SMO_28/SMO_5:EXP/MAZOWIECKIE_8_SMO_28/SMO_6:EXP/MAZOWIECKIE_8_SMO_28/SMO_8".split(":");

        String path_to_train_probs = "EXP/MAZOWIECKIE_8_SMO_28/AGG/Q_RAW_1.res";
        String path_to_test_probs = "EXP/MAZOWIECKIE_8_SMO_28/AGG/Q_RAW_2.res";

        String path_to_train_data = "DATA/MAZOWIECKIE_TRAIN.arff";
        String path_to_test_data = "DATA/MAZOWIECKIE_TEST.arff";

        Instances train = prep_data_for_agg( path_to_train_probs, path_to_train_data, order );
        train.setClassIndex(train.numAttributes() -1);

        Instances test = prep_data_for_agg( path_to_test_probs, path_to_test_data, order );
        test.setClassIndex(test.numAttributes() -1);

        System.out.println( train.numAttributes() );
        System.out.println( train.size());
        Files.write( Paths.get("D:\\AGG_MAZO_8_SMO_TRAIN.arf"), train.toString().getBytes() );
        Files.write( Paths.get("D:\\AGG_MAZO_8_SMO_TEST.arf"), test.toString().getBytes() );

        RandomForest smo = new RandomForest();
        smo.buildClassifier( train );

        Evaluation e = new Evaluation( test );
        e.evaluateModel(smo, test);

        for (int i = 0; i < test.numClasses(); i++) {
            System.out.println( i + " : " + e.fMeasure(i) );
        }
        System.out.println( e.toSummaryString() );
    }
}
