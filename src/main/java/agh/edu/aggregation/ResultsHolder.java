package agh.edu.aggregation;

import java.util.*;
import java.util.stream.Collectors;

//  TODO needs some refactoring
//  todo NORMALIZE the results of eight agg
public class ResultsHolder
{
    private final Integer ID;
    private final LinkedHashMap<String,List<double[]>> probs;

    public Integer getID() { return ID; }
    public LinkedHashMap<String, List<double[]>> getProbs() {
        return probs;
    }

    public ResultsHolder(Integer ID)
    {
        this.ID = ID;
        probs = new LinkedHashMap<>();
    }

    public ResultsHolder(Integer ID, LinkedHashMap<String,List<double[]>> m)
    {
        this.ID = ID;
        probs = m;
    }

    public void appendProbs(String model_id,List<double[]> part_probs, Map<String, ClassGrade> perf)
    {
        if( !probs.containsKey( model_id ) )
        {
            probs.put( model_id, part_probs);
        }
        else
        {
            System.out.println(" ###  TWO RESPONSES TO THE SAME QUERY FROM SLAVE: " + model_id);
        }
    }

    //#ID
    //M_1:M_2:M_3
    //[0,1,0]:[0,0,1]:[1,0,0]
    @Override
    public String toString()
    {
        List<String> to_save = new ArrayList<>();
        Set<String> ids = probs.keySet();

        to_save.add("#"+ID);
        to_save.add( String.join(":", ids) );
        int N = probs.get( ids.iterator().next() ).size();
        List<double[]> aux = new ArrayList<>();

        for (int i = 0; i < N; i++)
        {
            aux.clear();
            for( String m_id :  ids)
            {
                aux.add( probs.get( m_id ).get( i ) );
            }
            to_save.add( aux.stream().map(Arrays::toString).collect(Collectors.joining(":")) );
        }
        return String.join("\n", to_save);
    }

    public String toString_with_weights(Map<String, ClassGrade> perf)
    {
        List<String> to_save = new ArrayList<>();
        Set<String> ids = probs.keySet();

        to_save.add("#"+ID);
        to_save.add( String.join(":", ids) );
        int N = probs.get( ids.iterator().next() ).size();
        List<double[]> aux = new ArrayList<>();
        //double[] norm_wghts = ClassPred.SoftVotingVariations.normWeights( ids.size(),ids, perf );

        int num_classes = probs.get( ids.iterator().next() ).get(0).length;
        double[] arr = new double[ num_classes ];
        // over rows
        double max_val = 0.0;
        for (int i = 0; i < N; i++)
        {
            aux.clear();

            // over cols
            for( String m_id :  ids)
            {
                System.arraycopy(probs.get( m_id ).get( i ),0,arr,0, num_classes);
                for (int j = 0; j < arr.length; j++) {
//                    arr[j] = arr[j] * norm_wghts[ ctr ];
                    arr[j] = arr[j] * perf.get(m_id).getGrade();
                    if(max_val < arr[j]) max_val = arr[j];
                }
                aux.add( arr );
            }
        }

        for (int i = 0; i < N; i++)
        {
            aux.clear();
            // over cols
            int ctr = 0;
            for( String m_id :  ids)
            {
                System.arraycopy(probs.get( m_id ).get( i ),0,arr,0, num_classes);
                for (int j = 0; j < arr.length; j++) {
//                    arr[j] = arr[j] * norm_wghts[ ctr ];
                    arr[j] = arr[j] * perf.get(m_id).getGrade() / max_val;
                }
                aux.add( arr );
            }
            to_save.add( aux.stream().map(Arrays::toString).collect(Collectors.joining(":")) );
        }

        return String.join("\n", to_save);
    }


    public String toString_with_log_weights(Map<String, ClassGrade> perf)
    {
        List<String> to_save = new ArrayList<>();
        Set<String> ids = probs.keySet();

        to_save.add("#"+ID);
        to_save.add( String.join(":", ids) );
        int N = probs.get( ids.iterator().next() ).size();
        List<double[]> aux = new ArrayList<>();
        double[] norm_wghts = ClassPred.SoftVotingVariations.normWeights( ids.size(),ids, perf );
        // over rows
        int num_classes = probs.get( ids.iterator().next() ).get(0).length;
        double[] arr = new double[ num_classes ];
        for (int i = 0; i < N; i++)
        {
            aux.clear();

            // over cols
            int ctr = 0;
            for( String m_id :  ids)
            {

                System.arraycopy(probs.get( m_id ).get( i ),0,arr,0, num_classes);
                for (int j = 0; j < arr.length; j++) {
                    arr[j] = Math.log( arr[j] * norm_wghts[ ctr ]);
                }
                aux.add( arr );
                ctr++;
            }
            to_save.add( aux.stream().map(Arrays::toString).collect(Collectors.joining(":")) );
        }
        return String.join("\n", to_save);
    }


    public static ResultsHolder fromString( String s )
    {
        List<String> l = Arrays.asList( s.split("\n") );
        Integer query_id = Integer.valueOf( l.get(0).substring(1) );
        String[] keys = l.get(1).split(":");
        LinkedHashMap<String,List<double[]>> m = new LinkedHashMap<>();

        for (String key : keys) { m.put(key, new ArrayList<>()); }
        for (int i = 2; i < l.size(); i++)
        {
            String[] aux = l.get( i ).split(":");
            for (int i1 = 0; i1 < aux.length; i1++)
            {
                String[] str_dbls = aux[i1].substring(1,aux[i1].length() - 1).split(",");
                double[] x = new double[ str_dbls.length ];
                for (int i2 = 0; i2 < str_dbls.length; i2++)
                {
                    x[i2] = Double.valueOf( str_dbls[i2] );
                }
                m.get( keys[i1] ).add( x );
            }
        }
        return new ResultsHolder( query_id, m );
    }
}
