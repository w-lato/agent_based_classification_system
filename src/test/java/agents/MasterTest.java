package agents;

import agh.edu.agents.Master;
import agh.edu.agents.experiment.ConfParser;
import agh.edu.agents.experiment.RunConf;
import agh.edu.aggregation.ResultsHolder;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Kill;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        int cur_id = Files.walk(Paths.get( exp_path +"/AGG/" ) ).map(x-> x.getFileName().toString())
                .filter(x-> x.contains("Q_") && x.contains(".res"))
                .map( x-> x.replace("Q_","").replace(".res","") )
                .map(Integer::valueOf)
                .max(Integer::compareTo)
                .orElse(-1);

        // data and results file exists
        Assert.assertTrue( Files.exists(Paths.get(exp_path+ "/AGG/Q_" + (cur_id) + ".res")));
        Assert.assertTrue( Files.exists(Paths.get(exp_path+ "/AGG/Q_" + (cur_id) + ".arff")));

        // results file is made of four responses (we have 5 models)
        List<String> l = Files.readAllLines( Paths.get( exp_path+"/AGG/Q_"+cur_id+".res" ) );
        String s = l.stream().collect(Collectors.joining("\n"));
        ResultsHolder read_results = ResultsHolder.fromString( s );

        // check first two lines - the exp_id and the ids of models
        Assert.assertEquals( "#"+cur_id, l.get(0) );
        List<String> models = Arrays.asList( new String[]{"EXP/SLAVE_ONLY_TEST_1/ADA_100_100,true_0","EXP/SLAVE_ONLY_TEST_1/SMO_100_DEFAULT_35","EXP/SLAVE_ONLY_TEST_1/RF_100_true,true_1","EXP/SLAVE_ONLY_TEST_1/MLP_100_1000,2,300,1000_111"} );
        Set<String> models_set = new HashSet<>(models);
        Set<String> read_models = new HashSet<>( Arrays.asList( l.get(1).split(":") ) );
        for (String s1 : models_set) {
            Assert.assertTrue( read_models.contains( s1 ) );
        }

        // check the rest
        int count_how_many_arrs = l.get(3).split(":").length;
        Assert.assertEquals( count_how_many_arrs, models_set.size() );
        Assert.assertEquals( test.size(), l.size() - 2 );

        // send one message more
        m.tell( test,ActorRef.noSender() );
        Thread.sleep( 15 * 1000 );
        Assert.assertTrue( Files.exists(Paths.get(exp_path+ "/AGG/Q_" + (cur_id+1) + ".res")));
        Assert.assertTrue( Files.exists(Paths.get(exp_path+ "/AGG/Q_" + (cur_id+1) + ".arff")));


        m.tell("INSTANT_KILL", ActorRef.noSender());
        m.tell(Kill.getInstance(),ActorRef.noSender());
    }
}
