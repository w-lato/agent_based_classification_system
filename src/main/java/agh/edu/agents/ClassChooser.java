package agh.edu.agents;

import agh.edu.agents.enums.S_Type;
import agh.edu.agents.enums.Split;
import agh.edu.agents.enums.Vote;
import agh.edu.learning.DataSplitter;
import agh.edu.learning.WekaEval;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClassChooser
{
//    public static final int MAJORITY_VOTE = 0;
//    public static final int WEIGHTED_VOTE = 1;
//    public static final int AVERAGE = 2;

    private Map<ActorRef, Double> weights;
    private int CURRENT_STRAT;

    public void setWeights(Map<ActorRef,Double> weights)
    {
        this.weights = weights;
    }

    public void computeWeights(Map<ActorRef, List<Prediction>> results)
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

    public List<Integer> chooseClasses(Map<ActorRef, List<Prediction>> results, Vote method)
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

    public int chooseClass(Map<ActorRef, Prediction> results, Vote method)
    {
        switch (method)
        {
            case MAJORITY: return getMajorityVote( results );
            case WEIGHTED: return getWeightedVote( results );
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

    public static void main(String[] args) throws Exception {
//        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\spambase.arff");
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
        Instances rows = source.getDataSet();

        List<Instances> s = DataSplitter.splitIntoTrainAndTest( rows, 0.9 );
        List<Instances> d = DataSplitter.split( s.get(0),12 , Split.OVERLAP, 0.5 );
        List<WekaEval> l = new ArrayList<>();
        for (int i = 0; i < 6; i++)
        {
            l.add( new WekaEval(S_Type.SMO) );
        }
        for (int i = 6; i < 9; i++)
        {
            l.add( new WekaEval(S_Type.SMO) );
        }
        for (int i = 9; i < 12; i++)
        {
            l.add( new WekaEval(S_Type.SMO) );
        }
        IntStream.range(0,l.size()).parallel().forEach( x -> l.get(x).train(d.get(x)));


        // EVALUATE WITH TESTS DATA
        List<List<Prediction>> res = new ArrayList<>();
        for (int i = 0; i < l.size(); i++)
        {
            res.add( l.get(i).eval( s.get(1) ) );
        }

        ClassChooser cc = new ClassChooser();
//        cc.setWeights(  );
        Map<ActorRef, List<Prediction>> M = new HashMap<>();

        ActorSystem system = ActorSystem.create("testSystem");
        for (int i = 0; i < res.size(); i++) {
            M.put( system.actorOf( Master.props(false) ,"master" + i ),  res.get(i) );
        }
        cc.computeWeights( M );
        //

        // REAL TEST
        source = new ConverterUtils.DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_test.arff");
        rows = source.getDataSet();
        rows.setClassIndex( rows.numAttributes() - 1 );

        List<Integer> L = cc.chooseClasses( M, Vote.AVERAGE );
        int ctr = 0;
        for (int i = 0; i < L.size(); i++)
        {
            if( L.get(i) == res.get(0).get(i).actual()) ctr++;
            System.out.println( res.get(0).get(i).actual() + " "+ res.get(0).get(i).predicted() );
        }
        System.out.println( 100.0 * ((double) ctr) / L.size() );
    }
}
/**
 majowity vote - 0.93
 weighted - 0.9, 0.7 overlap - 95.21%
 majority - 0.9, 0.5 over - 95.099
 wighted - 0.9, 0.5 over - 95.13%
 average - 0.9, 0,5 over  - 87.88
 */