package agents;

import agh.edu.agents.Aggregator;
import agh.edu.agents.Aggregator.ClassGrade;
import agh.edu.agents.Aggregator.PartialRes;
import agh.edu.agents.ClassSlave;
import agh.edu.agents.ClassSlave.BestClass;
import agh.edu.agents.ClassSlave.ClassSetup;
import agh.edu.agents.ClassSlave.Query;
import agh.edu.agents.enums.S_Type;
import agh.edu.learning.ClassRes;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.concurrent.TimeUnit;

/**
 * https://developer.lightbend.com/guides/akka-quickstart-java/testing-actors.html
 */
public class ClassSlaveTests
{
    static int N = 20;
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
        data.setClassIndex( data.numAttributes() - 1);
        data.stratify( N );
        train = data.testCV( N, 0 );
        train.setClassIndex( train.numAttributes() - 1);



        ActorRef m = ActorRef.noSender();
        A = system.actorOf(ClassSlave.props(new ClassSetup(m, train, S_Type.RF)));
        S = system.actorOf(ClassSlave.props(new ClassSetup(A, train, S_Type.RF)));
        L = system.actorOf(ClassSlave.props(new ClassSetup(m, train, S_Type.RF)));

        smo = new SMO();
        smo.buildClassifier( train );
        cr = new ClassRes(S_Type.SMO,smo,train);
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system, Duration.create(3,TimeUnit.SECONDS),false);
        system = null;
    }

    @Test
    public void testBestClassAndQueryForwardMessage() throws Exception {
        final TestKit testProbe = new TestKit(system);
        ActorRef test = testProbe.testActor();

        ClassRes cr = new ClassRes(S_Type.SMO,smo,train);
        S = system.actorOf(ClassSlave.props(new ClassSetup(test, train, S_Type.RF)));
        S.tell( new Query(1,train.testCV(10,1)), ActorRef.noSender());
        testProbe.expectNoMessage( Duration.apply(3,TimeUnit.SECONDS) );

        S.tell( new BestClass(smo,"default",cr), ActorRef.noSender());
        ClassGrade cg = testProbe.expectMsgClass( ClassGrade.class );

        Assert.assertNotNull( cg.getAUROC() );
        Assert.assertNotNull( cg.getFscore() );

        Assert.assertTrue( Double.compare(cg.getAcc(),0.0) >= 0 );
        Assert.assertTrue( Double.compare(cg.getAcc(),1.0) <= 0 );

        S.tell( new Query(1,train.testCV(10,1)), ActorRef.noSender());
        PartialRes results = testProbe.within(Duration.create(10, TimeUnit.SECONDS), () -> {
            return testProbe.expectMsgClass( PartialRes.class );
        });

        ClassRes x = results.getCr();
        Assert.assertNotNull( x.getAUROC() );
        Assert.assertNotNull( x.getFscore() );
        Assert.assertTrue( Double.compare(x.getAcc(),0.0) >= 0 );
        Assert.assertTrue( Double.compare(x.getAcc(),1.0) <= 0 );
    }

}
