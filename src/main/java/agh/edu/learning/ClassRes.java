package agh.edu.learning;

import agh.edu.agents.Aggregator;
import agh.edu.agents.enums.S_Type;
import agh.edu.learning.custom.MLP;
import org.jetbrains.annotations.NotNull;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ClassRes implements Comparable<ClassRes>
{
    private double[] fscore;
    private double[] AUROC;
    private double  acc;

    private final int N = 5;
    // tODO how weights will change over time
    private double acc_wgt = 0.5;
    private double fmeas_wgt = 0.5;

    private List<Prediction> preds;
    private List<double[]> probs;

    public double getSumOfFscore() {
        double acc = 0.0;
        for (double v : fscore) {
            acc += v;
        }
        return acc;
    }

    public List<Prediction> getPreds() { return preds; }

    public List<double[]> getProbs() { return probs; }

    public double[] getAUROC() { return AUROC; }

    public double[] getFscore() {
        return fscore;
    }

    public double getAcc() {
        return acc;
    }

    public double getAcc_wgt() { return acc_wgt; }

    public double getFmeas_wgt() { return fmeas_wgt; }

    public ClassRes(S_Type type, Classifier model, Instances data) throws Exception
    {
        Evaluation eval = new Evaluation(data);
        if( type.equals(S_Type.MLP) ) handleMLP(((MLP) model), data);
        else {
            // TODO what about seed?
            eval.crossValidateModel(model,data, N, new Random(1));
            int siz = data.numClasses();
            fscore = new double[ siz ];
            AUROC = new double[ siz ];
            for (int i = 0; i < siz; i++)
            {
                fscore[i] = eval.fMeasure( i );
                AUROC[i] = eval.areaUnderROC( i );
            }
            acc = eval.correct() / data.size();
            acc = Math.round( acc * 100.0 );

            setupProbsAndPreds( eval, data, model );
            checkValues();
        }
    }

    private void handleMLP(MLP mlp, Instances data) throws Exception {
        data.stratify( N );
        AUROC = new double[ data.numClasses() ];
        fscore = new double[ data.numClasses() ];
        acc = 0.0;

        for (int i = 0; i < N; i++)
        {
            Instances in = data.trainCV(N,i);
            MLP n_mlp  = new MLP(mlp.getLayerWiseConfigurations(), mlp.getNum_of_iter()) ;

            n_mlp.buildClassifier( in );
            Instances to_test = data.testCV(N, i);
            Evaluation tmp = new Evaluation( to_test );
            tmp.evaluateModel( n_mlp, to_test );

            for (int j = 0; j < data.numClasses(); j++)
            {
                fscore[j] += tmp.fMeasure( j );
                AUROC[j] += tmp.areaUnderROC( j );// ?
            }
            acc += tmp.correct() / to_test.size();
//            System.out.println(  "\t" + tmp.correct() + "\t" +  tmp.incorrect());
        }

        // calc simple mean
        for (int i = 0; i < data.numClasses(); i++)
        {
            fscore[i] /=  N;
            AUROC[i] /=  N;
        }
        acc /= N;
        acc = Math.round( acc * 100.0 );

        Evaluation eval = new Evaluation( data );
        setupProbsAndPreds( eval, data, mlp );
        checkValues();
//        System.out.println("ACC: " + acc);
    }

    // eval.getPrecision() and others can result in NaN
    private void checkValues()
    {
        if( Double.isNaN( acc )) acc = 0.0;
        for (int i = 0; i < fscore.length; i++) {
            if( Double.isNaN( fscore[i] )) fscore[i] = 0.0;
            if( Double.isNaN( AUROC[i] )) AUROC[i] = 0.0;
        }
    }

    private void setupProbsAndPreds(Evaluation eval, Instances data, Classifier model)
    {
        this.preds = eval.predictions();
        probs = new ArrayList<>();
        for (Instance datum : data) {
            try {
                this.probs.add(model.distributionForInstance(datum));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int compareTo(@NotNull ClassRes o)
    {
        if( this == o ) return 0;

        double A = 0.0;
        double B = 0.0;
        for (int i = 0; i < this.fscore.length; i++)
        {
            A += this.fscore[i];
            B += o.fscore[i];
        }
        A = A * fmeas_wgt  + acc * acc_wgt;
        B = B * fmeas_wgt  + o.acc * acc_wgt;

        return Double.compare( A,B );
    }

    public static double computeWeight(double[] fscore, double acc, double fmeas_wgt, double acc_wgt)
    {
        double tmp = 0.0;
        for (double v : fscore) {
            tmp += v;
        }
        return tmp * fmeas_wgt + acc * acc_wgt;
    }

    public static double computeWeight(ClassRes cr)
    {
        return computeWeight(
                cr.getFscore(),
                cr.getAcc(),
                cr.getFmeas_wgt(),
                cr.getAcc_wgt()
            );
    }

    public static double computeWeight(Aggregator.ClassGrade cr)
    {
        return computeWeight(
                cr.getFscore(),
                cr.getAcc(),
                cr.getFmeas_wgt(),
                cr.getAcc_wgt()
        );
    }

    public static int compare(@NotNull ClassRes a, ClassRes b)
    {
        double A = computeWeight( a.getFscore(), a.getAcc(), a.getFmeas_wgt(), a.getAcc() );
        double B = computeWeight( b.getFscore(), b.getAcc(), b.getFmeas_wgt(), b.getAcc() );
        return Double.compare(A,B);
    }

}
