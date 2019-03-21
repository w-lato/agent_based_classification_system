package agh.edu.agents.experiment;

import agh.edu.agents.enums.S_Type;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

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

}
