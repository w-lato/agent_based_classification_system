package agh.edu.agents.experiment;

import agh.edu.agents.enums.S_Type;
import agh.edu.aggregation.ClassGrade;
import agh.edu.learning.ClassRes;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    public static void saveModel(String save_path, Classifier model, ClassRes grade, S_Type type, Instances data, Map<String,Double> used_configs) throws Exception
    {
        String s = modelMetadata( type, grade, used_configs );
        // save to files
        SerializationHelper.write(  save_path + ".model", model  );
        Files.write(Paths.get(          save_path + ".conf"), s.getBytes());
        if( Files.notExists(Paths.get(  save_path + ".arff")))
            Files.write(Paths.get(      save_path + ".arff"), data.toString().getBytes());
    }

    private static String modelMetadata(S_Type type, ClassRes grade, Map<String,Double> used_configs )
    {
        StringBuilder s = new StringBuilder();

        // TYPE:ACC_WGHT:F1_WGHT
        s.append( type ).append(":").append( grade.getAcc_wgt() )
                .append(":").append(grade.getFmeas_wgt()).append("\n");
        // CONF_STR:GRADE
        used_configs.entrySet().stream()
                .sorted(Collections.reverseOrder( Map.Entry.comparingByValue() ))
                .forEach( x-> s.append(x.getKey()).append(":").append(x.getValue()).append("\n") );

        return s.toString();
    }

    private static void saveAgg(String exp_id, Map<String, ClassGrade> perf) throws IOException {
        String s = exp_id +"/AGG/";
        Path p = Paths.get( s );

        // save model_id:grades
        List<String> to_save = new ArrayList<>(perf.keySet());
        to_save = to_save.stream().map( x-> x + ":" + perf.get(x).toString() ).collect(Collectors.toList());
        Files.write(p,to_save);

        // save aggr's model

        // TODO save results of tests? "ResultsHolder"?
    }

}
