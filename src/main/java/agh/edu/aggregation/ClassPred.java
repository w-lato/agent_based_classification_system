package agh.edu.aggregation;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.experiment.Saver;
import weka.core.matrix.Matrix;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ClassPred
{
    private static Map<Integer,Integer> aux_ctr= new HashMap<>();

    public static List<Integer> getPreds(ClassStrat s, Map<String, ClassGrade> perf,
                                         LinkedHashMap<String,List<double[]>> probs) {
        switch (s)
        {
            case MAJORITY: return majorityVoting( probs );
//            case WEIGHTED: return softVoting(perf,probs);
            case WEIGHTED: return soft_v1(perf,probs);
            default: return null;
        }
    }

    public static void saveClassChangesOverTime(String exp_id, int query_id, Map<String, ClassGrade> perf, LinkedHashMap<String,List<double[]>> probs) throws IOException {

        Set<String> keys = probs.keySet();
        Map<String,ClassGrade> tmp_perf = new HashMap<>();
        LinkedHashMap<String,List<double[]>> tmp_probs = new LinkedHashMap<>();
        int N = probs.get( keys.iterator().next() ).size();
        List<StringBuilder> aux = new ArrayList<>();
        for (int i = 0; i < N; i++)
        {
            aux.add( new StringBuilder() );
        }
        List<String> to_save;

        // for each strategy create a file - the order is kept
        for (ClassStrat strat : ClassStrat.values())
        {
            // clear
            aux.forEach(x ->x.setLength(0));
            tmp_perf.clear();
            tmp_probs.clear();

            // iterate over ordered set of models
            for (String mod_id : keys)
            {
                // add next model
                tmp_perf.put(  mod_id, perf.get(mod_id) );
                tmp_probs.put( mod_id, probs.get( mod_id ));

                // get predictions and append them to the list
                List<Integer> res = getPreds( strat, tmp_perf, tmp_probs );
                for (int i = 0; i < res.size(); i++)
                {
                    aux.get(i).append( res.get(i) ).append(",");
                }
            }
            aux = aux.stream().map( x->x.deleteCharAt( x.length() - 1 )).collect(Collectors.toList());
            to_save = aux.stream().map(StringBuilder::toString).peek(System.out::println).collect(Collectors.toList());
            to_save.add(0, String.join(":", keys));
            Saver.saveAggPredictions( exp_id, query_id, strat, to_save );
        }
    }

    // TODO normalize weights??
    static List<Integer> softVoting(Map<String, ClassGrade> perf, LinkedHashMap<String,List<double[]>> probs )
    {
        Set<String> ids = probs.keySet();
        int M = probs.get(ids.iterator().next()).size(); // rows
        int N = ids.size(); // cols
        double[][] tmp;

        List<Matrix> arr = new ArrayList<>();
        for (String id : ids)
        {
            tmp = new double[ M ][ N ];
            List<double[]> l = probs.get( id );
            for (int i1 = 0; i1 < l.size(); i1++)
            {
                System.arraycopy( l.get(i1),0, tmp[i1],0, N);
            }
            arr.add( new Matrix( tmp, M, N ));
        }
//        double max = perf.values().stream().map(ClassGrade::getGrade).max(Double::compareTo).orElse(0.0);
        double sum = perf.values().stream().map(ClassGrade::getGrade).reduce( (a,b)->a+b ).orElse(0.0);
        double[] wghts = new double[ids.size()];
        int ctr = 0;
        for (String id : ids)
        {
            wghts[ctr] = perf.get( id ).getGrade() / sum;
            ctr++;
        }
        for (int i = 0; i < N; i++) { arr.get(i).timesEquals( wghts[i]); }

        Matrix aux = arr.stream().reduce( Matrix::plus ).orElse(null);
        return Arrays.stream(aux.getArray())
                .map(ClassPred::maxIdxFrom)
                .collect(Collectors.toList());
    }

    static List<Integer> soft_v1(Map<String, ClassGrade> perf, LinkedHashMap<String,List<double[]>> probs )
    {
        Set<String> ids = probs.keySet();
        int rows = probs.get( ids.iterator().next() ).size();
        int cols = ids.size();
        List<Integer> l = new ArrayList<>();


        int num_of_classes = probs.get( ids.iterator().next()).get(0).length;
        // normalize weights of models
        double[] norm_wghts = new double[cols];
        double sum = 0.0;
        for (String id : ids) { sum += perf.get( id ).getGrade(); }
        int xd = 0;
        for (String id : ids)
        {
            norm_wghts[xd] = perf.get(id).getGrade() / sum;
            xd++;
        }

        // over rows
        double[] aux = new double[ num_of_classes ];
        for (int i = 0, ctr = 0; i < rows; i++, ctr = 0)
        {
            Arrays.fill( aux,0.0 );
            // over cols
            for (String id : ids)
            {
                // max prob from given model
                int chosen_class = maxIdxFrom( probs.get(id).get(i) );
                aux[ chosen_class ] += norm_wghts[ ctr ];
                ctr++;
            }
            l.add( maxIdxFrom( aux ) );
        }
        return l;
    }

    static List<Integer> majorityVoting( Map<String,List<double[]>> probs )
    {
        int N = ((List<double[]>) probs.values().toArray()[0]).size();
        List<Integer> l = new ArrayList<>();
        int[] tmp = new int[ probs.keySet().size() ];

        for (int i = 0, ctr = 0; i < N; i++, ctr = 0)
        {
            for (List<double[]> it : probs.values())
            {
                tmp[ ctr ] = maxIdxFrom( it.get( i ) );
                ctr++;
            }
            l.add( getMode( tmp ) );
        }
        return l;
    }

    static int maxIdxFrom(double[] arr)
    {
        double max_val = Arrays.stream(arr).max().orElse(0.0);

        for (int i = 0; i < arr.length; i++)
        {
            if( Double.compare(arr[i],max_val) == 0 ) return i;
        }
        return -1;
    }

    static int getMode(int[] arr)
    {
        aux_ctr.clear();
        for (int el : arr) {
            if (!aux_ctr.containsKey(el)) {
                aux_ctr.put(el, 1);
            } else {
                aux_ctr.put(el, aux_ctr.get(el) + 1);
            }
        }
        return findMode();
    }

    private static Integer findMode()
    {
        int max = aux_ctr.values()
                .stream()
                .max(Integer::compareTo)
                .orElse(-1);

        for (Map.Entry<Integer, Integer> it : aux_ctr.entrySet())
        {
            if( it.getValue() == max ) return it.getKey();
        }
        return -1;
    }
}
