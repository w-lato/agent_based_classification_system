package agh.edu.agents.experiment;

import agh.edu.agents.enums.S_Type;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Loader
{
    public static final class LoadExp
    {
        private final String exp_dir_path;
        private final String conf_path;

        public LoadExp(String exp_dir_path) throws IOException
        {
            this.exp_dir_path = exp_dir_path;
            String conf_name = exp_dir_path.substring( exp_dir_path.lastIndexOf("/") + 1, exp_dir_path.lastIndexOf("_") );
            conf_path = "CONF/" + conf_name;
        }
        public String getExp_dir_path() { return exp_dir_path; }
        public String getConf_path() { return conf_path; }
    }

    public static S_Type getType(Path p) throws IOException
    {
        List<String> s = Files.readAllLines( p );
        return S_Type.valueOf( s.get(0).split(":")[0] );
    }

    public static LinkedHashMap<String, String> getConfigs(Path p) throws IOException
    {
        List<String> s = Files.readAllLines( p );
        LinkedHashMap<String,String> m = new LinkedHashMap<>();
        for (int i = 1; i < s.size() - 1; i++)
        {
            m.put( s.get(i), s.get(i + 1) );
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
