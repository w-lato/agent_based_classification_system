package agh.edu.agents.experiment;


import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Splitter
{
    private static Random r = new Random(1);

    public static List<Instances> splitOnTestAndTrain(Instances data, int percent_of_test)
    {
        int folds = 100 / percent_of_test; // usually 10,20,30
        data.randomize( r );
        data.stratify( folds );
        List<Instances> l = new ArrayList<>();
        l.add( data.trainCV( folds, 0 ) );
        l.add( data.testCV( folds, 0 ) );
        return l;
    }

    public static List<Instances> OLsplit(Instances data, double OL, int n) throws IOException {
        data.randomize( r );
        data.stratify( n );
        List<Instances> l = new ArrayList<>();
        for (int i = 0; i < n; i++) { l.add( data.testCV( n, i) ); }
        int x_split = (int) (1.0 / OL);
        for (int i = 1; i < n; i++) {
            Instances prev = l.get( i - 1 );
            prev.stratify( x_split );
            l.get( i ).addAll( prev.testCV( x_split,0 ) );
        }
        return l;
    }

    public static List<Instances> equalSplit(Instances data, int n) throws IOException {
        data.randomize( r );
        data.stratify( n );
        List<Instances> l = new ArrayList<>();
        for (int i = 0; i < n; i++){ l.add( data.testCV( n, i) ); }
        return l;
    }

    public static List<Instances> fillSplit(Instances data, int n, double F) throws IOException
    {
        if( (1.0 / F) > n ) return null;
        data.randomize( r );
        List<Instances> l = new ArrayList<>();
        int x_in_part = (int) ( (F * (n - 1)) / (1.0 - F));
        int folds = (n-1) + x_in_part;

//        int folds = ((int) ((n - 1) / (1 - F))); // TODO to test
        System.out.println( folds + "  " + x_in_part );
        data.stratify( folds );

        for (int i = 0; i < n; i++)
        {
            Instances to_add = data.testCV( folds, i );
            for (int j = i + 1; j < x_in_part + i; j++)
            {
                to_add.addAll( data.testCV( folds, j ) );
            }
            l.add( new Instances(to_add) );
//            System.out.println("\t" + to_add.size());
        }

//        String s1 = l.get(0).toString();
//        Files.write(Paths.get("DATA/a.txt"), s1.getBytes());
//        String s2 = l.get(1).toString();
//        Files.write(Paths.get("DATA/b.txt"), s2.getBytes());
//        System.out.println( data.size() );
        return l;
    }



    public static void main(String[] args) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\mnist_train.arff");
        Instances data = source.getDataSet();
        data.randomize( new Random(1));
        data.setClassIndex( data.numAttributes() - 1);

        Splitter s = new Splitter();;
        fillSplit( data.testCV(10,0), 010, 0.2 );
    }
}
