package agents;

import agh.edu.agents.Master;
import agh.edu.agents.experiment.ConfParser;
import agh.edu.agents.experiment.RunConf;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Kill;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class MasterTest
{
    private final String exp_path = "EXP/SLAVE_ONLY_TEST_1";
    static ActorSystem system;
    static ActorRef m;
    static RunConf rc;
    static Master.SlaveOnlyExp soe;

    @BeforeClass
    public static void setup()
    {
        system = ActorSystem.create("testSystem");
        rc = ConfParser.getConfFrom( "CONF/SLAVE_ONLY_TEST" );
        m = system.actorOf( Master.props() ,"master" );
    }

    @Test
    public void testSlaveOnlyExp() throws InterruptedException, IOException
    {
        soe = new Master.SlaveOnlyExp( exp_path );
        m.tell( soe, ActorRef.noSender() );

        Thread.sleep( 360 * 1000 );
        m.tell("INSTANT_KILL", ActorRef.noSender());
        m.tell(Kill.getInstance(),ActorRef.noSender());
    }
}
