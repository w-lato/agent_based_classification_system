package agents;

import agh.edu.agents.Master;
import agh.edu.agents.experiment.ArffMaker;
import agh.edu.agents.experiment.ConfParser;
import agh.edu.agents.experiment.RunConf;
import agh.edu.aggregation.ResultsHolder;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Kill;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

// TODO old mlp grade
//  MLP_100_1000,2,300,1000_111@5.489246926494646:0.5:0.5:0.9980666666666667:[0.9989032312494728, 0.9996290526003413, 0.9978132884777124, 0.9971533143554291, 0.9987158633678624, 0.9980654076462459, 0.9991545485289145, 0.9988014382740711, 0.9954603854389722, 0.996730656383603]:[0.9999997221338245, 0.9999999526548693, 0.9999974905447889, 0.9999968934587047, 0.999999102376592, 0.9999993848707106, 0.9999997562936204, 0.9999993851176642, 0.9999854273231076, 0.9999965479643145]

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
    public void testSlaveOnlyExp() throws Exception {
        soe = new Master.SlaveOnlyExp( exp_path );
        m.tell( soe, ActorRef.noSender() );

        // send data to classify
        m.tell( test,ActorRef.noSender() );

        Thread.sleep( 60 * 1000 );
        int cur_id = Files.walk(Paths.get( exp_path +"/AGG/" ) ).map(x-> x.getFileName().toString())
                .filter(x-> x.contains("Q_") && x.contains(".res"))
                .map( x-> x.replaceAll("[_A-Za-z.]","") )
                .map(Integer::valueOf)
                .max(Integer::compareTo)
                .orElse(-1);

        // data and results file exists
        String path_to_res = exp_path+ "/AGG/Q_RAW_" + (cur_id) + ".res";
        String path_to_arff = exp_path+ "/AGG/Q_" + (cur_id) + ".arff";
        Assert.assertTrue( Files.exists(Paths.get( path_to_res )));
        Assert.assertTrue( Files.exists(Paths.get( path_to_arff )));

        ArffMaker.aggResToArff( path_to_res,path_to_arff);
        String path_to_train_arff = exp_path+ "/AGG/Q_" + (cur_id) + "_TRAIN.arff";
        Assert.assertTrue( Files.exists(Paths.get( path_to_train_arff )));

        // check saved data
        Instances read_data = ConverterUtils.DataSource.read( path_to_train_arff );
        read_data.setClassIndex( read_data.numAttributes() - 1 );
        SMO smo = new SMO();
        smo.buildClassifier( read_data );
        Evaluation evaluation = new Evaluation(read_data);
        evaluation.crossValidateModel( smo, read_data, 10,  new Random(1));
        String eval_res = evaluation.toSummaryString();
        Assert.assertTrue( !eval_res.isEmpty() );
        System.out.println( eval_res );

        // results file is made of four responses (we have 5 models)
        List<String> l = Files.readAllLines( Paths.get( exp_path+"/AGG/Q_RAW_"+cur_id+".res" ) );
        String s = l.stream().collect(Collectors.joining("\n"));
        ResultsHolder read_results = ResultsHolder.fromString( s );
        Assert.assertEquals( read_results.toString()
                , s );

        // check first two lines - the exp_id and the ids of models
        Assert.assertEquals( "#"+cur_id, l.get(0) );
        List<String> models = Arrays.asList( new String[]{"EXP/SLAVE_ONLY_TEST_1/ADA_100_100,true_0","EXP/SLAVE_ONLY_TEST_1/SMO_100_DEFAULT_35","EXP/SLAVE_ONLY_TEST_1/RF_100_true,true_1","EXP/SLAVE_ONLY_TEST_1/MLP_100_0,0.001,CONJUGATE_GRADIENT,RELU,2,100,5"} );
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
        Thread.sleep( 30 * 1000 );
        Assert.assertTrue( Files.exists(Paths.get(exp_path+ "/AGG/Q_" + (cur_id+1) + ".res")));
        Assert.assertTrue( Files.exists(Paths.get(exp_path+ "/AGG/Q_" + (cur_id+1) + ".arff")));


        m.tell("INSTANT_KILL", ActorRef.noSender());
        m.tell(Kill.getInstance(),ActorRef.noSender());
    }
//
//    @AfterClass
//    public static void cleanUp() throws IOException {
//        List<Path> to_del = Files.walk(Paths.get("EXP/SLAVE_ONLY_TEST_1/AGG/"))
//                .filter(x -> !x.getFileName().toString().equals("agg.conf") && x.getFileName().toString().startsWith("Q_"))
//                .collect(Collectors.toList());
//
//        for (Path path : to_del)
//        {
//            Files.delete( path );
//        }
//    }
}
