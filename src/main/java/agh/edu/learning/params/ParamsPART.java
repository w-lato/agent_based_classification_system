package agh.edu.learning.params;

import weka.classifiers.Classifier;
import weka.classifiers.rules.PART;

import java.util.ArrayList;
import java.util.List;

public class ParamsPART implements Params
{
    @Override
    public Classifier clasFromStr(String params)
    {
        String[] p = params.split(",");
        int num_of_folds = Integer.valueOf( p[0] );
        int num_of_objects = Integer.valueOf( p[1] );
        boolean bin_splits_only = Boolean.valueOf( p[2] );
        boolean set_reduced_err_prun = Boolean.valueOf( p[3] );
        boolean set_unpruned = Boolean.valueOf( p[4] );
        boolean mdl_uncorrelated = Boolean.valueOf( p[5] );

        PART part = new PART();
        part.setNumFolds( num_of_folds );
        part.setMinNumObj( num_of_objects );
        part.setBinarySplits( bin_splits_only );
        part.setReducedErrorPruning( set_reduced_err_prun );
        part.setUnpruned( set_unpruned );
        part.setUseMDLcorrection( mdl_uncorrelated );
        return part;
    }

    @Override
    public List<String> getParamsCartProd()
    {
        int[] num_of_folds = new int[]{5,7,9,11}; //default 3
        int[] num_of_objects = new int[]{1,4,6,8,10}; //default 2
        boolean[] bin_splits_only = new boolean[]{true, false};
        boolean[] set_reduced_err_prun = new boolean[]{true, false};
        boolean[] set_unpruned = new boolean[]{true, false};
        boolean[] mdl_uncorrelated = new boolean[]{true, false};

        List<String> confs = new ArrayList<>();
        for (int i = 0; i < num_of_folds.length; i++) {
            for (int i1 = 0; i1 < num_of_objects.length; i1++) {
                for (int i2 = 0; i2 < bin_splits_only.length; i2++) {
                    for (int i3 = 0; i3 < set_reduced_err_prun.length; i3++) {
                        for (int i4 = 0; i4 < set_unpruned.length; i4++) {
                            for (int i5 = 0; i5 < mdl_uncorrelated.length; i5++) {
                                confs.add( num_of_folds[i] + "," + num_of_objects[i1] +","
                                        + bin_splits_only[i2] +"," +set_reduced_err_prun[i3]
                                        +","+set_unpruned[i4] +"," +mdl_uncorrelated[i5]);
                            }
                        }
                    }
                }
            }
        }
        return confs;
    }
}
