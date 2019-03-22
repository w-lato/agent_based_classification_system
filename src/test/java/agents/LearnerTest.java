package agents;

import agh.edu.agents.ClassSlave;
import agh.edu.agents.Learner;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.experiment.Loader;
import agh.edu.aggregation.ClassGrade;
import agh.edu.learning.ClassRes;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LearnerTest
{
    static int N = 50;
    static Instances data;
    static Instances train;
    static ActorRef S;
    static ActorSystem system;

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
        String exp_path = "EXP/FOR_TESTS_0/";
        ActorRef slave = system.actorOf( ClassSlave.props( new ClassSlave.ClassSetup(test, S_Type.SMO,exp_path +"SMO_1")) );
        S = system.actorOf(Learner.props(exp_path,S_Type.SMO, train, slave));

        ClassGrade results = testProbe.within(Duration.create(15, TimeUnit.SECONDS), () -> {
            return testProbe.expectMsgClass( ClassGrade.class );
        });
//        double reaq_grade = "EXP/FOR_TESTS_0/"
//        System.out.println( results. );
    }


//    // TODO something is running but it couldn't reach better class than default one
//    @Test
//    public void testMLPOptimization() throws Exception {
//        final TestKit testProbe = new TestKit(system);
//        ActorRef test = testProbe.testActor();
//        S = system.actorOf(Learner.props("TO_DELETE", S_Type.MLP, train, test));
//
//        ClassSlave.BestClass results = testProbe.within(Duration.create(300, TimeUnit.SECONDS), () -> {
//            return testProbe.expectMsgClass( ClassSlave.BestClass.class );
//        });
//        results = testProbe.within(Duration.create(300, TimeUnit.SECONDS), () -> {
//            return testProbe.expectMsgClass( ClassSlave.BestClass.class );
//        });
//        System.out.println( results.getConf() );
//    }

    @Test
    public void testLoadFetureWithAllConfigsUsed() throws Exception {
        final TestKit testProbe = new TestKit(system);
        ActorRef test = testProbe.testActor();

        LinkedHashMap<String,Double> used_conf = Loader.getConfigs(Paths.get("EXP/FOR_TESTS_0/NA_1.conf") );
        Classifier model = Loader.getModel( "EXP/FOR_TESTS_0/NA_1.model" );
        S = system.actorOf(Learner.props("EXP/FOR_TESTS_0/NA_1",model, S_Type.NA, train, test, used_conf));

        ClassSlave.BestClass results = testProbe.within(Duration.create(10, TimeUnit.SECONDS), () -> {
            return testProbe.expectMsgClass( ClassSlave.BestClass.class );
        });

        Assert.assertEquals( "false,true", results.getConf() );
        ClassRes cr = new ClassRes(S_Type.NA, model, train);
        Assert.assertEquals( cr.compareTo(results.getResults()), 0 );
        Assert.assertTrue( S.isTerminated() );
    }

    @Test
    public void testUnfinishedSetOfConfigs() throws Exception
    {
        final TestKit testProbe = new TestKit(system);
        ActorRef test = testProbe.testActor();

        LinkedHashMap<String,Double> used_conf = Loader.getConfigs(Paths.get("EXP/FOR_TESTS_0/IBK_1.conf") );
        Classifier model = Loader.getModel( "EXP/FOR_TESTS_0/IBK_1.model" );
        S = system.actorOf(Learner.props("EXP/FOR_TESTS_0/IBK_1",model, S_Type.IBK, train, test, used_conf));

        ClassSlave.BestClass results = testProbe.within(Duration.create(80, TimeUnit.SECONDS), () -> {
            return testProbe.expectMsgClass( ClassSlave.BestClass.class );
        });

        Assert.assertEquals( "default", results.getConf() );
        ClassRes cr = new ClassRes(S_Type.IBK, model, train);
        Assert.assertEquals( cr.compareTo(results.getResults()), 0 );
        Assert.assertTrue( !S.isTerminated() );

        // check if its updating conf file with a new one
        Thread.sleep( 120 * 1000 );
        Path conf = Paths.get( "EXP/FOR_TESTS_0/IBK_1.conf" );
        List<String> l = Files.readAllLines( conf );
        Assert.assertTrue( l.size() > 2);
        while (l.size() > 2)
        {
            l.remove( l.size() - 1 );
        }
        Files.write( conf, l );
    }
}
