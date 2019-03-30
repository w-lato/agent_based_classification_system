package agh.edu.aggregation;

import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.experiment.Saver;
import weka.core.matrix.Matrix;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClassPred
{
    private static Map<Integer,Integer> aux_ctr= new HashMap<>();

    public static List<Integer> getPreds(ClassStrat s, Map<String, ClassGrade> perf,
                                         LinkedHashMap<String,List<double[]>> probs) {
        switch (s)
        {
            case MAJORITY: return majorityVoting( probs );
            case WEIGHTED: return soft_v1(perf,probs);
            case PROB_WEIGHT: return soft_prob(perf,probs);
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
            to_save = aux.stream().map(StringBuilder::toString)
//                    .peek(System.out::println)
                    .collect(Collectors.toList());
            to_save.add(0, String.join(":", keys));
            Saver.saveAggPredictions( exp_id, query_id, strat, to_save );
        }
    }


    static List<Integer> soft_v1(Map<String, ClassGrade> perf, LinkedHashMap<String,List<double[]>> probs )
    {
        SoftVotingVariations svv = new SoftVotingVariations( perf, probs );
        List<Integer> res = svv.getSoftVoteRes();
        svv = null;
        return res;
    }


    static List<Integer> soft_prob(Map<String, ClassGrade> perf, LinkedHashMap<String,List<double[]>> probs )
    {
        SoftVotingVariations svv = new SoftVotingVariations( perf, probs );
        List<Integer> res = svv.getProbSoft();
        svv = null;
        return res;
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

    private static class SoftVotingVariations
    {
        Set<String> ids;
        int rows;
        int cols;
        List<Integer> l;
        int num_of_classes;
        double[] norm_wghts;

        LinkedHashMap<String,List<double[]>> probs;
        Map<String, ClassGrade> perf;

        public SoftVotingVariations( Map<String, ClassGrade> perf, LinkedHashMap<String,List<double[]>> probs )
        {
            this.probs = probs;
            this.perf = perf;

            ids = probs.keySet();
            rows = probs.get( ids.iterator().next() ).size();
            cols = ids.size();
            l = new ArrayList<>();
            num_of_classes = probs.get( ids.iterator().next()).get(0).length;
            norm_wghts = normWeights(cols, ids, perf);
        }


        private double[] normWeights(int cols,Set<String> ids, Map<String, ClassGrade> perf)
        {
            double[] norm_wghts = new double[cols];
            double sum = 0.0;
            for (String id : ids) { sum += perf.get( id ).getGrade(); }
            int xd = 0;
            for (String id : ids)
            {
                norm_wghts[xd] = perf.get(id).getGrade() / sum;
                xd++;
            }
            return norm_wghts;
        }

        public List<Integer> getSoftVoteRes()
        {
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

        // probabilities are multiplied by wights and ummed up
        public List<Integer> getProbSoft()
        {
            double[] aux = new double[cols];
            return IntStream.range(0,rows)
                    .map( x -> {
                        int ctr = 0;
                        Arrays.fill( aux,0.0 );

                        // iterate over cols
                        for (String id : ids)
                        {
                            // mul wght * prob and then add everything
                            double[] arr = probs.get(id).get(x);
                            for (int i = 0; i < arr.length; i++)
                            {
                                aux[i] += arr[i] * norm_wghts[ctr];
                            }
                            ctr++;
                        }
                        return maxIdxFrom( aux );
                    })
                    .boxed()
                    .collect(Collectors.toList());
        }
    }

}
