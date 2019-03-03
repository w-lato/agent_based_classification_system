package agh.edu.learning;

import org.jetbrains.annotations.NotNull;
import org.nd4j.evaluation.meta.Prediction;
import java.util.List;

public final class ClassRes implements Comparable<ClassRes>
{
    private final double acc_wgt = 0.5;
    private final double fmeas_wgt = 0.5;


    private final List<Prediction> preds;
    private final List<double[]> probs;

    public ClassRes(List<Prediction> preds, List<double[]> probs)
    {
        this.preds = preds;
        this.probs = probs;
    }

    public List<Prediction> getPreds()
    {
        return preds;
    }

    public List<double[]> getProbs()
    {
        return probs;
    }


    @Override
    public int compareTo(@NotNull ClassRes o)
    {
// tODO
        return 0;
    }
}
