package agh.edu.GUI;


import agh.edu.agents.RunConf;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.enums.Split;
import agh.edu.agents.enums.Vote;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConfParser
{
    public static RunConf getConfFrom(String path)
    {
        try {
            String data = new String(Files.readAllBytes(Paths.get("CONF/default")));
            JSONObject obj = new JSONObject(data);
            String train_path = obj.getJSONObject("paths").getString("train");
            String test_path = obj.getJSONObject("paths").getString("test");
            S_Type[] agents = toENumArr(obj.getJSONObject("agents"));
            Split split = Split.valueOf( obj.getJSONObject("split").getString("method") );
            Double ratio = obj.getJSONObject("split").getDouble("ratio");
            Double OL = obj.getJSONObject("split").getDouble("OL");
            Vote vote = Vote.valueOf( obj.getString("vote") );

            ConverterUtils.DataSource source = new ConverterUtils.DataSource( train_path );
            Instances train = source.getDataSet();
            source = new ConverterUtils.DataSource( test_path );
            Instances test = source.getDataSet();

            for (int i = 0; i < agents.length; i++) {
                System.out.println( agents[i] );
            }

            return new RunConf.Builder()
                    .agents( agents )
                    .split_meth( split )
                    .class_method( vote )
                    .ol_ratio( OL )
                    .split_ratio( ratio )
                    .train( train )
                    .test( test )
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static S_Type[] toENumArr( JSONObject obj )
    {
        String[] class_types = new String[]{
                S_Type.J48.name(),
                S_Type.PART.name(),
                S_Type.SMO.name()
        };
        List<S_Type> l = new ArrayList<>();

        for (int i = 0; i < class_types.length; i++)
        {
            String curr = class_types[i];
            if( obj.has( curr ) )
                for (int j = 0; j < obj.getInt( curr ); j++)
                    l.add(  S_Type.valueOf( curr )  );
        }


        return l.toArray(new S_Type[0]);
    }



    public static void main(String[] args) throws IOException
    {
        System.out.println ( S_Type.J48.name());
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
