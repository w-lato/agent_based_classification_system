package agh.edu.agents.experiment;

import agh.edu.agents.Aggregator;
import agh.edu.agents.Aggregator.ClassGrade;
import agh.edu.agents.enums.S_Type;
import agh.edu.learning.ClassRes;
import org.bytedeco.javacpp.presets.opencv_core;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Saver
{

    public static final String save_dir = "EXP/";

    /**
     * Saves file in given directory:
     *  EXP/MNIST_1/TYPE_GRADE.model - a model
     *  EXP/MNIST_1/TYPE_GRADE.meta  - data related to used config, grading, and already tested configs
     *  EXP/MNIST_1/TYPE_GRADE.arff  - .arff data used to train the model
     *
     *
     * */
    public static void saveModel(String exp_id, Classifier model, ClassRes grade, S_Type type, Instances data, Map<String,String> used_configs) throws Exception
    {
        String cur_exp_path = save_dir + exp_id + "/" + type + "_" + String.valueOf( ClassRes.computeWeight( grade ) );
        String s = modelMetadata( type,grade, used_configs );

        // save to files
        SerializationHelper.write(  cur_exp_path + ".model", model  );
        Files.write(Paths.get(          cur_exp_path + ".arff"), data.toString().getBytes());
        Files.write(Paths.get(          cur_exp_path + ".conf"), s.getBytes());
    }

    private static String modelMetadata(S_Type type, ClassRes grade, Map<String,String> used_configs )
    {
        StringBuilder s = new StringBuilder();

        // TYPE:GRADE
        s.append( type ).append( ":").append( ClassRes.computeWeight( grade ) ).append("\n");
        s.append( gradeToString( grade ) ).append("\n");

        // TRIED_CONFIGS:WITH_THEIR_GRADES
        used_configs.entrySet().stream()
                .sorted(Collections.reverseOrder( Map.Entry.comparingByValue() ))
                .forEach( x-> s.append(x.getKey()).append("\n").append(x.getValue()).append("\n") );

        return s.toString();
    }

    public static String gradeToString(ClassRes cr)
    {
        StringBuilder s = new StringBuilder();
        // acc
        s.append(cr.getAcc() ).append(":");

        // fscore
        double[] aux = cr.getFscore();
        for (double aux1 : aux) { s.append(aux1).append(","); }
        s.append(":");
        aux = cr.getAUROC();
        for (double aux1 : aux) { s.append(aux1).append(","); }
        return s.toString();
    }
}
