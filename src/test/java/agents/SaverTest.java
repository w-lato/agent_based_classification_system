package agents;

import agh.edu.agents.Master;
import agh.edu.agents.enums.S_Type;
import agh.edu.agents.experiment.ConfParser;
import agh.edu.agents.experiment.RunConf;
import agh.edu.agents.experiment.Saver;
import agh.edu.learning.ClassRes;
import agh.edu.learning.params.ParamsSMO;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Kill;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO sleep() is nessacary because the test end even if actor-thread are running !!!
public class SaverTest
{
    private static Instances data;

    @BeforeClass
    public static void setup() throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\mnist_train.arff");
        data = source.getDataSet();
        data.setClassIndex( data.numAttributes() - 1);
        int n = 20;
        data.stratify(n);
        data = source.getDataSet().testCV(n,0);
        data.setClassIndex( data.numAttributes() - 1);
        cleanWorkspace();

//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                cleanWorkspace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }));
    }


    @Test
    public void testAgentSetup() throws IOException, InterruptedException {
        ActorSystem system = ActorSystem.create("testSystem");
        RunConf rc = ConfParser.getConfFrom( "CONF/TEST_CASE" );
        ActorRef m = system.actorOf( Master.props() ,"master" );
        m.tell( rc, ActorRef.noSender() );

        Thread.sleep( 15*1000 );
        Path test = Files.list( Paths.get("EXP/") ).filter(x-> x.toString().contains("TEST_CASE_")).findFirst().orElse(null);
        List<String> l = Files.list( test )
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());

        int count_ADAs = (int) l.stream().filter(x -> x.contains("ADA")).count();
        int count_MLPs = (int) l.stream().filter(x -> x.contains("MLP")).count();
        int count_SMOs = (int) l.stream().filter(x -> x.contains("SMO")).count();
        Assert.assertEquals(9, count_ADAs);
        Assert.assertEquals(6, count_MLPs);
        Assert.assertEquals(3, count_SMOs);

//        m.tell("RESET", ActorRef.noSender());
        m.tell("INSTANT_KILL", ActorRef.noSender());
        m.tell(Kill.getInstance(), ActorRef.noSender());
    }


    @Test
    public void testSavingInFile() throws Exception
    {
        S_Type type = S_Type.SMO;
        SMO smo = new SMO();
        smo.buildClassifier( data );
        ClassRes cr = new ClassRes( type, smo, data );
        Map<String,Double> m = new HashMap<>();
        m.put("A",9123.01);
        m.put("B",1.001);
        m.put("C",5670.0);
        m.put("D",200.0);

        String cur_dir = Saver.setupNewExp("TEST_DIR");
        String exp_dir =  cur_dir + "/other_id";
        Saver.saveModel( exp_dir, smo, cr, type, data, m );

        // check if files were created
        Assert.assertTrue(Files.exists( Paths.get( exp_dir + ".model" ) ));
        Assert.assertTrue(Files.exists( Paths.get( exp_dir + ".arff" ) ));
        Assert.assertTrue(Files.exists( Paths.get( exp_dir + ".conf" ) ));


        // check if model from file is working
        List<String> l = Files.readAllLines(Paths.get(exp_dir+".conf"));
        S_Type read_type = S_Type.valueOf(l.get(0).split(":")[0]);

        // TODO some process is using other_id.arff - and cannot be deleted no matter what
//        SMO read_smo = (SMO) SerializationHelper.read(exp_dir+".model");
//        Instances read_instances = ConverterUtils.DataSource.read( exp_dir+".arff" );
//        read_instances.setClassIndex( read_instances.numAttributes() - 1 );
//        ClassRes read_cr = new ClassRes( read_type, read_smo, read_instances );
//        Assert.assertEquals(0, cr.compareTo(read_cr));
//        FileUtils.deleteQuietly( Paths.get( exp_dir + ".arff" ).toFile() );


        // check if files contain valid data
        Assert.assertEquals( type, read_type );
        String the_rest = "SMO:0.5:0.5\n" + "A:9123.01\n" + "C:5670.0\n" + "D:200.0\n" + "B:1.001\n";
        StringBuilder read_rest = new StringBuilder();
        for (int i = 0; i < l.size(); i++)
        {
            read_rest.append(l.get(i)).append("\n");
        }
        Assert.assertEquals( the_rest, read_rest.toString());

//        String err_file = Paths.get( exp_dir + ".arff" ).toAbsolutePath().toString();
//        Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"Del /F "+ err_file + " && exit\"");
//        FileDeleteStrategy.FORCE.delete( Paths.get( exp_dir + ".arff" ).toFile() );
//        FileUtils.forceDelete( Paths.get( exp_dir + ".arff" ).toFile() );
//        Files.delete(  );

//        FileUtils.cleanDirectory( Paths.get( cur_dir ).toFile() );
    }

    @Test
    public void checkOptimizationSaving() throws Exception
    {
        S_Type type = S_Type.SMO;
        SMO smo = new SMO();
        smo.buildClassifier( data );
        ClassRes cr = new ClassRes( type, smo, data );
        Map<String,Double> m = new HashMap<>();
        m.put("A",9123.01);
        m.put("B",1.001);
        m.put("C",5670.0);
        m.put("D",200.0);


        String cur_dir =  Saver.setupNewExp("TEST_DIR");
        String exp_dir =  cur_dir + "/some_id";
        Saver.saveModel( exp_dir, smo, cr, type, data, m );

        ParamsSMO p = new ParamsSMO();
        smo = (SMO) p.clasFromStr( p.getParamsCartProd().get(2) );
        smo.buildClassifier( data );

        cr = new ClassRes( type, smo, data);
        m.put( "E",9999.0 );
        Saver.saveModel( exp_dir, smo, cr, type, data, m );

        // check updated files
        List<String> l = Files.readAllLines(Paths.get(exp_dir+".conf"));
        S_Type read_type = S_Type.valueOf(l.get(0).split(":")[0]);
        Assert.assertEquals( type, read_type );

        double acc_wght = Double.valueOf( l.get(0).split(":")[1]);
        double f1_wght = Double.valueOf( l.get(0).split(":")[2]);
        double read_grade  = Double.valueOf( l.get(1).split(":")[1]);

        Assert.assertEquals( read_grade, 9999.0,0.001 );
        Assert.assertEquals( acc_wght, 0.5,0.001 );
        Assert.assertEquals( f1_wght,  0.5,0.001 );

        String the_rest = "E:9999.0\n" + "A:9123.01\n" + "C:5670.0\n" + "D:200.0\n" + "B:1.001\n";
        StringBuilder read_rest = new StringBuilder();
        for (int i = 1; i < l.size(); i++)
        {
            read_rest.append(l.get(i)).append("\n");
        }
        Assert.assertEquals( the_rest, read_rest.toString());

        // check if model from file is working
        SMO read_smo = (SMO) SerializationHelper.read(exp_dir+".model");
//        ConverterUtils.DataSource source = new ConverterUtils.DataSource( );
        Instances read_instances = ConverterUtils.DataSource.read( exp_dir+".arff" );
//        Instances read_instances = source.getDataSet();
        read_instances.setClassIndex( read_instances.numAttributes() - 1 );
        ClassRes read_cr = new ClassRes( read_type, read_smo, read_instances );
        Assert.assertEquals(0, cr.compareTo(read_cr));
    }


    // todo DEAL WITH ID OF MODEL /na_1 
    @Test
    public void testIfAgentsSavesThingsAtTheEnd() throws Exception
    {
        System.out.println( "======================================================== END SAVE TEST STARTED" );
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\iris.arff");
        data = source.getDataSet();
        data.setClassIndex( data.numAttributes() - 1);

        ActorSystem system = ActorSystem.create("testSystem");
        RunConf rc = ConfParser.getConfFrom( "CONF/END_TEST" );
        ActorRef m = system.actorOf( Master.props() ,"master" );
        m.tell( rc, ActorRef.noSender() );
        Thread.sleep( 300 * 1000 );

        Path path = Files.walk(Paths.get("EXP/")).filter(x->x.getFileName().toString()
                .contains("END_TEST")).findFirst().orElse(null);

        assert path != null;
        String m_1 = Files.walk(path).filter(x->x.getFileName().toString().contains("NA_")).findFirst().get().toString();
        m_1 = m_1.split("\\.")[0];
        NaiveBayes nb = (NaiveBayes) SerializationHelper.read( m_1 + ".model" );
        List<String> l = Files.readAllLines( Paths.get( m_1 + ".conf" ) );

        String[] conf = l.get(1).split(":")[0].split(",");
        if( !conf[0].equals("default") )
        {
            Assert.assertEquals((boolean) Boolean.valueOf(conf[0]), nb.getUseKernelEstimator());
            Assert.assertEquals((boolean) Boolean.valueOf(conf[1]), nb.getUseSupervisedDiscretization());
        } else {
            Assert.assertFalse( nb.getUseKernelEstimator());
            Assert.assertFalse( nb.getUseSupervisedDiscretization());
        }

        // the second model
        String m_2 = Files.walk(path).filter(x->x.getFileName().toString().contains("NA_")).findFirst().get().toString();
        m_2 = m_2.split("\\.")[0];
        NaiveBayes nb2 = (NaiveBayes) SerializationHelper.read( m_2 + ".model" );
        List<String> l2 = Files.readAllLines( Paths.get( m_2 + ".conf" ) );

        String[] conf2 = l2.get(1).split(":")[0].split(",");
        if( !conf[0].equals("default") )
        {
            Assert.assertEquals((boolean) Boolean.valueOf(conf2[0]), nb2.getUseKernelEstimator());
            Assert.assertEquals((boolean) Boolean.valueOf(conf2[1]), nb2.getUseSupervisedDiscretization());
        } else {
            Assert.assertFalse( nb2.getUseKernelEstimator());
            Assert.assertFalse( nb2.getUseSupervisedDiscretization());
        }
        int num_of_confs = Math.toIntExact(Files.walk(path).filter(x -> x.getFileName().toString().contains(".conf")).peek(System.out::println).count());
        Assert.assertEquals( 3,num_of_confs);
        m.tell("INSTANT_KILL", ActorRef.noSender());
        m.tell(Kill.getInstance(), ActorRef.noSender());
    }

    @AfterClass
    public static void cleanWorkspace() throws IOException
    {
        List<Path> l = Files.list(Paths.get("EXP"))
                .filter(x-> {
                    String s = x.getFileName().toString();
                    return  s.contains("TEST_CASE") || s.contains("TEST_DIR") || s.contains("END_TEST");
                })
                .peek(System.out::println)
                .collect(Collectors.toList());

        for (Path path : l)
        {
            if( Files.isDirectory( path ) ) FileUtils.cleanDirectory( path.toFile() );
            Files.deleteIfExists( path );
        }
    }
}
