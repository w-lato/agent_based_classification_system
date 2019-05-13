package agh.edu.utils;

import agh.edu.agents.Master;
import agh.edu.agents.experiment.ConfParser;
import agh.edu.agents.experiment.RunConf;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.apache.mahout.classifier.df.data.DataUtils;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.nio.file.Files;

// TODO entire test mnist data set gets stuck when using 128 SMOs probably because of
//      large amounts of memory needed to store all data...
public class RUN_EXP
{
    public static void main(String[] args) throws Exception {
        RunConf rc = ConfParser.getConfFrom( "CONF/MAZOWIECKIE_8_SMO" );
        ActorSystem system = ActorSystem.create("test_sys");
        ActorRef m = system.actorOf( Master.props() ,"master" );
        System.out.println("START AT: " + System.currentTimeMillis());

//        m.tell( new Master.LoadExp("EXP/5_SMO_2_J48_5_PART_MNIST_10_19"), ActorRef.noSender());
//        m.tell( rc, ActorRef.noSender() );
//        m.tell( new Master.LoadExp("EXP/FIRST_DATA_LIMITS_14"), ActorRef.noSender());



        m.tell( new Master.SlaveOnlyExp("EXP/MAZOWIECKIE_8_SMO_28"), ActorRef.noSender());
//        Instances test = ConverterUtils.DataSource.read("D:\\FILTERED_SPEED_DATA\\FILTERED_PST-K-TCoCrN15N50A (województwo małopolskie).arff");
//        Instances test = new ConverterUtils.DataSource("DATA/mnist_test.arff").getDataSet();
        Instances test = new ConverterUtils.DataSource("DATA/MAZOWIECKIE_TEST.arff").getDataSet();
        m.tell( test, ActorRef.noSender());
    }
}

