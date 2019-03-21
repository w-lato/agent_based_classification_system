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
    private final List<String> class_order;
    private final Map<String,List<double[]>> probs;
    private final List<StringBuilder> results;

    public Map<String, List<double[]>> getProbs() {
        return probs;
    }

    public ResultsHolder(Integer ID, ClassStrat strat)
    {
        this.ID = ID;
        this.strat = strat;
        class_order = new ArrayList<>();
        probs = new HashMap<>();
        results = new ArrayList<>(); // the final outcome
    }

    public void appendPredsAndProbs(PartialRes pr, String model_id, Map<String, ClassGrade> perf)
    {
        if( !results.contains( model_id ) )
        {
            class_order.add( model_id );
            probs.put( model_id, pr.getCr().getProbs() );
            calculatePreds( strat, perf );
        }
        else
        {
            System.out.println(" ###  TWO RESPONSES FROM SLAVE: " + model_id);
        }
    }

    public void calculatePreds(ClassStrat strategy, Map<String, ClassGrade> perf)
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
