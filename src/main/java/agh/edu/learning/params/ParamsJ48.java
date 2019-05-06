package agh.edu.learning.params;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;

import java.util.ArrayList;
import java.util.List;

public class ParamsJ48 implements Params
{
    @Override
    public Classifier clasFromStr(String params)
    {
        String[] p = params.split(",");
        int num_of_folds = Integer.valueOf( p[0] );
        int num_of_objects = Integer.valueOf( p[1] );
        boolean bin_splits_only = Boolean.valueOf( p[2] );
        boolean set_reduced_err_prun = Boolean.valueOf( p[3] );
        boolean mdl_uncorrelated = Boolean.valueOf( p[4] );
        boolean laplace_smooth = Boolean.valueOf( p[5] );
        float conf_thresh = Float.valueOf( p[6] );

        J48 j48 = new J48();
        j48.setNumFolds( num_of_folds );
        j48.setMinNumObj( num_of_objects );
        j48.setBinarySplits( bin_splits_only );
        j48.setReducedErrorPruning( set_reduced_err_prun );
        j48.setUseMDLcorrection( mdl_uncorrelated );
        j48.setUseLaplace( laplace_smooth );
        j48.setConfidenceFactor( conf_thresh );
        return j48;
    }

    @Override
    public List<String> getParamsCartProd()
    {
        int[] num_of_folds = new int[]{5,7,9,11}; //default 3
        int[] num_of_objects = new int[]{1,4,6,8,10}; //default 2
        boolean[] bin_splits_only = new boolean[]{true, false};
        boolean[] set_reduced_err_prun = new boolean[]{true, false};
        boolean[] mdl_uncorrelated = new boolean[]{true, false};

        boolean[] laplace_smooth = new boolean[]{true, false};
        float[] conf_thresh = new float[]{0.05f,0.15f,0.35f}; // def 0.25

        List<String> confs = new ArrayList<>();
        for (int i = 0; i < num_of_folds.length; i++) {
            for (int i1 = 0; i1 < num_of_objects.length; i1++) {
                for (int i2 = 0; i2 < bin_splits_only.length; i2++) {
                    for (int i3 = 0; i3 < set_reduced_err_prun.length; i3++) {
                        for (int i4 = 0; i4 < mdl_uncorrelated.length; i4++) {
                            for (int i5 = 0; i5 < laplace_smooth.length; i5++) {
                                for (int i6 = 0; i6 < conf_thresh.length; i6++) {
                                    confs.add( num_of_folds[i] + "," + num_of_objects[i1] +","
                                            + bin_splits_only[i2] +"," +set_reduced_err_prun[i3]
                                            +","+mdl_uncorrelated[i4] +"," +laplace_smooth[i5] +","+
                                            conf_thresh[i6]
                                    );
                                }
                            }

                        }
                    }
                }
            }
        }
        return confs;
    }
}
