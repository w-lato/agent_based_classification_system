package agh.edu.agents;

import akka.actor.ActorRef;
import weka.classifiers.evaluation.Prediction;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClassChooser
{
    public static final int MAJORITY_VOTE = 0;
    public static final int WEIGHTED_VOTE = 1;
    public static final int AVERAGE = 2;

    private Map<ActorRef, Double> weights;
    private int CURRENT_STRAT;

    public void setWeights(Map<ActorRef, List<Prediction>> results)
    {
        weights = new HashMap<>();
        results.forEach((k,v) ->
        {
            double wgt = 0.0;
            for (int i = 0; i < v.size(); i++)
            {
                Prediction act = v.get(i);
                if( act.predicted() == act.actual() ) wgt++;
            }
            weights.put( k, wgt / ((double) v.size()));
        });

        // normalize - div by number of agents
        weights.forEach((k,v)->{
            weights.put( k, v / ((double) weights.size()));
        });
    }

    // TODO CHANGE WEIGHTS
    public void deleteActor(ActorRef id)
    {
        if( weights != null ) weights.remove( id );
    }

    public List<Integer> chooseClasses(Map<ActorRef, List<Prediction>> results, int method)
    {
        return IntStream.range( 0, results.entrySet().iterator().next().getValue().size() )
            .parallel()
            .mapToObj(i -> {
                Map<ActorRef,Prediction> x = new HashMap<>();
                results.forEach((key, value) -> x.put(key, value.get(i)));
                return x;
            })
            .map(el -> chooseClass(el, method))
            .collect(Collectors.toList());
    }


    private static List<Integer> combineMyLists(List<Integer>... args) {
        List<Integer> combinedList = new ArrayList<>();
        for(List<Integer> list : args){
            for(Integer i: list){
                combinedList.add(i);
            }
        }
        return combinedList;
    }

    public int chooseClass(Map<ActorRef, Prediction> results, int method)
    {
        //setWeights( results );
        switch (method)
        {
            case MAJORITY_VOTE: return getMajorityVote( results );
            case WEIGHTED_VOTE: return getWeightedVote( results );
            case AVERAGE:       return getAverage( results );
            default: return -1;
        }
    }

    // HARD VOTING
    private int getMajorityVote(Map<ActorRef, Prediction> results)
    {
        Map<Integer,Integer> ctr = new HashMap<>();
        results.forEach( (k,v) ->{
            int pred = (int) v.predicted();

            if( ctr.containsKey(pred ))
                ctr.put( pred, ctr.get(pred) + 1);
            else
                ctr.put( pred, 1);
        });

        return ctr.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .get().getKey();
    }

    // SOFT VOTING
    private int getWeightedVote(Map<ActorRef, Prediction> results)
    {
        Map<Integer,Double> ctr = new HashMap<>();
        results.forEach( (k,v) ->{
            int pred = (int) v.predicted();
            if( ctr.containsKey( pred ))
                ctr.put( pred, ctr.get(pred) + weights.get( k ));
            else
                ctr.put( pred, weights.get( k ));
        });

        return ctr.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .get().getKey();
    }

    private int getAverage(Map<ActorRef, Prediction> results)
    {
        return (int)Math.round(
                results.values().stream()
                        .mapToDouble(Prediction::predicted).sum()
                        / ((double) results.size())
        );
    }


    public static void main(String[] args)
    {
        Map<String,List<Integer>> m1 = new HashMap<>();
        m1.put("A",Arrays.asList(1,2,3,4));
        m1.put("B",Arrays.asList(0,0,0,0));
        m1.put("C",Arrays.asList(1,1,1,1));

//
//        m1.entrySet().parallelStream()
//                .map((k,v)->{
//
//                    Map<String,Integer> x = new new HashMap<>();
//                    return x.put( k,v );
//
//                })
//                .collect(Collectors.toList())
//                .forEach( x -> {
//                    System.out.println( x );
//                });

        m1.keySet().parallelStream()
                .map( x -> x + "_XD" )
                .collect(Collectors.toList())
                .forEach( x -> {
                    System.out.println( x );
                });

//        m1.values().parallelStream()
//                .map( v -> {
//                    for (int i = 0; i < v.size(); i++) {
//                        v.set(i, 10);
//                    }
//                    return v;
////                    v.parallelStream().forEach( z -> z = z + 10 )
//                } )
//                .collect(Collectors.toList())
//                .forEach( x -> {
//                    System.out.println( x );
//                });
//
//        m1.entrySet().parallelStream()
//                .collect(Collectors.toList())
//                .forEach( x -> {
//                    System.out.println( x );
//                });

        IntStream.range(0,m1.entrySet().iterator().next().getValue().size() )
        .parallel().mapToObj(i -> {
            Map<String,Integer> x = new HashMap<>();
            m1.forEach((key, value) -> x.put(key, value.get(i)));
            return x;
        }).collect(Collectors.toList()).forEach(System.out::println);

    }
}
