package agh.edu.utils;

import agh.edu.aggregation.ResultsHolder;
import agh.edu.learning.params.ParamsSMO;
import org.apache.commons.math.analysis.interpolation.SmoothingBicubicSplineInterpolator;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SPEED_LIMITS
{
    public static void main(String[] args) throws Exception
    {
        List<Path> paths = Files.walk(Paths.get("EXP/TIME_SMO_8_0.1_8/AGG")).filter(x->x.getFileName().toString().contains(".res") && !x.getFileName().toString().contains("RAW_11")).collect(Collectors.toList());
        paths.sort( Path::compareTo );
        paths.forEach(System.out::println);

        List<Path> arf_paths = Files.walk(Paths.get("EXP/TIME_SMO_8_0.1_8/AGG"))
                .filter(x->x.getFileName().toString().contains(".arff") && !x.getFileName().toString().contains("Q_11"))
                .sorted( Path::compareTo )
                .peek( System.out::println )
                .collect(Collectors.toList());

        List<Instances> arf_inst = new ArrayList<>();
        for (int i = 0; i < arf_paths.size(); i++)
        {
            arf_inst.add( ConverterUtils.DataSource.read( arf_paths.get(i).toAbsolutePath().toString() ) );
            arf_inst.get( arf_inst.size() - 1 ).setClassIndex( arf_inst.get( arf_inst.size() - 1 ).numAttributes() - 1 );
        }

        String[] order = "EXP/TIME_SMO_8_0.1_8/SMO_2:EXP/TIME_SMO_8_0.1_8/SMO_1:EXP/TIME_SMO_8_0.1_8/SMO_4:EXP/TIME_SMO_8_0.1_8/SMO_3:EXP/TIME_SMO_8_0.1_8/SMO_5:EXP/TIME_SMO_8_0.1_8/SMO_6:EXP/TIME_SMO_8_0.1_8/SMO_8:EXP/TIME_SMO_8_0.1_8/SMO_7".split(":");
        Arrays.sort(order, String::compareTo);
        for (int i = 0; i < order.length; i++)
        {
            System.out.println( order[i] );
        }

        List<Instances> insta = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++)
        {
            List<String> file_content = Files.readAllLines( paths.get(i) );
            ResultsHolder res = ResultsHolder.fromString( String.join( "\n",file_content) );
            Instances res_inst = res.toStackTrainSet( order );
            res_inst.setClassIndex( res_inst.numAttributes() - 1 );
            Set<Integer> ctr = new HashSet<>();
            for (int i1 = 0; i1 < res_inst.size(); i1++)
            {
                int class_val = Integer.valueOf( arf_inst.get(i).get(i1).classAttribute().value((int) arf_inst.get(i).get(i1).classValue()) );
//                System.out.println( class_val );
                ctr.add(class_val );
                res_inst.get( i1 ).setClassValue( class_val );
            }

            System.out.println( i +  "  NUM " + ctr.size() );
            ctr.clear();
            insta.add( res_inst );
        }


        Instances all = insta.get(0);
        for (int i = 1; i < insta.size(); i++)
        {
            all.addAll( insta.get(i) );
        }

//        NumericToNominal convert = new NumericToNominal();
//        String[] options= new String[2];
//        options[0]="-R";
//        options[1]=String.valueOf("last");  //range of variables to make numeric
//        convert.setInputFormat( all );
//        all = Filter.useFilter(all, convert);
//
//            insta.add( new_data );

        Files.write( Paths.get("D:\\test_arff.txt"), all.toString().getBytes() );

        // split into train and test
        List<String> file_content = Files.readAllLines( Paths.get("EXP\\TIME_SMO_8_0.1_8\\AGG\\Q_RAW_11.res") );
        ResultsHolder res = ResultsHolder.fromString( String.join( "\n",file_content) );
        Instances test = res.toStackTrainSet( order );
        test.setClassIndex( test.numAttributes() - 1 );

        Instances mnist_test = ConverterUtils.DataSource.read("DATA/mnist_test.arff");
        mnist_test.setClassIndex( mnist_test.numAttributes() - 1 );
        for (int i = 0; i < mnist_test.size(); i++)
        {
            test.get(i).setClassValue( mnist_test.get(i).classValue() );
        }

//        convert = new NumericToNominal();
//        options= new String[2];
//        options[0]="-R";
//        options[1]=String.valueOf("last");  //range of variables to make numeric
//        convert.setInputFormat( test );
//        test = Filter.useFilter(test, convert);

//        Files.write( Paths.get("D:\\test_test.txt"), test.toString().getBytes() );

        // buld and eval
        long st = System.currentTimeMillis();
        System.out.println( st );
        ParamsSMO p = new ParamsSMO();
        SMO smo = (SMO) p.clasFromStr("0,3.0,false");
//        SMO smo = new SMO();
        System.out.println( all.size() );
        smo.buildClassifier( all );
        SerializationHelper.write(   "D:\\SMP_1_03_false_SMO_8_01_8.model", smo);

        System.out.println( System.currentTimeMillis() - st );
        st = System.currentTimeMillis();

        Evaluation eval = new Evaluation(test);
        eval.evaluateModel( smo, test );
        System.out.println( eval.toSummaryString() );
        System.out.println( System.currentTimeMillis() - st);
    }
}
