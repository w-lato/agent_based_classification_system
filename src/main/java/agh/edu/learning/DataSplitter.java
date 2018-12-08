package agh.edu.learning;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataSplitter
{

    public static List<Instances> divideEqual(Instances rows, int N)
    {
        rows.randomize(new Random(System.currentTimeMillis()));
        List<Instances> arr = new ArrayList<Instances>();
        int part_siz = rows.size() /  N;
        int first = 0;
        int S = rows.size();

        for (int i = 0; i < N; i++)
        {
            arr.add( new Instances(rows, first, part_siz) );
            first += part_siz;
            if( first + part_siz >= S )
            {
                part_siz = S - 1 - first;
                first = S -  part_siz;
            }
        }
        return arr;
    }


    public static List<Instances> overlapDivide(Instances rows, int n, double OL)
    {
        //rows.randomize(new Random(System.currentTimeMillis()));

        List<Instances> arr = new ArrayList<Instances>();
        int part_siz = ((int) (rows.size() / (n * (1 - OL) + OL)));
        int interval = ((int) ((1 - OL) * part_siz)) + 1;
        int first = 0;
        int S = rows.size();

        for (int i = 0; i < n; i++)
        {
            if( (i == (n - 1)) && ((first + part_siz) < (S - 1)))
            {
                System.out.println("&&&&&&&&&&&&&");
                part_siz = S - first;
            }
            System.out.println(i+  " " + first + " " + part_siz + " " + ((first + part_siz) < (S - 1)));
            arr.add( new Instances(rows, first, part_siz) );
            first += interval;
            if( first + part_siz >= S )
            {
                System.out.println("$$$$");
                part_siz = S - first;
                first = S -  part_siz;
            }
        }
        return arr;
    }


    public static void printRowsList( List<Instances>  arr)
    {
        for (int i = 0; i < arr.size(); i++)
        {
            System.out.println( i + ": siz: " + arr.get( i ).size() + "  class: " + arr.get(i).classIndex() );
            for (int j = 0; j < arr.get( i ).size(); j++)
            {
//                System.out.println( "\t" + arr.get( i ).get(j) );
                System.out.println(arr.get( i ).get(j) );
            }
        }
    }
    public static void main(String[] args) throws Exception {
//        DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\spambase.arff");
        DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\iris.arff");

        Instances rows = source.getDataSet();
        rows.setClassIndex( rows.numAttributes() - 1 );
        System.out.println( rows );


        System.out.println("========================================================");
        System.out.println("========================================================");
        System.out.println("========================================================");

        List<Instances> l;// = divideEqual( rows, 15 );
//        printRowsList( l );

        System.out.println("========================================================");
        System.out.println("========================================================");
        System.out.println("========================================================");

        l = overlapDivide( rows, 10, 0.9 );
        printRowsList( l );



//        System.out.println(  rows );
    }
}
