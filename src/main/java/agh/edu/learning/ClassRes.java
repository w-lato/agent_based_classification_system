package agh.edu.learning;

import agh.edu.agents.enums.S_Type;
import agh.edu.learning.custom.MLP;
import org.jetbrains.annotations.NotNull;
import org.nd4j.evaluation.meta.Prediction;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.supervised.instance.StratifiedRemoveFolds;
import weka.knowledgeflow.steps.TrainTestSplitMaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ClassRes implements Comparable<ClassRes>
{
    double[] fscore;
    double[] AUROC;
    double  acc;

    private final int N = 10;
    private final double acc_wgt = 0.5;
    private final double fmeas_wgt = 0.5;


//    private final List<Prediction> preds;
//    private final List<double[]> probs;

    public ClassRes(List<Prediction> preds, List<double[]> probs)
    {
//        this.preds = preds;
//        this.probs = probs;
    }

    public ClassRes(S_Type type, Classifier model, Instances data) throws Exception
    {
        Evaluation eval = new Evaluation(data);
        if( type.equals(S_Type.MLP) ) handleMLP(((MLP) model), data);
        else {
            eval.crossValidateModel(model,data, N, new Random(1));

        }
    }

//    public List<Prediction> getPreds()
//    {
//        return preds;
//    }
//
//    public List<double[]> getProbs()
//    {
//        return probs;
//    }


    private void handleMLP(MLP mlp, Instances data) throws Exception {
        List<Instances> list = new ArrayList<>();
        for (int i = 0; i < N; i++) list.add(data.testCV( N,i));

        TrainTestSplitMaker tsm = new TrainTestSplitMaker();

        // stratification weka methods
        data.stratify(N);

//srf.
        AUROC = new double[ data.numClasses() ];
        fscore = new double[ data.numClasses() ];

        //
        for (int i = 0; i < N; i++)
        {
            Instances in = null;
            for (int j = 0; j < N; j++)
            {
                if( j != i )
                {
                    if( in == null ) in = list.get(j);
                    else in.addAll( list.get(j) );
                }
            }
            Evaluation tmp = new Evaluation(in);
            tmp.evaluateModel( mlp, in );

            for (int j = 0; j < data.numClasses(); j++)
            {
                fscore[j] += tmp.fMeasure( j );
                AUROC[j] += tmp.areaUnderROC( j );// ?
                acc = tmp.correct();
            }
        }

        // zalezy od rozkladu danych - accuracy moze nie byc dobry
        // zmienny
        // calc simple mean
        for (int i = 0; i < data.numClasses(); i++)
        {
            fscore[i] += fscore[i] / N;
            AUROC[i] += AUROC[i] / N;
        }
        acc /= N;
    }

    @Override
    public int compareTo(@NotNull ClassRes o)
    {
// tODO
        return 0;
    }
}
