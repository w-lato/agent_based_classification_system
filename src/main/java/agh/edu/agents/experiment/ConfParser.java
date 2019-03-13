package agh.edu.agents.experiment;


import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.enums.Split;
import org.json.JSONObject;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfParser
{

    public static RunConf getConfFrom(String path)
    {
        try {
            String data = new String(Files.readAllBytes(Paths.get(path)));
            JSONObject obj = new JSONObject(data);
            String train_path = obj.getJSONObject("paths").getString("train");
            S_Type[] agents = toENumArr(obj.getJSONObject("agents"));
            Split split = Split.valueOf( obj.getJSONObject("split").getString("method") );
            Optional<Double> fill = obj.getJSONObject("split").has("fill")? Optional.of( obj.getJSONObject("split").getDouble("fill") )
                    : Optional.empty();
            ClassStrat vote = ClassStrat.valueOf( obj.getString("class_strat") );

            ConverterUtils.DataSource source = new ConverterUtils.DataSource( train_path );
            Instances train = source.getDataSet();
            Instances test;
            if( !obj.getJSONObject("paths").has("test") )
            {
                List<Instances> l = Splitter.splitOnTestAndTrain( train, 10 );
                train = l.get(0);
                test = l.get(1);
            } else {
                String test_path = obj.getJSONObject("paths").getString("test");
                source = new ConverterUtils.DataSource( test_path );
                test = source.getDataSet();
            }
            train.setClassIndex( train.numAttributes() - 1 );
            test.setClassIndex( test.numAttributes() - 1 );

//            for (S_Type agent : agents) { System.out.println(agent); }
            return new RunConf.Builder()
                    .agents( agents )
                    .split_meth( split )
                    .class_method( vote )
                    .fill( fill )
                    .train( train )
                    .test( test )
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static S_Type[] toENumArr( JSONObject obj )
    {
        String[] class_types = new String[]{
                S_Type.SMO.name(),
                S_Type.ADA.name(),
                S_Type.NA.name(),
                S_Type.IBK.name(),
                S_Type.LOG.name(),
                S_Type.MLP.name(),
                S_Type.RF.name()
        };
        List<S_Type> l = new ArrayList<>();

        for (String curr : class_types)
        {
            if (obj.has(curr))
                for (int j = 0; j < obj.getInt(curr); j++)
                    l.add(S_Type.valueOf(curr));
        }
        return l.toArray(new S_Type[0]);
    }



    public static void main(String[] args) throws IOException
    {
        System.out.println ( S_Type.SMO.name());
        String data = new String(Files.readAllBytes(Paths.get("CONF/default")));
        System.out.println( data );
        JSONObject obj = new JSONObject(data);
        Integer pageName = obj.getJSONObject("agents").getInt("J48");

        System.out.println(pageName);

        JSONObject arr = obj.getJSONObject("split");
        for (int i = 0; i < arr.length(); i++) {
//            String post_id = arr.getJSONObject(i).getString("method");
//            System.out.println(post_id);
        }

        S_Type[] arr1 = toENumArr( obj.getJSONObject("agents") );
        for (int i = 0; i < arr1.length; i++)
        {
            System.out.println( arr1[i] );
        }

        RunConf rc = getConfFrom( "CONF/default" );
        System.out.println("dsfd");
    }
}
