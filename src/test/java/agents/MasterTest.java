package agents;

import agh.edu.agents.Master;
import agh.edu.agents.experiment.ConfParser;
import agh.edu.agents.experiment.RunConf;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Kill;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;

public class MasterTest
{
    private final String exp_path = "EXP/SLAVE_ONLY_TEST_1";
    static ActorSystem system;
    static ActorRef m;
    static RunConf rc;
    static Master.SlaveOnlyExp soe;
    static Instances test;
    @BeforeClass
    public static void setup() throws Exception {
        system = ActorSystem.create("testSystem");
        rc = ConfParser.getConfFrom( "CONF/SLAVE_ONLY_TEST" );
        m = system.actorOf( Master.props() ,"master" );

        test = ConverterUtils.DataSource.read( "DATA/mnist_test.arff" );
        test.setClassIndex( test.numAttributes() - 1);
    }

    @Test
    public void testSlaveOnlyExp() throws InterruptedException, IOException
    {
        soe = new Master.SlaveOnlyExp( exp_path );
        m.tell( soe, ActorRef.noSender() );

        // send data to classify
        m.tell( test,ActorRef.noSender() );

        Thread.sleep( 60 * 1000 );
        m.tell("INSTANT_KILL", ActorRef.noSender());
        m.tell(Kill.getInstance(),ActorRef.noSender());
    }
}
