package agh.edu.aggregation;

import agh.edu.agents.Aggregator.ClassGrade;
import agh.edu.agents.enums.ClassStrat;
import agh.edu.learning.ClassRes;
import akka.actor.ActorRef;
import org.bytedeco.javacpp.presets.opencv_core;
import weka.core.matrix.Matrix;

import java.util.*;
import java.util.stream.Collectors;

public class ClassPred
{
    private static Map<Integer,Integer> aux_ctr= new HashMap<>();

    public static List<Integer> getPreds(ClassStrat s,
                                       Map<String, ClassGrade> perf,
                                       Map<String,List<double[]>> probs)
    {
        switch (s)
        {
            case MAJORITY: return majorityVoting( probs );
            case WEIGHTED: return softVoting(perf,probs);
            default: return null;
        }
    }


    // TODO normalize weights??
    static List<Integer> softVoting(Map<String, ClassGrade> perf, Map<String,List<double[]>> probs )
    {
        Object[] ACTs =  perf.keySet().toArray();
        int M = probs.get(ACTs[0]).size(); // rows
        int N = ACTs.length; // cols
        double[][] tmp;

        List<Matrix> arr = new ArrayList<>();
        for (int i = 0; i < N; i++)
        {
            tmp = new double[ M ][ N ];
            List<double[]> l = probs.get( ACTs[i] );
            for (int i1 = 0; i1 < l.size(); i1++)
            {
                System.arraycopy( l.get(i1),0, tmp[i1],0, N);
            }
            arr.add( new Matrix( tmp, M, N ));
        }

        Object[] wghts = perf.values().stream().map(ClassRes::computeWeight).toArray();
        for (int i = 0; i < N; i++) { arr.get(i).timesEquals((Double) wghts[i]); }
        Matrix aux = arr.stream().reduce( Matrix::plus ).orElse(null);
        return Arrays.stream(aux.getArray())
                .map(ClassPred::maxIdxFrom)
                .collect(Collectors.toList());
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
