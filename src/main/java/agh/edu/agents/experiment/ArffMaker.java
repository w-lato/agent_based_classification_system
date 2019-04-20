package agh.edu.agents.experiment;

import agh.edu.aggregation.ResultsHolder;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO sort results by classifiers name
public class ArffMaker
{
    public static void aggResToArff(String res_path, String arff_path) throws Exception {
        String exp_id = res_path.substring(0,res_path.lastIndexOf("/"));

        // load .arff to get classes
        Instances data = ConverterUtils.DataSource.read( arff_path );
        data.setClassIndex( data.numAttributes() - 1 );

        // arff data
        List<String> to_save = Files.readAllLines(Paths.get( res_path ) );
        int id = Integer.valueOf(to_save.get(0).substring(1));
        ResultsHolder rh = ResultsHolder.fromString( String.join("\n",to_save) );
        Map<String,List<double[]>> probs = rh.getProbs();
        List<String> sorted_models = probs.keySet().stream().sorted().collect(Collectors.toList());

        // create the @data
        to_save.clear();
        int N = probs.get( sorted_models.get(0) ).size();

        // over rows
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < N; i++)
        {
            // over sorted cols (models)
            s.setLength(0);
            for (String model_id : sorted_models)
            {
                s.append(Arrays.toString(probs.get(model_id).get(i)).replace("[","").replace("]",""));
                s.append(",");
            }
            s.append( (int)data.get(i).classValue() );
            to_save.add( s.toString() );
        }

        // prepare "prefix"
        List<String> prefix = new ArrayList<>();
        int num_of_atts = to_save.get(0).split(",").length - 1;

        prefix.add("@relation " + res_path.substring(res_path.lastIndexOf("/")+1).replace(".res",""));
        prefix.add("");
        for (int i = 0; i < num_of_atts; i++)
        {
            prefix.add("@attribute in"+(i+1) +" numeric");
        }
        String classes = "@attribute class {";
        for (int i = 0; i < data.numClasses() - 1; i++) {
            classes += i + ",";
        }
        classes += (data.numClasses() - 1) + "}";
        prefix.add(classes);
        prefix.add("");
        prefix.add("@data");
        prefix.addAll( to_save );

        Files.write(Paths.get(exp_id + "/Q_" + id + "_TRAIN.arff"), String.join("\n",prefix).getBytes());
    }
}
