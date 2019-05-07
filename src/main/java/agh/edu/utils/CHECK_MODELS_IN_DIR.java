package agh.edu.utils;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CHECK_MODELS_IN_DIR
{
    public static void main(String[] args) throws Exception
    {
        String dir_path = "EXP/MAZOWIECKIE_8_SMO_27";
        List<Path> models = Files.walk(Paths.get(dir_path)).filter(x->x.getFileName().toString().contains(".model")).collect(Collectors.toList());

        Instances test = ConverterUtils.DataSource.read( "DATA/MAZOWIECKIE_TEST.arff" );
        test.setClassIndex( test.numAttributes() - 1 );

        for (Path path : models)
        {
            System.out.println( path.getFileName().toString() );
            Classifier model = (Classifier) SerializationHelper.read(path.toAbsolutePath().toString());
            Evaluation eval = new Evaluation( test );
            eval.evaluateModel( model, test );

            for (int i = 0; i < test.numClasses(); i++)
            {
                System.out.println(i + ", " + eval.fMeasure( i ) );

            }
            System.out.println( eval.toSummaryString() );
            System.out.println();
        }
    }
}
