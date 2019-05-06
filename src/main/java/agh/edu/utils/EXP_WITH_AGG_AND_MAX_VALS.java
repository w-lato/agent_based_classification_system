package agh.edu.utils;

import agh.edu.aggregation.ResultsHolder;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class EXP_WITH_AGG_AND_MAX_VALS
{
    public static void main(String[] args) throws Exception
    {
        List<Path> paths = Files.walk(Paths.get("EXP/TIME_SMO_8_0.1_8/AGG"))
                .filter(x->x.getFileName().toString().contains(".res") &&
                        !x.getFileName().toString().contains("RAW_11") &&
                        !x.getFileName().toString().contains("RAW_12")
                ).collect(Collectors.toList());
        paths.sort( Path::compareTo );
        paths.forEach(System.out::println);

        List<Path> arf_paths = Files.walk(Paths.get("EXP/TIME_SMO_8_0.1_8/AGG"))
                .filter(x->x.getFileName().toString().contains(".arff")
                        && !x.getFileName().toString().contains("Q_11")
                        && !x.getFileName().toString().contains("Q_12"))
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
            Instances res_inst = res.toMaxStackSet( order );
            res_inst.setClassIndex( res_inst.numAttributes() - 1 );
            Set<Integer> ctr = new HashSet<>();
            for (int i1 = 0; i1 < res_inst.size(); i1++)
            {
                int class_val = Integer.valueOf( arf_inst.get(i).get(i1).classAttribute().value((int) arf_inst.get(i).get(i1).classValue()) );
                ctr.add(class_val );
//                System.out.println(i + "::" + class_val + " :: " + res_inst.numAttributes() );
//                System.out.println(i + "::" + res_inst.get(i1).classIndex() );
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

        Files.write( Paths.get("D:\\train_arff.txt"), all.toString().getBytes() );

        List<String> file_content = Files.readAllLines( Paths.get("EXP\\TIME_SMO_8_0.1_8\\AGG\\Q_RAW_11.res") );
        ResultsHolder res = ResultsHolder.fromString( String.join( "\n",file_content) );
        Instances test = res.toMaxStackSet( order );
        test.setClassIndex( test.numAttributes() - 1 );

        Instances mnist_test = ConverterUtils.DataSource.read("DATA/mnist_test.arff");
        mnist_test.setClassIndex( mnist_test.numAttributes() - 1 );
        for (int i = 0; i < mnist_test.size(); i++)
        {
            test.get(i).setClassValue( mnist_test.get(i).classValue() );
        }
        Files.write( Paths.get("D:\\test_arff.txt"), test.toString().getBytes() );



    }
}
