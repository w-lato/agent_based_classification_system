package agh.edu.utils;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CLASS_SPLIT
{
    public static Instances filterLabels(String labels, Instances to_filter) throws Exception
    {
        RemoveWithValues filter = new RemoveWithValues();
        String[] options = new String[4];
        options[0] = "-C";   // Choose attribute to be used for selection
        options[1] = "last"; // Attribute number
        options[2] = "-L";   // Numeric value to be used for selection on numeric attribute. Instances with values smaller than given value will be selected. (default 0)
        options[3] = labels;
        filter.setOptions(options);

        filter.setInputFormat( to_filter );
        return Filter.useFilter(to_filter, filter);
    }

    public static void main(String[] args) throws Exception {
        Instances train = ConverterUtils.DataSource.read("DATA/mnist_train.arff");
        train.setClassIndex( train.numAttributes() - 1 );
        Instances test = ConverterUtils.DataSource.read("DATA/mnist_test.arff");
        test.setClassIndex( train.numAttributes() - 1 );

        System.out.println( train.size() );

        String[] labs = new String[]{
          "2-10",
          "1,3-10",
          "1,2,4-10",
          "1-3,5-10",
          "1-4,6-10",
          "1-5,7-10",
          "1-6,8-10",
          "1-7,9-10",
          "1-9"
        };

        int ctr = 1;
        train.stratify( 10 );
        for (String lab : labs)
        {
            Instances ones = filterLabels(lab, train);
            System.out.println( ones.size() );
            Instances the_rest = filterLabels(String.valueOf(ctr), train.testCV(10,ctr-1));
            ones.addAll( the_rest );

            SMO smo = new SMO();
            smo.buildClassifier( ones );

            Evaluation e = new Evaluation( test );
            e.evaluateModel( smo, test );

            System.out.println( e.toSummaryString() );
            for (int i = 0; i < 10; i++)
            {
                System.out.println(i + " : " + e.fMeasure(i) );
            }
            int curr_class = ctr - 1;
            int correct = 0;
            int total = 0;
            for (int i = 0; i < e.predictions().size(); i++)
            {
                if( e.predictions().get(i).predicted() == curr_class )
                {
                    total++;
                    if( e.predictions().get(i).actual() == curr_class ) correct++;
                }
            }
            System.out.println( ctr + " :: " + correct + " out of " + total  );

//            System.out.println( " : " + e.numFalseNegatives(ctr) );
//            System.out.println(1 + " : " + e.numFalsePositives(ctr) );
//            System.out.println(1 + " : " + e.numTrueNegatives(ctr) );
//            System.out.println(1 + " : " + e.numTruePositives(ctr) );
            ctr++;
        }
    }
}
