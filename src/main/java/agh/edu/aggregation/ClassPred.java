package agh.edu.aggregation;

import agh.edu.agents.Aggregator.ClassGrade;
import agh.edu.agents.enums.ClassStrat;
import agh.edu.learning.ClassRes;
import akka.actor.ActorRef;
import weka.core.matrix.Matrix;

import java.util.*;
import java.util.stream.Collectors;

public class ClassPred
{
    private static Map<Short,Integer> aux_ctr= new HashMap<>();

    public static List<Short> getPreds(ClassStrat s,
                                       Map<ActorRef, ClassGrade> perf,
                                       Map<ActorRef,List<double[]>> probs)
    {
        switch (s)
        {
            case MAJORITY: return majorityVoting( probs );
            case WEIGHTED: return softVoting(perf,probs);
            default: return null;
        }
    }


    static List<Short> softVoting(  Map<ActorRef, ClassGrade> perf,
                                    Map<ActorRef,List<double[]>> probs )
    {
        int M = probs.keySet().size();
        int N = ((List<double[]>) probs.values().toArray()[0]).size();
        double[][] tmp = new double[N][ probs.keySet().size() ];
        Matrix wghts = new Matrix( M, N );
        Matrix[] arr = new Matrix[M];
        int i = 0;
        for (ActorRef it : perf.keySet())
        {
            ClassGrade cg = perf.get(it);
            wghts.set( i, i, ClassRes.computeWeight( cg.getFscore(),cg.getAcc(), cg.getFmeas_wgt(), cg.getAcc_wgt() ));
            int j = 0;
            for (double[] doubles : probs.get(it)) {
                System.arraycopy( doubles,0, tmp[j],M-1, M);
                j++;
            }
            arr[i] = new Matrix( tmp );
            i++;
        }
        for (i = 0; i < arr.length; i++) arr[i].arrayTimesEquals(wghts);

        Matrix aux = arr[0];
        for (i = 1; i < arr.length; i++) {aux.plus( arr[i] );}
        return Arrays.stream(aux.getArray())
                .map(ClassPred::maxFrom)
                .collect(Collectors.toList());
    }

    static List<Short> majorityVoting( Map<ActorRef,List<double[]>> probs )
    {
        int N = ((List<double[]>) probs.values().toArray()[0]).size();
        List<Short> l = new ArrayList<>();
        Short[] tmp = new Short[ probs.keySet().size() ];
        int ctr = 0;

        for (int i = 0; i < N; i++, ctr = 0)
        {
            for (List<double[]> it : probs.values())
            {
                tmp[ ctr ] = maxFrom( it.get( ctr ) );
                ctr++;
            }
            l.add( getMode( tmp ) );
        }
        return l;
    }

    public static Short maxFrom(double[] arr)
    {
        return (short) Arrays.stream(arr).max().orElse(0);
    }

    public static Short getMode(Short[] arr)
    {
        aux_ctr.clear();
        for (Short el : arr) {
            if (!aux_ctr.containsKey(el)) {
                aux_ctr.put(el, 1);
            } else {
                aux_ctr.put(el, aux_ctr.get(el) + 1);
            }
        }
        return findMode();
    }

    private static Short findMode()
    {
        Short max_idx = -1;
        Integer max = -1;
        for( Map.Entry<Short,Integer> it : aux_ctr.entrySet())
        {
            if( max < it.getValue() )
            {
                max_idx = it.getKey();
                max = it.getValue();
            }
        }
        return max_idx;
    }
}
