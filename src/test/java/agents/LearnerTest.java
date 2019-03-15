package agents;

import agh.edu.agents.ClassSlave;
import agh.edu.agents.Learner;
import agh.edu.agents.enums.S_Type;
import agh.edu.learning.ClassRes;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LearnerTest
{
    static int N = 50;
    static Instances data;
    static Instances train;
    static ActorRef S;
    static ActorRef L;
    static ActorRef A;
    static ActorSystem system;
    static SMO smo;
    static ClassRes cr;

    @BeforeClass
    public static void setup() throws Exception {
        system = ActorSystem.create();
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\mnist_train.arff");
        data = source.getDataSet();
        data.randomize( new Random(1));
        data.setClassIndex( data.numAttributes() - 1);
        data.stratify( N );
        train = data.testCV( N, 0 );
        train.setClassIndex( train.numAttributes() - 1);
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system, Duration.create(3, TimeUnit.SECONDS),false);
        system = null;
    }

    // 1,3.5,true :  1,3.5,true <-
    @Test
    public void testBestClassAndQueryForwardMessage() throws Exception {
        final TestKit testProbe = new TestKit(system);
        ActorRef test = testProbe.testActor();
        S = system.actorOf(Learner.props("TO_DELETE",S_Type.SMO, train, test));

        ClassSlave.BestClass results = testProbe.within(Duration.create(15, TimeUnit.SECONDS), () -> {
            return testProbe.expectMsgClass( ClassSlave.BestClass.class );
        });
        results = testProbe.within(Duration.create(300, TimeUnit.SECONDS), () -> {
            return testProbe.expectMsgClass( ClassSlave.BestClass.class );
        });
        System.out.println( results.getConf() );
    }


    // TODO something is runing but it couldn't reach better class than default one
    @Test
    public void testMLPOptimization() throws Exception {
        final TestKit testProbe = new TestKit(system);
        ActorRef test = testProbe.testActor();
        S = system.actorOf(Learner.props("TO_DELETE", S_Type.MLP, train, test));

        ClassSlave.BestClass results = testProbe.within(Duration.create(300, TimeUnit.SECONDS), () -> {
            return testProbe.expectMsgClass( ClassSlave.BestClass.class );
        });
        results = testProbe.within(Duration.create(300, TimeUnit.SECONDS), () -> {
            return testProbe.expectMsgClass( ClassSlave.BestClass.class );
        });
        System.out.println( results.getConf() );
    }

}
