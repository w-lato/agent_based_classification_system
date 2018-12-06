package agh.edu;

import agh.edu.agents.Master;
import agh.edu.messages.M;
import agh.edu.utils.CSVReader;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.sun.xml.internal.ws.api.policy.ModelGenerator;
import weka.classifiers.evaluation.output.prediction.CSV;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;

public class MAIN
{
    public static void main(String[] args)
    {
        ActorSystem system = ActorSystem.create("testSystem");
        ActorRef m = system.actorOf( Master.props() ,"master" );


        ActorRef m2 = system.actorOf( Master.props() ,"master1" );
        m2.tell( new M.Classify( "007" ), m );
        System.out.println( system.child("master") );
        System.out.println( system.child("master") );
        System.out.println(m.path() );

        m.tell( new Master.Init(10,10), m);
        m.tell(new Master.GetList(), m);



        for (int i = 0; i < 2000; i++) {};
        System.out.println("KILL TEST");
        m.tell( new Master.Kill(6), m);
        for (int i = 0; i < 2000; i++) {

        };
        m.tell(new Master.GetList(), m);



        try {
            // Read all the instances in the file (ARFF, CSV, XRFF, ...)
            DataSource source = new DataSource( "/home/vm/IdeaProjects/masters_thesis/DATA/diabetic_retinopathy.csv");
            Instances instances = source.getDataSet();

            // Make the last attribute be the class
            instances.setClassIndex(instances.numAttributes() - 1);

            // Print header and instances.
            System.out.println("\nDataset:\n");
            System.out.println(instances);

            // Print header and instances.
            System.out.println("\nDataset:\n");
            System.out.println(instances);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
