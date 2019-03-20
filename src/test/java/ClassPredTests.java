import agh.edu.agents.Aggregator;
import agh.edu.agents.Aggregator.ClassGrade;
import agh.edu.agents.ClassSlave;
import agh.edu.agents.ClassSlave.ClassSetup;
import agh.edu.agents.enums.ClassStrat;
import agh.edu.agents.enums.S_Type;
import agh.edu.aggregation.ClassPred;
import agh.edu.learning.ClassRes;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPredTests
{
    Map<ActorRef,List<double[]>> probs;
    Map<ActorRef, ClassGrade> grades;
    ActorRef A;
    ActorRef B;
    ActorRef C;

    @Before
    public void setup() throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "DATA\\mnist_train.arff");
        Instances train = source.getDataSet();
        train.setClassIndex( train.numAttributes() - 1);
        train.stratify( 20 );
        train = train.trainCV( 20, 0 );

        ActorSystem system = ActorSystem.create("testSystem");
        ActorRef m = ActorRef.noSender();
        A = system.actorOf(ClassSlave.props(new ClassSetup(m, S_Type.RF)));
        B = system.actorOf(ClassSlave.props(new ClassSetup(m, S_Type.RF)));
        C = system.actorOf(ClassSlave.props(new ClassSetup(m, S_Type.RF)));

        probs = new HashMap<>();
        grades = new HashMap<>();

        List<double[]> l1 = new ArrayList<>();
        double[] a1 = {  1,0.1,0.1};
        double[] a2 = {0.5,  1,0.5};
        double[] a3 = {  1,0.5,  0};
        double[] a4 = {0.1,0.1,0.1};
        double[] a5 = {  0,  0,  0};
        l1.add( a1 );
        l1.add( a2 );
        l1.add( a3 );
        l1.add( a4 );
        l1.add( a5 );


        List<double[]> l2 = new ArrayList<>();
        double[] b1 = {0.1,  1,0.1};
        double[] b2 = {0.5,  1,0.5};
        double[] b3 = {  1,0.5,  0};
        double[] b4 = {0.1,0.1,0.1};
        double[] b5 = {  0,  0,  0};
        l2.add( b1 );
        l2.add( b2 );
        l2.add( b3 );
        l2.add( b4 );
        l2.add( b5 );

        List<double[]> l3 = new ArrayList<>();
        double[] c1 = {0.1,0.1,  1};
        double[] c2 = {0.5,  1,0.5};
        double[] c3 = {  1,0.5,  0};
        double[] c4 = {0.1,0.1,0.1};
        double[] c5 = {  0,  0,  0};
        l3.add( c1 );
        l3.add( c2 );
        l3.add( c3 );
        l3.add( c4 );
        l3.add( c5 );

        probs.put(A,l1);
        probs.put(B,l2);
        probs.put(C,l3);

        // GRADES
        double[] fm_1 = {1,2,3};
        double[] au_1 = {1,2,3};
        double acc_1 = 0.5;
        double acc_w_1 = 0.5;
        double fm_w_1 = 0.5;
        ClassGrade cg_1 = new ClassGrade( fm_1, au_1, acc_1, acc_w_1, fm_w_1 );

        double[] fm_2 = {10,10,10};
        double[] au_2 = {10,10,10};
        double acc_2 = 1.0;
        double acc_w_2 = 0.5;
        double fm_w_2 = 0.5;
        ClassGrade cg_2 = new ClassGrade( fm_2, au_2, acc_2, acc_w_2, fm_w_2 );

        double[] fm_3 = {0.1,0.1,0.1};
        double[] au_3 = {0.1,0.1,0.1};
        double acc_3 = 0.1;
        double acc_w_3 = 0.5;
        double fm_w_3 = 0.5;
        ClassGrade cg_3 = new ClassGrade( fm_3, au_3, acc_3, acc_w_3, fm_w_3 );

        grades.put( A, cg_1 );
        grades.put( B, cg_2 );
        grades.put( C, cg_3 );
    }

    @Test
    public void testMajorityVoting()
    {
        List<Integer> l = ClassPred.getPreds( ClassStrat.MAJORITY, null, probs );
        assert l != null;
//        l.forEach(System.out::println);
        Assert.assertArrayEquals( l.toArray(), new Integer[]{0,1,0,0,0} );
    }

    @Test
    public void testProbSoftVoting()
    {
        List<Integer> l = ClassPred.getPreds( ClassStrat.WEIGHTED, grades, probs );
        assert l != null;
//        l.forEach(System.out::println);
        Assert.assertEquals(0, Double.compare(ClassRes.computeWeight(grades.get(A)), 3.25));
        Assert.assertEquals(0, Double.compare(ClassRes.computeWeight(grades.get(B)), 15.5));
        Assert.assertEquals(0, Double.compare(ClassRes.computeWeight(grades.get(C)), 0.2));
        Assert.assertArrayEquals(  new Integer[]{1,1,0,0,0}, l.toArray() );
    }

}
