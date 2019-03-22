package aggregation;

import agh.edu.aggregation.ClassGrade;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ClassGradeTest
{
    @Test
    public void testToStringAndFrom()
    {
        double[] fscore = {0.1,0.2,0.3,0.4,0.5};
        double[] AUROC = {0.6,0.7,0.8,0.9,1.0};
        double acc = 1.0;
        double acc_wgt = 0.5;
        double fmeas_wgt = 0.5;
        double grade  = 100.0;

        ClassGrade cg = new ClassGrade( fscore, AUROC, acc, acc_wgt, fmeas_wgt, grade );
        String to_str = cg.toString();
        String expected = grade + ":" +
                acc_wgt + ":" +
                fmeas_wgt + ":" +
                acc + ":" +
                Arrays.toString(fscore) + ":" +
                Arrays.toString(AUROC);

        Assert.assertEquals( expected, to_str );
        ClassGrade from_str = ClassGrade.fromString( to_str );
        Assert.assertEquals( acc, from_str.getAcc(), 0.001 );
        Assert.assertEquals( acc_wgt, from_str.getAcc_wgt(), 0.001 );
        Assert.assertEquals( fmeas_wgt, from_str.getFmeas_wgt(), 0.001 );
        Assert.assertEquals( grade, from_str.getGrade(), 0.001 );
        Assert.assertArrayEquals( fscore, from_str.getFscore(), 0.001 );
        Assert.assertArrayEquals( AUROC, from_str.getAUROC(), 0.001 );
    }
}
