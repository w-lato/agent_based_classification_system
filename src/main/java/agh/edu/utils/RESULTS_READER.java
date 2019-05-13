package agh.edu.utils;


import agh.edu.learning.params.ParamsMLP;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class RESULTS_READER
{

    public static Map<String,Double> read_accuracies(String path) throws IOException {
        Map<String,Double> results = new HashMap<>();

//        List<String> res = Files.readAllLines(Paths.get("D:\\gpu_all_measures.txt") );
        List<String> res = Files.readAllLines(Paths.get(path));
        String conf = "";
        for (String re : res)
        {
            if( re.isEmpty() || re.contains("==") || re.contains("/") || re.contains("SOFTMAX")) continue;
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

        return results;
    }

    public static Map<String,Pair> read_all_pairs_from(String path) throws IOException
    {
        List<String> confs = Files.readAllLines( Paths.get(path));
//        confs = confs.stream().filter(x->x.contains(" : ") || x.contains("Correctly Classified Instances")).collect(Collectors.toList());

        Map<String,Pair> map = new HashMap<>();
        for (int i = 0; i < confs.size(); i+=2)
        {
            String conf = confs.get( i ).split(" : ")[1];
            long dur = Long.valueOf(confs.get( i ).split(" : ")[2]);
            int len = confs.get(i+1).replaceAll(" +", " ").split(" ").length;
            double acc = Double.valueOf( confs.get(i+1).replaceAll(" +", " ").split(" ")[len - 2] );

            if( map.containsKey( conf ) ) System.out.println("!! " + conf );
            map.put( conf, new Pair(dur,acc) );
        }
        return map;
    }

    public static class Pair
    {
        public long milis = -1;
        public double acc = -1;

        public Pair(long milis, double acc)
        {
            this.milis = milis;
            this.acc = acc;
        }
    }

    public static void main(String[] args) throws Exception
    {
//        Map<String,Double> TANH_GPU_results = read_accuracies( "D:\\GPU_TEST_RESULTS\\CPU_TANH_ALL.txt");
//        Map<String,Double> TANH_CPU_results = read_accuracies( "D:\\GPU_TEST_RESULTS\\CPU_TANH_ALL.txt");

        Map<String,Double> SOFTMAX_CPU_results = read_accuracies( "D:\\OUTPUTS\\cpu_all_confs.txt");
        Map<String,Double> SOFTMAX_GPU_results = read_accuracies( "D:\\MLP_GPU_ALL_CONFS.txt");

        System.out.println( SOFTMAX_GPU_results.size() );
        System.out.println( SOFTMAX_CPU_results.size() );


        double sum = 0.0;
        for (Map.Entry<String, Double> stringDoubleEntry : SOFTMAX_CPU_results.entrySet())
        {
            sum += stringDoubleEntry.getValue();
        }
        System.out.println( sum / SOFTMAX_CPU_results.size() );
        System.out.println( SOFTMAX_CPU_results.size() );
        System.out.println( SOFTMAX_GPU_results.size() );


        SOFTMAX_CPU_results.keySet().removeAll( SOFTMAX_GPU_results.keySet() );
        SOFTMAX_CPU_results.keySet().stream().forEach( System.out::println );
        Instances TEST_RAW = ConverterUtils.DataSource.read("DATA/mnist_test.arff");
        TEST_RAW.setClassIndex( TEST_RAW.numAttributes() - 1 );
        System.out.println( new ParamsMLP( TEST_RAW ).getParamsCartProd().size());

        // CARTESIAN PRODUCT OF PARAMS
        Set<String> confs = new HashSet<>(new ParamsMLP(TEST_RAW).getParamsCartProd());
        Files.write(Paths.get("D:\\NEW_MLP_CONFS.txt"), Collections.singleton(String.join("\n", confs)));
//        confs.removeAll( SOFTMAX_CPU_results.keySet() );
//        System.out.println( confs.size() );


        // TESTED CONFIGURATIONS
        List<String> read_confs = Files.readAllLines( Paths.get("D:\\MLP_GPU_ALL_CONFS.txt"));
//        List<String> read_confs = Files.readAllLines( Paths.get("D:\\OUTPUTS\\cpu_all_confs.txt"));
        read_confs = read_confs.stream().filter(x->x.contains(" : ") && !x.contains("SOFTMAX")).map(x->x.split(" : ")[1]).collect(Collectors.toList());

        // CP \ TESTED
        Set<String> all  =  new HashSet<>(read_confs);
        confs.removeAll( all );

        for (String conf : confs)
        {
            System.out.println( "\"" + conf + "\"," );
        }
        System.out.println("SIZE OF READ CONFIGS: " + all.size());
        System.out.println( confs.size() );


        // read additional confs
        List<String> additional_confs = Files.readAllLines( Paths.get("D:\\GPU_TEST_RESULTS\\GPU_TANH_ALL_RES.txt"));
//        List<String> additional_confs = Files.readAllLines( Paths.get("D:\\GPU_TEST_RESULTS\\CPU_TANH_ALL.txt"));
        additional_confs = additional_confs.stream().filter(x->x.contains(" : ") || x.contains("Correctly Classified Instances")).collect(Collectors.toList());
        System.out.println( additional_confs.size() );
//        Files.write( Paths.get("D:\\CLEANED_RESULTS\\GPU_TANH_ALL.txt"), String.join("\n",additional_confs).getBytes() );

        // read GPU confs
//        read_confs = Files.readAllLines( Paths.get("D:\\MLP_GPU_ALL_CONFS.txt"));
//        read_confs = read_confs.stream().filter(x->x.contains(" : ") || x.contains("Correctly Classified Instances")).collect(Collectors.toList());
//        read_confs.forEach(System.out::println);
//        read_confs = Files.readAllLines( Paths.get("D:\\MLP_GPU_ALL_CONFS.txt"));;
        read_confs = Files.readAllLines( Paths.get("D:\\OUTPUTS\\smo_CPU_all_confs.txt"));;
        read_confs = read_confs.stream().filter(x->x.contains(" : ") || x.contains("Correctly Classified Instances")).collect(Collectors.toList());

//        List<String> XD = new ArrayList<>();
//        for (int i = 0; i < read_confs.size(); i+=2) {
//            XD.add( read_confs.get(i) + "##"+read_confs.get(i+1) );
//        }
//        XD = XD.stream().filter( x->!x.contains("SOFTMAX") ).collect(Collectors.toList());
//        read_confs.clear();
//        for (int i = 0; i < XD.size(); i++)
//        {
//            String[] a = XD.get(i).split("##");
//            read_confs.add( a[0] );
//            read_confs.add( a[1] );
//        }
//
//
//        Files.write( Paths.get("D:\\CLEANED_RESULTS\\TEST.txt"), String.join("\n",read_confs).getBytes() );
        //
        Map<String,Pair> A = read_all_pairs_from( "D:\\CLEANED_RESULTS\\gpu_old_confs.txt" );
        Map<String,Pair> B = read_all_pairs_from( "D:\\CLEANED_RESULTS\\cpu_old_confs.txt" );

        Map<String,Pair> C = read_all_pairs_from( "D:\\CLEANED_RESULTS\\GPU_TANH_ALL.txt" );
        Map<String,Pair> D = read_all_pairs_from( "D:\\CLEANED_RESULTS\\CPU_TANH_ALL.txt" );

        sum = 0.0;
        for (Map.Entry<String, Pair> stringPairEntry : A.entrySet()) {
            sum += stringPairEntry.getValue().acc;
        }
        for (Map.Entry<String, Pair> stringPairEntry : C.entrySet()) {
            sum += stringPairEntry.getValue().acc;
        }
        System.out.println( sum  / (A.size() + C.size()) );

        sum = 0.0;
        for (Map.Entry<String, Pair> stringPairEntry : B.entrySet()) {
            sum += stringPairEntry.getValue().acc;
        }
        for (Map.Entry<String, Pair> stringPairEntry : D.entrySet()) {
            sum += stringPairEntry.getValue().acc;
        }
        System.out.println( sum  / (B.size()+D.size()) );


        System.out.println("===================================================");
        long tm = 0;
        tm = 0;
        for (Map.Entry<String, Pair> stringPairEntry : A.entrySet()) {
            tm += stringPairEntry.getValue().milis;
        }
        for (Map.Entry<String, Pair> stringPairEntry : C.entrySet()) {
            tm += stringPairEntry.getValue().milis;
        }
        System.out.println( tm );

        tm = 0;
        for (Map.Entry<String, Pair> stringPairEntry : B.entrySet()) {
            tm += stringPairEntry.getValue().milis;
        }
        for (Map.Entry<String, Pair> stringPairEntry : D.entrySet()) {
            tm += stringPairEntry.getValue().milis;
        }
        System.out.println( tm );


        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

        Map<String,Pair> GPU = read_all_pairs_from( "D:\\CLEANED_RESULTS\\GPU_ALL.txt" );
        Map<String,Pair> CPU = read_all_pairs_from( "D:\\CLEANED_RESULTS\\CPU_ALL.txt" );

        sum = 0.0;
        double max = 0.0;
        int ctr = 0;
        List<String> vals=  new ArrayList<>();
        for (Map.Entry<String, Pair> stringPairEntry : GPU.entrySet()) {
            sum += stringPairEntry.getValue().acc;
            if( stringPairEntry.getValue().acc > max ) max = stringPairEntry.getValue().acc;
            if( stringPairEntry.getValue().acc > 84.99 ) ctr++;
            vals.add( String.valueOf(stringPairEntry.getValue().acc) );
        }
        Files.write( Paths.get("D:\\MEASURES\\GPU_ALL_ACC.txt"),String.join("\n",vals).getBytes() );

        System.out.println( sum  / (GPU.size()) );
        System.out.println( "MAX: " + max );
        System.out.println("CTR: " + ctr);
        sum = 0.0;
        for (Map.Entry<String, Pair> stringPairEntry : CPU.entrySet()) {
            sum += stringPairEntry.getValue().acc;
        }
        System.out.println( sum  / (CPU.size()) );


        tm = 0;
        for (Map.Entry<String, Pair> stringPairEntry : GPU.entrySet()) {
            tm += stringPairEntry.getValue().milis;
        }
        System.out.println( tm );

        tm = 0;
        for (Map.Entry<String, Pair> stringPairEntry : CPU.entrySet()) {
            tm += stringPairEntry.getValue().milis;
        }
        System.out.println( tm );
        System.out.println(  GPU.size() );
    }
}
