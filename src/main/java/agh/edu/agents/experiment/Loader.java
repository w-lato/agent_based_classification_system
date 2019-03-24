package agh.edu.agents.experiment;

import agh.edu.agents.Aggregator.AggSetup;
import agh.edu.agents.enums.S_Type;
import agh.edu.aggregation.ClassGrade;
import akka.actor.ActorRef;
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

    public static LinkedHashMap<String, Double> getConfigs(Path p) throws IOException
    {
        List<String> s = Files.readAllLines( p );
        LinkedHashMap<String, Double> m = new LinkedHashMap<>();
        for (int i = 1; i < s.size(); i++)
        {
            System.out.println( s.get(i) );
            String[] aux = s.get(i).split(":");
            m.put( aux[0], Double.valueOf( aux[1] ) );
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


    public static AggSetup getAggSetup(Path p, ActorRef master) throws IOException
    {
        List<String> l = Files.readAllLines( p );
        String id = l.get(0);
        Map<String, ClassGrade> m = new HashMap<>();
        if( l.size() > 1 )
        {
            for (int i = 1; i < l.size(); i++)
            {
                String[] aux = l.get(i).split("@");
                m.put( aux[0], ClassGrade.fromString( aux[1] ) );
            }
        }
        return new AggSetup(master, id, m);
    }

    public static int getNextQueryID(String s) throws IOException {
        Path p = Paths.get( s );
        if ( Files.notExists( p )) return -1;
        int query_id = Files.list(p).map(x -> x.getFileName().toString())
                .filter(x -> x.startsWith("Q_") && x.endsWith(".arff"))
                .map(x -> x.replace("Q_","").replace(".arff", ""))
                .map(Integer::valueOf)
                .max(Integer::compareTo)
                .orElse(0);
        return query_id + 1;
    }

}
