package agh.edu.agents;

import akka.actor.ActorRef;
import weka.classifiers.evaluation.Prediction;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void deleteActor(ActorRef id)
    {
        if( weights != null ) weights.remove( id );
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

}
