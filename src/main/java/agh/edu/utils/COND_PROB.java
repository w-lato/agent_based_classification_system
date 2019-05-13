package agh.edu.utils;

import org.apache.commons.collections.map.HashedMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class COND_PROB
{
    public static List<int[]> perms = new ArrayList<>();

    public static double calcProb(Map<String[],Double> results, String[] conf, double acc_thresh)
    {
//        double P_A = 0.0;
        double B = 0.0;
        double AnB = 0.0;
        for (Map.Entry<String[], Double> it : results.entrySet())
        {
            int ctr = 0;
            for (int i = 0; i < conf.length; i++)
            {
                boolean present = false;
                for (int j = 0; j < it.getKey().length; j++)
                {
                    if( it.getKey()[j].equals(conf[i])){
                        present = true;
                        break;
                    }
                }
                if( !present ) break;
                else ctr++;
            }
//            if( ctr == conf.length ) P_A++;
            if( it.getValue() >= acc_thresh ) B++;
            if( ctr == conf.length && (it.getValue() >= acc_thresh) ) AnB++;
        }
//        System.out.println( conf[0] + " :: " + AnB );
        return (AnB / results.size()) / (B / results.size());
    }


    public static void main(String[] args) throws IOException {
        // load data from GPU
        Map<String,Double> results = new HashMap<>();

//        List<String> res = Files.readAllLines(Paths.get("D:\\gpu_all_measures.txt") );
//        List<String> res = Files.readAllLines(Paths.get("D:\\MLP_GPU_ALL_CONFS.txt") );
        List<String> res = Files.readAllLines(Paths.get("D:\\CLEANED_RESULTS\\GPU_ALL.txt") );
        String conf = "";
        for (String re : res)
        {
            if( re.isEmpty() || re.contains("==") ) continue;
            if( re.contains(" : ") )
            {
//                conf = re.split(" : ")[1].split(",");
                conf = re.split(" : ")[1];
                String[] aux = conf.split(",");
                int updater = Integer.valueOf( aux[0] );
                switch ( updater )
                {
                    case  0: aux[0] = "ADAM"; break;
                    case  1: aux[0] = "MOMENTUM"; break;
                    case  2: aux[0] = "ADA_GRID"; break;
                    case  3: aux[0] = "RMS_PROB"; break;
                    default: aux[0] = "NULL"; break;
                }
                switch (aux[5])
                {
                    case  "100": aux[5] = "INP_100"; break;
                    case  "300": aux[5] = "INP_300"; break;
                    case  "500": aux[5] = "INP_500"; break;
                    case  "1000": aux[5] = "INP_1000"; break;
                    default: aux[5] = "NULL"; break;
                }
                conf = String.join(",",aux);
            }
            if( !conf.isEmpty() && re.startsWith("Correctly Classified Instances") )
            {
                Double acc = Double.valueOf(re.split("    ")[ re.split("    ").length - 2 ]);
                results.put( conf, acc);
                conf = "";
            }
        }
        results.entrySet().stream()
                .sorted( Map.Entry.comparingByValue())
                .forEach( System.out::println );

        String s = "3,0.001,STOCHASTIC_GRADIENT_DESCENT";
        Map<String[], Double> results_1 = new HashMap<>();
        for (Map.Entry<String, Double> stringDoubleEntry : results.entrySet()) {
            results_1.put( stringDoubleEntry.getKey().split(","), stringDoubleEntry.getValue() );
        }
        System.out.println( calcProb(results_1,s.split(","),85.0) );


        // generate permutations of configurations
        List<String[]> perms = new ArrayList<>();
        int[] keys =  new int[] {0,1,2,3,4,5,6};

        String[] activations = {"RELU", "TANH", "CUBE"};
        String[] opt_algo = {"CONJUGATE_GRADIENT","LBFGS", "STOCHASTIC_GRADIENT_DESCENT"};
        String[] updaters = {"ADAM","MOMENTUM","ADA_GRAD","RMS_PROP"};
        String[] num_of_layers = {"2", "3"};
        String[] learning_rates = {"0.001","0.005","0.1"};
        String[] hid_lay_inputs = {"INP_100","INP_300","INP_500","INP_1000"};
        String[] num_of_iter = {"5", "20", "50", "100", "500", "1000"};

        List<String[]> params = new ArrayList<>();
        params.add( activations );
        params.add( opt_algo );
        params.add( updaters );
        params.add( num_of_layers );
        params.add( learning_rates );
        params.add( hid_lay_inputs );
        params.add( num_of_iter );


        Map<String[],Double> perf = new HashMap<>();
        for (int i = 2; i < 7; i++)
        {
            Combination.printCombination( keys, 7, i );
        }
        for (int[] perm : Combination.perms)
        {
            int N = 4;
            if( perm.length != N ) continue;

            if( N == 1 )
            {
                for (int i = 0; i < params.get( perm[ 0 ] ).length; i++)
                {
                    perf.put( new String[]{ params.get( perm[0] )[i] }, 0.0 );
                }
            }

            if( N == 2 )
            {
                    // two params
                for (int i = 0; i < params.get( perm[ 0 ] ).length; i++)
                {
                    for (int j = 0; j < params.get( perm[ 1 ] ).length; j++)
                    {
                           perf.put( new String[]{ params.get( perm[0] )[i], params.get( perm[1] )[j] }, 0.0 );
                    }
                }
            }
            if( N == 3 )
            {
                for (int i = 0; i < params.get( perm[ 0 ] ).length; i++)
                {
                    for (int j = 0; j < params.get( perm[ 1 ] ).length; j++)
                    {
                        for (int k = 0; k < params.get( perm[ 2 ] ).length; k++)
                        {
                            perf.put( new String[]{ params.get( perm[ 0 ] )[i ], params.get( perm[ 1 ] )[j], params.get( perm[ 2 ] )[k] }, 0.0 );
                        }
                    }
                }
            }

            if( N == 4 )
            {
                for (int i = 0; i < params.get( perm[ 0 ] ).length; i++)
                {
                    for (int j = 0; j < params.get( perm[ 1 ] ).length; j++)
                    {
                        for (int k = 0; k < params.get( perm[ 2 ] ).length; k++)
                        {
                            for (int l = 0; l < params.get( perm[ 3 ] ).length; l++) {
                                perf.put( new String[]{ params.get( perm[ 0 ] )[i ], params.get( perm[ 1 ] )[j], params.get( perm[ 2 ] )[k], params.get( perm[ 3 ] )[l] }, 0.0 );
                            }

                        }
                    }
                }
            }

            if( N == 5 )
            {
                for (int i = 0; i < params.get( perm[ 0 ] ).length; i++)
                {
                    for (int j = 0; j < params.get( perm[ 1 ] ).length; j++)
                    {
                        for (int k = 0; k < params.get( perm[ 2 ] ).length; k++)
                        {
                            for (int l = 0; l < params.get( perm[ 3 ] ).length; l++)
                            {
                                for (int m = 0; m < params.get( perm[ 4 ] ).length; m++)
                                {
                                    perf.put( new String[]{ params.get( perm[ 0 ] )[i ], params.get( perm[ 1 ] )[j], params.get( perm[ 2 ] )[k], params.get( perm[ 3 ] )[l], params.get( perm[ 4 ] )[m] }, 0.0 );
                                }
                            }
                        }
                    }
                }
            }

            if( N == 6 )
            {
                for (int i = 0; i < params.get( perm[ 0 ] ).length; i++)
                {
                    for (int j = 0; j < params.get( perm[ 1 ] ).length; j++)
                    {
                        for (int k = 0; k < params.get( perm[ 2 ] ).length; k++)
                        {
                            for (int l = 0; l < params.get( perm[ 3 ] ).length; l++)
                            {
                                for (int m = 0; m < params.get( perm[ 4 ] ).length; m++)
                                {
                                    for (int n = 0; n < params.get( perm[5] ).length; n++)
                                    {
                                        perf.put( new String[]{ params.get( perm[ 0 ] )[i ], params.get( perm[ 1 ] )[j], params.get( perm[ 2 ] )[k], params.get( perm[ 3 ] )[l], params.get( perm[ 4 ] )[m], params.get( perm[5] )[n] }, 0.0 );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        perf.forEach( (k,v)-> {
            perf.put( k, calcProb(results_1, k, 84.99) );
        } );


        List<String> st = new ArrayList<>();
        perf.entrySet().stream()
                .sorted( Map.Entry.comparingByValue())
                .forEach((x->{
                    st.add( String.join(",", x.getKey()) + " & " + (x.getValue()*100) + "\\% \\\\" );
//                    System.out.println( String.join(",", x.getKey()) + " & " + (x.getValue()*100) + "\\% \\\\");
                }));

        for (int i = st.size()-1; i >= 0; i--)
        {
            System.out.println( st.get(i) );
            System.out.println( "\\hline" );
        }
//        results.forEach( (k,v)->{
//            System.out.println( k + " : : " + v );
//        } );


        // calc cond. probabilities


    }
}
