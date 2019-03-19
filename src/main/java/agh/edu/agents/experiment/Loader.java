package agh.edu.agents.experiment;

import agh.edu.agents.enums.S_Type;
import weka.classifiers.Classifier;
import weka.core.SerializationHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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

    public static Map<String, String> getConfigs(Path p) throws IOException
    {
        List<String> s = Files.readAllLines( p );
        Map<String,String> m = new HashMap<>();
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
}
