package agh.edu.utils;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Reorder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PREPARE_SPEED_DATA
{
    public static void countClasses(Instances data)
    {
        Map<Double,Integer> class_counter = new HashMap<>();
        for (int i = 0; i < data.size(); i++)
        {
            double clas_val = data.get(i).classValue();
            if( class_counter.containsKey( clas_val ) )
            {
                class_counter.put( clas_val, class_counter.get(clas_val) + 1 );
            } else {
                class_counter.put( clas_val, 1 );
            }
        }

        class_counter.forEach((k,v)->{
            Double flat = k;
            System.out.printf("%s : %d%n", data.classAttribute().value(flat.intValue()), v);
        });

    }

    public static void main(String[] args) throws Exception
    {
        Instances data =  ConverterUtils.DataSource.read("D:\\MAZOWIECKIE_SPEED_LIMITS.arff\\MAZOWIECKIE_SPEED_LIMITS.arff");
        data.setClassIndex( data.numAttributes() - 1 );
//
        countClasses( data );

        data.stratify( 10 );
        Instances test = data.testCV( 10, 0 );
        test.setClassIndex( test.numAttributes() - 1 );
        Files.write( Paths.get("D:\\MAZOWIECKIE_SPEED_LIMITS.arff\\MAZOWIECKIE_TEST.arff"), test.toString().getBytes() );

        Instances train = data.trainCV(10,0);
        train.setClassIndex(train.numAttributes() - 1);
        Files.write( Paths.get("D:\\MAZOWIECKIE_SPEED_LIMITS.arff\\MAZOWIECKIE_TRAIN.arff"), train.toString().getBytes() );
//
//        data = data.trainCV(10,0);
//
//
//        SMO smo = new SMO();
//        smo.buildClassifier( data );
//        Evaluation e = new Evaluation(test);
//        e.evaluateModel( smo, test );
//        System.out.println( e.toSummaryString() );



//        Instances all = null; //Instances.mergeInstances(null,null);
//        for (Path path : Files.list(Paths.get("D:\\speed-limit")).collect(Collectors.toList()))
//        {
//            String name = path.getFileName().toString();
////            if( name.startsWith("PST-") )
//            if( name.contains("mazowieckie") )
////            if( name.startsWith("P-") || name.startsWith("T-") )
//            {
//                System.out.println( name );
//                System.out.println( path.toAbsolutePath().toString() );
//                String new_path = "D:\\FILTERED_SPEED_DATA\\FILTERED_" + path.getFileName().toString();//.replaceAll("[ół() \\-]","");
//
//                // load and delete ID column
//                Instances data_set = ConverterUtils.DataSource.read( path.toString() );
//                data_set.deleteAttributeAt(0);
//
//                // set max_speed as the last column
//                Reorder reorder = new Reorder();
//                String order = "2-last,first";
//                reorder.setAttributeIndices(order);
//                reorder.setInputFormat( data_set );
//                data_set = Filter.useFilter( data_set, reorder );
//
//                // numeric max_speed to nominal
////                NumericToNominal convert = new NumericToNominal();
////                String[] options= new String[2];
////                options[0]="-R";
////                options[1]="last";  //range of variables to make numeric
////                convert.setInputFormat( data_set );
//////
////                Instances new_data = Filter.useFilter(data_set, convert);
////                Files.write( Paths.get(  new_path ), new_data.toString().getBytes());
//                Files.write( Paths.get(  new_path ), data_set.toString().getBytes());
//            }
//        }
//        all.deleteAttributeAt(0);
//        System.out.println( all.numAttributes() );
//
//        // set max_speed as the last column
//        Reorder reorder = new Reorder();
//        String order = "2-last,first";
//        reorder.setAttributeIndices(order);
//        reorder.setInputFormat(all);
//        all = Filter.useFilter( all, reorder );
//
//        // numeric max_speed to nominal
//        NumericToNominal convert = new NumericToNominal();
//        String[] options= new String[2];
//        options[0]="-R";
//        options[1]="last";  //range of variables to make numeric
//        convert.setInputFormat( all );
//        Instances new_data = Filter.useFilter(all, convert);
//
//
//        for (int i = 0; i < all.numAttributes(); i++)
//        {
//            System.out.println( i + " : " +  all.attribute(i).name() );
//            System.out.println( all.attribute(i).isNominal() );
//        }
//        System.out.println( all.size() );
//
//
//        for (int i = 0; i < new_data.numAttributes(); i++)
//        {
//            System.out.println( i + " : " +  new_data.attribute(i).name() );
//            System.out.println( new_data.attribute(i).isNominal() );
//        }
//        System.out.println( all.size() );
//        Files.write( Paths.get(  "D:\\speed_test.arff"), new_data.toString().getBytes());
//        all.deleteAttributeAt( 0 );
//        all.replaceAttributeAt(  );
//        Files.write( Paths.get(  "NEW_SPEED.arff"), all.toString().getBytes());
//        Instances test = ConverterUtils.DataSource.read("D:\\speed-limit\\S-K-TCoCrN15N50A-NBUP (województwo małopolskie).arff");

    }
}
