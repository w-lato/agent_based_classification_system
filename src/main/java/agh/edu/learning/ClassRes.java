package agh.edu.learning;

import weka.core.Instances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClassRes
{
    private double acc;
    private double F1;
    private double FPR;
    private double TPR;

    private Map<Double,Double> class_TPR;
    private Map<Double,Double> class_FPR;

    private Map<Double,Double> class_ctr;
    private Map<Double,Double> class_TP;
    private Map<Double,Double> class_TN;
    private Map<Double,Double> class_FP;
    private Map<Double,Double> class_FN;

    private Instances data;
    private List<Double> preds;
    private List<double[]> probs;

    public ClassRes(Instances data, List<Double> classes, List<double[]> probs)
    {
        this.data = data;
        this.preds = classes;
        this.probs = probs;

        countClasses();
        calcConfMat();
        calcTPR();
        calcFPR();
        calcAcc();
        calcF1();
    }

    private void calcAcc()
    {
        double ctr = 0;
        for (int i = 0; i < data.size(); i++)
        {
            double curr_class = data.get( i ).classValue();
            if( Double.compare(curr_class, preds.get(i)) ==  0) ctr++;
        }
        acc = ctr / ((double) data.size());
    }

    private void countClasses()
    {
        class_ctr = new HashMap<>();
        data.forEach(
                x -> {
                    if( !class_ctr.containsKey( x ) )
                        class_ctr.put( x.classValue(), 1.0 );
                    else
                        class_ctr.put( x.classValue(), class_ctr.get(x) + 1 );
                }
        );
    }

    private void calcConfMat()
    {
        class_TP = new HashMap<>( );
        class_TN = new HashMap<>( );
        class_FP = new HashMap<>( );
        class_FN = new HashMap<>( );

        for (int i = 0; i < data.size(); i++) {
            double curr_class = data.get(i).classValue();

            if( Double.compare(preds.get(i), curr_class) == 0)
            {
                if( !class_TP.containsKey( curr_class ) ) class_TP.put( curr_class, 1.0 );
                else class_TP.put( curr_class, class_TP.get(curr_class) + 1.0 );
            }
            else
            {
                if( !class_FP.containsKey( curr_class ) ) class_FP.put( preds.get(i), 1.0 );
                else class_FP.put( curr_class, class_FP.get(preds.get(i)) + 1.0 );

                if( !class_FN.containsKey( curr_class ) ) class_FN.put( curr_class, 1.0 );
                else class_FN.put( curr_class, class_FN.get( curr_class ) + 1.0 );
            }
        }
        for( double it : class_TP.keySet())
        {
            class_TN.put( it, data.size() - class_TP.get(it) - class_FP.get(it) - class_FN.get(it) );
        }
    }

    private void calcFPR()
    {
        for( double it : class_FP.keySet())
        {
            class_FPR.put( it, class_FP.get(it) / ( class_FP.get(it) + class_TN.get(it) ) );
        }
        FPR = 0.0;
        for( double it : class_FPR.keySet())
        {
            FPR += class_FPR.get(it) / ((double) class_ctr.get(it) / data.size());
        }
    }

    private void calcTPR()
    {
        for( double it : class_TP.keySet())
        {
            class_FPR.put( it, class_TP.get(it) / ( class_TP.get(it) + class_FP.get(it) ) );
        }
        TPR = 0.0;
        for( double it : class_TPR.keySet())
        {
            TPR += class_TPR.get(it) / ((double) class_ctr.get(it) / data.size());
        }
    }

    private void calcF1()
    {
        F1 = 2.0 * ( TPR * FPR ) / ( TPR + FPR);
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    //
    //                                            GETTERS


    public double getAcc() {
        return acc;
    }

    public double getF1() {
        return F1;
    }

    public double getFPR() {
        return FPR;
    }

    public double getTPR() {
        return TPR;
    }

    public Instances getData() {
        return data;
    }

    public List<Double> getPreds() {
        return preds;
    }

    public List<double[]> getProbs() {
        return probs;
    }
}
