package agh.edu.agents.experiment;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArffMaker
{
    public static void aggResToArff(String res_path, String arff_path) throws Exception {
        String exp_id = res_path.substring(0,res_path.lastIndexOf("/"));

        // load .arff to get classes
        Instances data = ConverterUtils.DataSource.read( arff_path );
        data.setClassIndex( data.numAttributes() - 1 );

        // arff data
        List<String> to_save = Files.readAllLines(Paths.get( res_path ) );
        int id = Integer.valueOf(to_save.remove(0).substring(1));
        to_save.remove(0);
        to_save = to_save.stream().map(x->x.replace("]:[",",").replaceAll("[\\[: \\]]","")).collect(Collectors.toList());
        for (int i = 0; i < data.size(); i++)
        {
            to_save.set( i, to_save.get(i) + "," +  (int)data.get(i).classValue());
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
