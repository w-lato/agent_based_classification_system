package agh.edu.learning;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.SMO;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataSplitter
{
    public static final int SIMPLE_DIVIDE = 0;
    public static final int OVERLAP_DIVIDE = 1;
    public static final int MULTIFOLD = 2;

    public static List<Instances> divideEqual(Instances rows, int N)
    {
        rows.randomize(new Random(System.currentTimeMillis()));
        List<Instances> arr = new ArrayList<Instances>();
        int part_siz = rows.size() /  N;
        int first = 0;
        int S = rows.size();

        for (int i = 0; i < N; i++)
        {
            arr.add( new Instances(rows, first, part_siz) );
            first += part_siz;
            if( first + part_siz >= S )
            {
                part_siz = S - 1 - first;
                first = S -  part_siz;
            }
        }
        return arr;
    }


    public static List<Instances> overlapDivide(Instances rows, int n, double OL)
    {
        //rows.randomize(new Random(System.currentTimeMillis()));

        List<Instances> arr = new ArrayList<Instances>();
        int part_siz = ((int) (rows.size() / (n * (1 - OL) + OL)));
        int interval = ((int) ((1 - OL) * part_siz)) + 1;
        int first = 0;
        int S = rows.size();

        for (int i = 0; i < n; i++)
        {
            if( (i == (n - 1)) && ((first + part_siz) < (S - 1)))
            {
                System.out.println("&&&&&&&&&&&&&");
                part_siz = S - first;
            }
            System.out.println(i+  " " + first + " " + part_siz + " " + ((first + part_siz) < (S - 1)));
            arr.add( new Instances(rows, first, part_siz) );
            first += interval;
            if( first + part_siz >= S )
            {
                System.out.println("$$$$");
                part_siz = S - first;
                first = S -  part_siz;
            }
        }
        return arr;
    }

    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
        Instances[][] split = new Instances[2][numberOfFolds];

        for (int i = 0; i < numberOfFolds; i++)
        {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }

        return split;
    }

    public static List<Instances> splitIntoTrainAndTest( Instances rows, double train_ratio )
    {
        int train_rows = ((int) (rows.size() * train_ratio));
        List<Instances> l = new ArrayList<>();
        l.add( new Instances(rows, 0, train_rows) );
        l.add( new Instances(rows, train_rows + 1, rows.size() - train_rows - 1) );

        // set class idx
        l.get(0).setClassIndex( l.get(0).numAttributes() - 1 );
        l.get(1).setClassIndex( l.get(0).numAttributes() - 1 );
        return l;
    }

    public static void printRowsList( List<Instances>  arr)
    {
        for (int i = 0; i < arr.size(); i++)
        {
            System.out.println( i + ": siz: " + arr.get( i ).size() + "  class: " + arr.get(i).classIndex() );
            for (int j = 0; j < arr.get( i ).size(); j++)
            {
//                System.out.println( "\t" + arr.get( i ).get(j) );
                System.out.println(arr.get( i ).get(j) );
            }
        }
    }



    public static Evaluation classify(Classifier model,
                                      Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation evaluation = new Evaluation(trainingSet);

        model.buildClassifier(trainingSet);
        evaluation.evaluateModel(model, testingSet);

        return evaluation;
    }

    public static double calculateAccuracy(ArrayList<Prediction> predictions) {
        double correct = 0;

        for (int i = 0; i < predictions.size(); i++)
        {
            NominalPrediction np = (NominalPrediction) predictions.get(i);
            if (np.predicted() == np.actual())
            {
                correct++;
            }
        }

        return 100 * correct / predictions.size();
    }


    /**
     *
     *
     * Accuracy of J48: 95.13%
     * ---------------------------------
     * Accuracy of PART: 95.31%
     * ---------------------------------
     * Accuracy of SMO: 96.54%
     * ---------------------------------
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
//        DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\spambase.arff");
//        DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\iris.arff");
        DataSource source = new DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\pendigits.arff");

        Instances rows = source.getDataSet();
        rows.setClassIndex( rows.numAttributes() - 1 );
        System.out.println( rows );


        System.out.println("========================================================");
        System.out.println("========================================================");
        System.out.println("========================================================");

        List<Instances> l;// = divideEqual( rows, 15 );
//        printRowsList( l );

        System.out.println("========================================================");
        System.out.println("========================================================");
        System.out.println("========================================================");

        l = splitIntoTrainAndTest( rows, 0.8 );
        Instances train = l.get(0);
        Instances test = l.get( 1 );


//        l = overlapDivide( train, 12, 0.3 );
        l = divideEqual( train, 3);
        printRowsList( l );


        Classifier[] models = {
                new J48(), // a decision tree
                new PART(),
                new SMO()
        };
        try {
            for (int j = 0; j < models.length; j++)
            {
                ArrayList<Prediction> results = new ArrayList<>();
                for (int i = 0; i < l.size(); i++)
                {
                    Evaluation validation = classify(models[j], l.get(i), test);
                    results.addAll( validation.predictions() );
                }
                double accuracy = calculateAccuracy( results );
                System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
                        + String.format("%.2f%%", accuracy)
                        + "\n---------------------------------");
            }
            WekaEval we = new WekaEval(0);
            we.setModel( new SMO() );
            we.train( train );
            we.train( l.get(0) );
            ArrayList<Prediction> p = we.eval( test );
            p.forEach(x -> {
                System.out.println( x.actual() + " " + x.predicted() + " " + x.weight() );
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}