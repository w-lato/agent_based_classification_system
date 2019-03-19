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
import org.apache.commons.io.FileUtils;
import org.junit.*;
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
    }

    @Test
    public void testSavingInFile() throws Exception
    {
        S_Type type = S_Type.SMO;
        SMO smo = new SMO();
        smo.buildClassifier( data );
        ClassRes cr = new ClassRes( type, smo, data );
        Map<String,String> m = new HashMap<>();
        m.put("A","9123");
        m.put("B","0001");
        m.put("C","5670");
        m.put("D","2000");

        String exp_dir = Saver.setupNewExp("TEST_DIR") + "/some_id";
        Saver.saveModel( exp_dir, smo, cr, type, data, m );

        // check if files were created
        Assert.assertTrue(Files.exists( Paths.get( exp_dir + ".model" ) ));
        Assert.assertTrue(Files.exists( Paths.get( exp_dir + ".arff" ) ));
        Assert.assertTrue(Files.exists( Paths.get( exp_dir + ".conf" ) ));

        // check if files contain valid data
        List<String> l = Files.readAllLines(Paths.get(exp_dir+".conf"));
        S_Type read_type = S_Type.valueOf(l.get(0).split("\n")[0].split(":")[0]);
        Assert.assertEquals( type, read_type );

        double grade = ClassRes.computeWeight( cr );
        double read_grade  = Double.valueOf( l.get(0).split("\n")[0].split(":")[1] );
        Assert.assertEquals( grade, read_grade,0.001 );

        String the_rest = "A\n" + "9123\n" + "C\n" + "5670\n" + "D\n" + "2000\n" + "B\n" + "0001\n";
        StringBuilder read_rest = new StringBuilder();
        for (int i = 1; i < l.size(); i++)
        {
            read_rest.append(l.get(i)).append("\n");
        }
        Assert.assertEquals( the_rest, read_rest.toString());

        // check if model from file is working
        SMO read_smo = (SMO) SerializationHelper.read(exp_dir+".model");
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( exp_dir+".arff");
        Instances read_instances = source.getDataSet();
        read_instances.setClassIndex( read_instances.numAttributes() - 1 );
        ClassRes read_cr = new ClassRes( read_type, read_smo, read_instances );
        Assert.assertEquals(0, cr.compareTo(read_cr));
    }

    @Test
    public void testAgentSetup() throws IOException, InterruptedException {
        ActorSystem system = ActorSystem.create("testSystem");
        RunConf rc = ConfParser.getConfFrom( "CONF/TEST_CASE" );
        ActorRef m = system.actorOf( Master.props() ,"master" );
        m.tell( rc, ActorRef.noSender() );

        Thread.sleep( 5000 );
        List<String> l = Files.list(Paths.get("EXP/TEST_CASE_0"))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());

        int count_ADAs = (int) l.stream().filter(x -> x.contains("ADA")).count();
        int count_MLPs = (int) l.stream().filter(x -> x.contains("MLP")).count();
        int count_SMOs = (int) l.stream().filter(x -> x.contains("SMO")).count();
        Assert.assertEquals(9, count_ADAs);
        Assert.assertEquals(6, count_MLPs);
        Assert.assertEquals(3, count_SMOs);
    }

    @Test
    public void checkOptimizationSaving() throws Exception {
        S_Type type = S_Type.SMO;
        SMO smo = new SMO();
        smo.buildClassifier( data );
        ClassRes cr = new ClassRes( type, smo, data );
        Map<String,String> m = new HashMap<>();
        m.put("A","9123");
        m.put("B","0001");
        m.put("C","5670");
        m.put("D","2000");

        String exp_dir = Saver.setupNewExp("TEST_DIR") + "/some_id";
        Saver.saveModel( exp_dir, smo, cr, type, data, m );

        ParamsSMO p = new ParamsSMO();
        smo = (SMO) p.clasFromStr( p.getParamsCartProd().get(2) );
        smo.buildClassifier( data );

        cr = new ClassRes( type, smo, data);
        m.put( "E","9999" );
        Saver.saveModel( exp_dir, smo, cr, type, data, m );

        // check updated files
        List<String> l = Files.readAllLines(Paths.get(exp_dir+".conf"));
        S_Type read_type = S_Type.valueOf(l.get(0).split("\n")[0].split(":")[0]);
        Assert.assertEquals( type, read_type );

        double grade = ClassRes.computeWeight( cr );
        double read_grade  = Double.valueOf( l.get(0).split("\n")[0].split(":")[1] );
        Assert.assertEquals( grade, read_grade,0.001 );

        String the_rest = "E\n" + "9999\n" + "A\n" + "9123\n" + "C\n" + "5670\n" + "D\n" + "2000\n" + "B\n" + "0001\n";
        StringBuilder read_rest = new StringBuilder();
        for (int i = 1; i < l.size(); i++)
        {
            read_rest.append(l.get(i)).append("\n");
        }
        Assert.assertEquals( the_rest, read_rest.toString());

        // check if model from file is working
        SMO read_smo = (SMO) SerializationHelper.read(exp_dir+".model");
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( exp_dir+".arff");
        Instances read_instances = source.getDataSet();
        read_instances.setClassIndex( read_instances.numAttributes() - 1 );
        ClassRes read_cr = new ClassRes( read_type, read_smo, read_instances );
        Assert.assertEquals(0, cr.compareTo(read_cr));
    }


    @Test
    public void testIfAgentsSavesThingsAtTheEnd() throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\iris.arff");
        data = source.getDataSet();
        data.setClassIndex( data.numAttributes() - 1);
        int n = 5;
        data.stratify(n);
        data = source.getDataSet().testCV(n,0);
        data.setClassIndex( data.numAttributes() - 1);

        ActorSystem system = ActorSystem.create("testSystem");
        RunConf rc = ConfParser.getConfFrom( "CONF/END_TEST" );
        ActorRef m = system.actorOf( Master.props() ,"master" );
        m.tell( rc, ActorRef.noSender() );
        Thread.sleep( 60000 );



        System.out.println("END END END");
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
            if(Files.exists( path ) )
            {
                FileUtils.cleanDirectory( path.toFile() );
                Files.delete(path);
            }
        }
    }
}
