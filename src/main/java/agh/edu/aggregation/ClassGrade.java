package agh.edu.aggregation;

import agh.edu.learning.ClassRes;

import java.util.Arrays;

public final class ClassGrade
{
    private String model_id;

    private final double[] fscore;
    private final double[] AUROC;
    private final double acc;
    private final double acc_wgt;
    private final double fmeas_wgt;
    private final double grade;

    public double[] getFscore() { return fscore; }
    public double[] getAUROC() { return AUROC; }
    public double getAcc() { return acc; }
    public double getAcc_wgt() { return acc_wgt; }
    public double getFmeas_wgt() { return fmeas_wgt; }
    public double getGrade() { return grade; }
    public String getModel_id() { return model_id; }

    public ClassGrade(double[] fscore, double[] AUROC, double acc, double acc_wgt, double fmeas_wgt, double grade)
    {
        int N = fscore.length;
        this.fscore = new double[ N ];
        this.AUROC = new double[ N ];
        this.acc_wgt = acc_wgt;
        this.fmeas_wgt = fmeas_wgt;

        this.acc = acc;
        for (int i = 0; i < N; i++) {
            this.fscore[ i ] = fscore[ i ];
            this.AUROC[ i ] = AUROC[ i ];
        }
        this.grade = grade;
    }

    public ClassGrade(ClassRes cr, String model_id)
    {
        this( cr.getFscore(), cr.getAUROC(), cr.getAcc(), cr.getAcc_wgt(),cr.getFmeas_wgt(), cr.getGrade());
        this.model_id = model_id;
    }

    @Override
    public String toString()
    {
        return  grade + ":" +
                acc_wgt + ":" +
                fmeas_wgt + ":" +
                acc + ":" +
                Arrays.toString(fscore) + ":" +
                Arrays.toString(AUROC);
    }

    public static ClassGrade fromString(String s)
    {
        String[] arr = s.split(":");
        double grade = Double.valueOf( arr[0] );
        double acc_wgt = Double.valueOf( arr[1] );
        double fmeas_wgt = Double.valueOf( arr[2] );
        double acc = Double.valueOf( arr[3] );

        String[] f1 = arr[4].replace("[", "").replace("]", "").split(",");
        String[] au = arr[5].replace("[", "").replace("]", "").split(",");
        double[] fscore = new double[f1.length];
        double[] auroc = new double[au.length];
        for (int i = 0; i < f1.length; i++)
        {
            fscore[i] = Double.valueOf(f1[i]);
            auroc[i] = Double.valueOf(au[i]);
        }
        return new ClassGrade( fscore, auroc, acc, acc_wgt, fmeas_wgt, grade );
    }
}
