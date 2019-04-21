package agh.edu.agents.experiment;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.enums.S_Type;
import agh.edu.aggregation.ClassGrade;
import agh.edu.aggregation.ClassPred;
import agh.edu.aggregation.ResultsHolder;
import agh.edu.learning.ClassRes;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// TODO save data from ResultsHolder
// TODO save "future" model from Aggregator
public class Saver
{
    public static AtomicInteger id_gen = new AtomicInteger();
    public static final String save_dir = "EXP/";
    public static final String agg_postfix = "/AGG/agg.conf";

    public static synchronized int getIntID() { return id_gen.incrementAndGet(); }

    public static String setupNewExp(String conf_name) throws IOException
    {
        System.out.println(" :@: NEW EXP " + conf_name);
        List<String> l = Files.list(Paths.get(save_dir))
                .filter(Files::isDirectory)
                .map(x->x.getFileName().toString())
                .collect(Collectors.toList());

        l = l.stream().map(x->x.substring(x.lastIndexOf("_")+1)).collect(Collectors.toList());
        int max = l.stream().map(Integer::valueOf).max(Integer::compareTo).orElse(-1);
        String exp_id = save_dir + conf_name.toUpperCase() + "_" + (max+1);
        Files.createDirectories( Paths.get(exp_id +"/AGG") );
        Files.write( Paths.get( exp_id +"/AGG/agg.conf" ), exp_id.getBytes() );

        return exp_id;
    }

    /**
     * Saves file in given directory:
     *  EXP/MNIST_1/TYPE_MODEL_ID.model - a model
     *  EXP/MNIST_1/TYPE_MODEL_ID.conf  - data related to used config, grading, and already tested configs
     *  EXP/MNIST_1/TYPE_MODEL_ID.arff  - .arff data used to train the model
     * */
    public static void saveModel(String save_path, Classifier model, ClassRes grade, S_Type type, Instances data, Map<String,Double> used_configs) throws Exception
    {
        String s = modelMetadata( type, grade, used_configs );
        // save to files
        SerializationHelper.write(  save_path + ".model", model  );
        Files.write(Paths.get(          save_path + ".conf"), s.getBytes());
        if( !Files.exists(Paths.get(  save_path + ".arff")))
            Files.write( Paths.get(  save_path + ".arff"), data.toString().getBytes());
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

    public static void saveAgg(String exp_id, Map<String, ClassGrade> perf) throws IOException {
        Path p = Paths.get( exp_id + agg_postfix );

        // save model_id:grades
        List<String> to_save = new ArrayList<>(perf.keySet());
        if( !to_save.isEmpty() )
        {
            to_save = to_save.stream().map( x-> x.substring(x.lastIndexOf("/")+1) + "@" + perf.get(x).toString() ).collect(Collectors.toList());
        }
        to_save.add(0, exp_id);
        Files.write(p,to_save);
    }

    public static void saveQueryData(String exp_id,int query_id, Instances to_save) throws IOException
    {
        String new_arff_data = exp_id + "/AGG/Q_" + query_id + ".arff";
        saveStringIn( new_arff_data, to_save.toString() );
    }

    private static void saveStringIn(String path, String to_save) throws IOException
    {
        BufferedWriter writer = new BufferedWriter( new FileWriter( path ));
        writer.write( to_save );
        writer.close();
    }

    // save few versions of prob data
    // 1. raw (with no prob modifications)
    // 2. every prob is multiplied by the performance of the classifier
    // 3. the same as in 2nd, but with log(  )
    public static void saveAggResults(String exp_id,Map<String,ClassGrade>  perf, Map<Integer,ResultsHolder> results) throws IOException {
        Set<Integer> ids = results.keySet();

        // RAW
        for (Integer id : ids)
        {
            Path p = Paths.get( exp_id + "/AGG/Q_RAW_" + id + ".res" );
            Files.write( p, results.get( id ).toString().getBytes() );
        }

        // MUL BY THE NORMALIZED GRADES
        // TODO fix
//        for (Integer id : ids)
//        {
//            Path p = Paths.get( exp_id + "/AGG/Q_WEIGHT_" + id + ".res" );
//            Files.write( p, results.get( id ).toString_with_weights(perf).getBytes() );
//        }

//        // LOG_e( prob * wght )
//        for (Integer id : ids)
//        {
//            Path p = Paths.get( exp_id + "/AGG/Q_LOG_" + id + ".res" );
//            Files.write( p, results.get( id ).toString_with_log_weights(perf).getBytes() );
//        }
    }

    public static void saveAggPredictions(String exp_id, int query_id, ClassStrat strat, List<String> to_save) throws IOException
    {
        Path p = Paths.get( exp_id + "/AGG/Q_"+query_id+ "_"+strat+".pred" );
        String s = to_save.stream().collect(Collectors.joining("\n"));
        Files.write( p, s.getBytes());
    }
}
