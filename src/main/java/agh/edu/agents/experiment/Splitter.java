package agh.edu.agents.experiment;


import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Splitter
{
    private static Random r = new Random(1);

    public static List<Instances> splitOnTestAndTrain(Instances data, int percent_of_test)
    {
        int folds = 100 / percent_of_test; // usually 10,20,30
        prepareInst( data, folds );
        List<Instances> l = new ArrayList<>();
        l.add( data.trainCV( folds, 0 ) );
        l.add( data.testCV( folds, 0 ) );
        return l;
    }

    public static List<Instances> OLsplit(Instances data, double OL, int n) throws IOException
    {
        prepareInst( data,n );
        List<Instances> l = new ArrayList<>();
        int P = 100;
        int percent_num = (int)(OL * P);

        int x;
        if( (n*percent_num) > 1.0 ) x = ((int) (((1.0 - OL) * P) / (n - 1)));
        else x = percent_num;

        data.stratify( P );
        for (int i = 0; i < n; i++)
        {
            int curr = i * x;
            l.add( data.testCV(P, curr) );
            for (int j = curr + 1; j < curr + percent_num && j < P; j++)
            {
                l.get(i).addAll( data.testCV( P, j) );
            }
        }
        return l;
    }

    public static List<Instances> equalSplit(Instances data, int n) throws IOException {
        prepareInst( data,n );
        List<Instances> l = new ArrayList<>();
        for (int i = 0; i < n; i++){ l.add( data.testCV( n, i) ); }
        return l;
    }


    private static void prepareInst( Instances data, int n )
    {
        data.randomize( r );
        data.setClassIndex( data.numAttributes() - 1 );
        data.stratify( n );
    }
}
