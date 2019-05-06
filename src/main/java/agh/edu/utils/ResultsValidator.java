package agh.edu.utils;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultsValidator
{

    public static void compare(String to_file, String to_data) throws Exception
    {

        Instances data = ConverterUtils.DataSource.read( to_data );
        data.setClassIndex( data.numAttributes() - 1 );
        Path p = Paths.get( to_file );
        List<String> l = Files.readAllLines( p );
        l.remove(0);
        List<Integer> classes = l.stream().map(x->x.substring( x.lastIndexOf(",")+1 )).map(Integer::valueOf).collect(Collectors.toList());

        Map<Integer,Integer> class_ctr = new HashMap<>();
        Map<Integer,Integer> wrong_classes = new HashMap<>();

        for (int i = 0; i < data.numClasses(); i++)
        {
            class_ctr.put( i,0 );
            wrong_classes.put( i,0 );
        }


        int ctr = 0;
        for (int i = 0; i < classes.size(); i++)
        {
            Integer correct_class = (int)data.get( i ).classValue();
            class_ctr.put( correct_class, class_ctr.get( correct_class ) + 1 );
            if( classes.get(i ) != correct_class )
            {
                wrong_classes.put( correct_class, wrong_classes.get( correct_class ) + 1 );
                ctr++;
//                System.out.println( i + " : is : " +  classes.get(i) + " but should be: " + correct_class  );
            }
        }
        System.out.println();
        System.out.println("TOTAL: " + ctr + "    " + (((double)ctr / data.size())*100) + "% misclassified");
        for (int i = 0; i < data.numClasses(); i++)
        {
            System.out.println( i + "  cnt: " + class_ctr.get( i ) + "  misses: " + wrong_classes.get(i) + "  " + (double)(wrong_classes.get(i))/class_ctr.get(i));
        }
        System.out.println();
    }

    public static void main(String[] args) throws Exception
    {
//        String exp_id = "EXP/BEST_128_SMO_0";
//        String exp_id = "EXP/MNIST_32_MLP_SIMPLE_4";
//        String exp_id = "EXP\\MNIST_8_OF_ALL_SIMPLE_5";
        String exp_id = "EXP/TIME_SMO_8_0.1_8";
        int query_id = 12;

        String test_data_path = exp_id + "\\AGG\\Q_" + query_id + ".arff";
        String results_path_1 = exp_id + "\\AGG\\Q_" + query_id + "_MAJORITY.pred";
        String results_path_2 = exp_id + "\\AGG\\Q_" + query_id + "_WEIGHTED.pred";
        String results_path_3 = exp_id + "\\AGG\\Q_" + query_id + "_PROB_WEIGHT.pred";
        String results_path_4 = exp_id + "\\AGG\\Q_" + query_id + "_F1_SCORE_VOTING.pred";
        String results_path_5 = exp_id + "\\AGG\\Q_" + query_id + "_F1_SCORE_PROB.pred";

//        String results_path_4 = exp_id + "\\AGG\\Q_" + query_id + "_F1_SCORE_VOTING.pred";

        System.out.println("============================ MAJORITY");
        ResultsValidator.compare(results_path_1, test_data_path);
        System.out.println("============================ WEIGHTED");
        ResultsValidator.compare(results_path_2, test_data_path);
        System.out.println("============================ PROB_WEIGHTED");
        ResultsValidator.compare(results_path_3, test_data_path);

        if(Files.exists( Paths.get( results_path_4 ) ))
        {
            System.out.println("============================ F1_SCORE_VOTING");
            ResultsValidator.compare(results_path_4, test_data_path);
        } else {
            System.out.println("NO F1_SCORE_VOTING_FILE");
        }

        if(Files.exists( Paths.get( results_path_5 ) ))
        {
            System.out.println("============================ F1_SCORE_PROB_VOTING");
            ResultsValidator.compare(results_path_5, test_data_path);
        } else {
            System.out.println("NO F1_PROB_FILE");
        }

//        String exp_id = "EXP\\SLAVE_ONLY_TEST_1";
//        int query_id = 40;
//
//        String test_data_path = exp_id + "\\AGG\\Q_" + query_id + ".arff";
//        String results_path_1 = exp_id + "\\AGG\\Q_" + query_id + "_MAJORITY.pred";
//        String results_path_2 = exp_id + "\\AGG\\Q_" + query_id + "_WEIGHTED.pred";
//        String results_path_3 = exp_id + "\\AGG\\Q_" + query_id + "_PROB_WEIGHT.pred";
//
////        String results_path_4 = exp_id + "\\AGG\\Q_" + query_id + "_F1_SCORE_VOTING.pred";
//
//        System.out.println("============================ MAJORITY");
//        ResultsValidator.compare(results_path_1, test_data_path);
//        System.out.println("============================ WEIGHTED");
//        ResultsValidator.compare(results_path_2, test_data_path);
//        System.out.println("============================ PROB_WEIGHTED");
//        ResultsValidator.compare(results_path_3, test_data_path);
//        System.out.println("============================ ");
//        ResultsValidator.compare(results_path_4, test_data_path);
    }
}
