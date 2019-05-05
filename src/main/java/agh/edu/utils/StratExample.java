package agh.edu.utils;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StratExample
{
    public static void main(String[] args) throws Exception
    {
        Instances mnist = new ConverterUtils.DataSource("DATA/mnist_train.arff").getDataSet();
        mnist.setClassIndex( mnist.numAttributes() - 1 );

        Map<Integer,Integer> ctr = new HashMap<>();
        for (int i = 0; i < 10; i++)
        {
            ctr.put( i, 0 );
        }
        mnist.forEach( x->{
            int cls = (int)x.classValue();
            ctr.put( (int)cls, ctr.get(cls) + 1 );
        } );

        ctr.forEach((k,v)->{
            System.out.println( k + "\t" + v );
        });
        System.out.println("================================== " + mnist.size());
        mnist.stratify(5);
        Instances  one_fold = mnist.trainCV(5,0);
        for (int i = 0; i < 10; i++)
        {
            ctr.put( i, 0 );
        }
        one_fold.forEach( x->{
            int cls = (int)x.classValue();
            ctr.put( (int)cls, ctr.get(cls) + 1 );
        } );
        ctr.forEach((k,v)->{
            System.out.println( k + "\t" + v );
        });
        System.out.println("================================== " + one_fold.size());
    }
}
