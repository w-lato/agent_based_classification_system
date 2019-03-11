package agh.edu.aggregation;

import agh.edu.agents.Aggregator.PartialRes;
import agh.edu.agents.Aggregator.ClassGrade;
import agh.edu.agents.enums.ClassStrat;
import akka.actor.ActorRef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


// TODO some kind of identifiers to ActorRef... probably similar to save files
public class ResultsHolder
{
    private final Integer ID;
    private final ClassStrat strat;
    // TODO replace this with List<ActorRef> because it will be hart to convert it from string to ref
    private final StringBuilder class_order;
    private final Map<ActorRef,List<double[]>> probs;
    private final List<StringBuilder> results;

    public ResultsHolder(Integer ID, ClassStrat strat)
    {
        this.ID = ID;
        this.strat = strat;
        class_order = new StringBuilder();
        probs = new HashMap<>();
        results = new ArrayList<>(); // the final outcome
    }

    public void appendPredsAndProbs(PartialRes pr, ActorRef ref, Map<ActorRef, ClassGrade> perf)
    {
        if( !results.contains( ref ) )
        {
            class_order.append(ref).append(',');
            class_order.append( ref ).append(',');
            probs.put( ref, pr.getCr().getProbs() );
            calculatePreds( strat, perf );
        }
        else
        {
            System.out.println(" ###  TWO RESPONSES FROM SLAVE: " + ref);
        }
    }

    public void calculatePreds(ClassStrat strategy, Map<ActorRef, ClassGrade> perf)
    {
        List<String> l = ClassPred.getPreds( strategy,perf, probs)
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toList());

        if( results.isEmpty() )
        {
            l.forEach( x -> results.add(new StringBuilder(x)) );
        }
        else
        {
            for (int i = 0; i < results.size(); i++)
            {
                results.get(i).append(l.get(i));
            }
        }
    }

    private String arrToStr(double[] arr)
    {
        StringBuilder s = new StringBuilder();
        for (double v : arr) {
            s.append(v).append(',');
        }
        return s.toString();
    }
}
