import org.junit.Test;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.TestCase.assertTrue;

public class StratificationTest
{
    @Test
    public void testStratification() throws Exception
    {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("DATA/mnist_train.arff");
        Instances instances = source.getDataSet();
        instances.setClassIndex( instances.numAttributes() - 1);
        Map<Integer,Integer> vals = new HashMap<>();
        Map<Integer,Integer> vals_parts = new HashMap<>();

        instances.stream().forEach( x -> {
            int cl = ((int) x.classValue());
            if( !vals.containsKey( cl ) ) vals.put( cl, 1 );
            else  vals.put( cl, vals.get( cl ) + 1 );
        });

        int N = 10;
        instances.stratify( N );
        for (int i = 0; i < N; i++)
        {
            vals_parts.clear();
            Instances part = instances.testCV( N, i );
            part.stream().forEach( x -> {
                int cl = ((int) x.classValue());
                if( !vals_parts.containsKey( cl ) ) vals_parts.put( cl, 1 );
                else  vals_parts.put( cl, vals_parts.get( cl ) + 1 );
            });


            System.out.println( i +  " : "  );
            vals_parts.forEach( (k,v)-> {
                Integer orig = vals.get( k ) / 10;
                assertTrue("Error, random is too high", orig+1 >= v);
                assertTrue("Error, random is too low",  orig-1  <= v);
                System.out.print( k + " : " + v + ", " );
            } );
            System.out.println();
        }
    }
}
