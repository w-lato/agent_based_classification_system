package agh.edu.agents.experiment;

import agh.edu.agents.enums.S_Type;
import agh.edu.learning.ClassRes;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Saver
{

    public static AtomicInteger id_gen = new AtomicInteger();
    public static final String save_dir = "EXP/";

    public static synchronized int getIntID() { return id_gen.incrementAndGet(); }

    public static String setupNewExp(String conf_name) throws IOException
    {
        System.out.println(" CONFFF " + conf_name);
        List<String> l = Files.list(Paths.get(save_dir))
                .filter(Files::isDirectory)
                .map(x->x.getFileName().toString())
                .collect(Collectors.toList());

        l = l.stream().map(x->x.substring(x.lastIndexOf("_")+1)).collect(Collectors.toList());
        int max = l.stream().map(Integer::valueOf).max(Integer::compareTo).orElse(-1);
        String s = save_dir + conf_name.toUpperCase() + "_" + (max+1);
        Files.createDirectories( Paths.get(s) );

        return s;
    }

    /**
     * Saves file in given directory:
     *  EXP/MNIST_1/TYPE_MODEL_ID.model - a model
     *  EXP/MNIST_1/TYPE_MODEL_ID.conf  - data related to used config, grading, and already tested configs
     *  EXP/MNIST_1/TYPE_MODEL_ID.arff  - .arff data used to train the model
     *
     * */
    public static void saveModel(String save_path, Classifier model, ClassRes grade, S_Type type, Instances data, Map<String,String> used_configs) throws Exception
    {
        String s = modelMetadata( type, grade, used_configs );
        // save to files
        SerializationHelper.write(  save_path + ".model", model  );
        Files.write(Paths.get(          save_path + ".conf"), s.getBytes());
        if( Files.notExists(Paths.get(  save_path + ".arff")))
            Files.write(Paths.get(      save_path + ".arff"), data.toString().getBytes());
    }

    private static String modelMetadata(S_Type type, ClassRes grade, Map<String,String> used_configs )
    {
        StringBuilder s = new StringBuilder();

        // TYPE:GRADE
        s.append( type ).append( ":").append( ClassRes.computeWeight( grade ) ).append("\n");

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
        s.deleteCharAt( s.lastIndexOf(",") );
        s.append(":");
        aux = cr.getAUROC();
        for (double aux1 : aux) { s.append(aux1).append(","); }
        s.deleteCharAt( s.lastIndexOf(",") );
        return s.toString();
    }
}
