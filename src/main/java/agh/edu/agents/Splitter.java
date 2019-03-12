package agh.edu.agents;

import agh.edu.learning.DataSplitter;
import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import org.opencv.core.Mat;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// TODO stratify the data when splitting
public class Splitter// extends AbstractActor
{
    private List<Instances> train;
    private Instances eval;

    private void onSplit( RunConf c )
    {
        // split data into N parts
        int N = c.agents.length;
        List<Instances> x = DataSplitter.splitIntoTrainAndTest( c.train, c.split_ratio );
        train = DataSplitter.split( x.get(0), N, c.split_meth, c.ol_ratio );
        this.eval = x.get(1);

        //
    }


    public static List<Instances> OLsplit(Instances data, double OL, int n) throws IOException {
       data.stratify( n );
       List<Instances> l = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            l.add( data.testCV( n, i) );
        }
        int x_split = (int) (1.0 / OL);
        for (int i = 1; i < n; i++) {
            Instances prev = l.get( i - 1 );
            prev.stratify( x_split );
            l.get( i ).addAll( prev.testCV( x_split,0 ) );
//            System.out.println( l.get(i).size() );
        }
        return l;
//        String s1 = l.get(0).toString();
//        Files.write(Paths.get("DATA/a.txt"), s1.getBytes());
//        String s2 = l.get(1).toString();
//        Files.write(Paths.get("DATA/b.txt"), s2.getBytes());
//        System.out.println( data.size() );
    }

    public static List<Instances> equalSplit(Instances data, int n) throws IOException {
        data.stratify( n );
        List<Instances> l = new ArrayList<>();
        for (int i = 0; i < n; i++)
        {
            l.add( data.testCV( n, i) );
        }
        return l;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //                                    MESSAGES




//    @Override
//    public Receive createReceive()
//    {
//        return receiveBuilder()
//                .match(  RunConf.class, this::onSplit)
//                .matchAny(o -> { System.out.println("Master received unknown message: " + o); })
//                .build();
//    }

    public static void main(String[] args) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\mnist_train.arff");
        Instances data = source.getDataSet();
        data.randomize( new Random(1));
        data.setClassIndex( data.numAttributes() - 1);

        Splitter s = new Splitter();;
        OLsplit( data.testCV(10,0), 0.15, 10 );
    }
}
