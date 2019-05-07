package agh.edu.agents.experiment;

import agh.edu.agents.Aggregator.AggSetup;
import agh.edu.agents.enums.S_Type;
import agh.edu.aggregation.ClassGrade;
import agh.edu.learning.ClassRes;
import akka.actor.ActorRef;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import scala.concurrent.java8.FuturesConvertersImpl;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO the agg's model and load results (?)
public class Loader
{
    public static S_Type getType(Path p) throws IOException
    {
        List<String> s = Files.readAllLines( p );
        return S_Type.valueOf( s.get(0).split(":")[0] );
    }

    public static LinkedHashMap<String, ClassRes> getConfigs(Path p) throws IOException
    {
        List<String> s = Files.readAllLines( p );
        LinkedHashMap<String, ClassRes> m = new LinkedHashMap<>();
        for (int i = 1; i < s.size(); i++)
        {
            String[] aux = s.get(i).split("@");
            m.put( aux[0], ClassRes.fromGrade( ClassGrade.fromString(aux[1]) ) );
        }
        return m;
    }

    public static Classifier getModel(String p) throws Exception
    {
        return (Classifier) SerializationHelper.read( p );
    }

    public static Instances getData(String s) throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( s );
        Instances instances = source.getDataSet();
        instances.setClassIndex( instances.numAttributes() - 1 );
        return instances;
    }


    public static AggSetup getAggSetup(Path p, ActorRef master) throws Exception
    {
        List<String> l = Files.readAllLines( p );

        // setup stack model
        String[] first_line = l.get(0).split(":");
        String model_id = (first_line.length > 1) ? first_line[1] : "";
        String[] order = (first_line.length > 1) ? first_line[2].split(",") : null;
        String exp_id = first_line[0];

        Map<String, ClassGrade> m = new HashMap<>();
        if( l.size() > 1 )
        {
            for (int i = 1; i < l.size(); i++)
            {
                String[] aux = l.get(i).split("@");
                m.put( exp_id + "/" +aux[0], ClassGrade.fromString( aux[1] ) );
            }
        }
        Classifier model = !model_id.isEmpty() ? (Classifier) SerializationHelper.read(p.toString()+ "\\AGG\\"+model_id +".model") : null;
        return new AggSetup(master, exp_id, m, model, order);
    }

    public static int getNextQueryID(String s) throws IOException {
        Path p = Paths.get( s );
        if ( Files.notExists( p )) return -1;
        int query_id = Files.list(p).map(x -> x.getFileName().toString())
                .filter(x -> x.startsWith("Q_") && x.endsWith(".arff"))
                .map(x -> x.replaceAll("[Q_|.arff|TRAIN]",""))
                .map(Integer::valueOf)
                .max(Integer::compareTo)
                .orElse(0);
        return query_id + 1;
    }

}
