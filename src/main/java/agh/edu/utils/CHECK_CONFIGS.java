package agh.edu.utils;

import org.apache.commons.collections.map.HashedMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CHECK_CONFIGS
{
    public static void main(String[] args) throws IOException
    {
            List<String> lines = Files.readAllLines(Paths.get("D:\\MLP_GPU_ALL_CONFS - Copy.txt") );
            lines = lines.stream().filter(x->!x.trim().isEmpty()).filter(x->!x.contains("=")).map(x->x.replaceAll("\\s{2,}"," ")).collect(Collectors.toList());

            lines.forEach(System.out::println);
        LinkedHashMap<String,Double> confs = new LinkedHashMap<>();
        for (int i = 0; i < lines.size(); i+=2)
        {
            Double acc = Double.valueOf( lines.get(i+1).split(" ")[lines.get(i+1).split(" ").length - 2 ]);
            String str = lines.get(i).split(" : ")[1].trim();
            confs.put( str,acc );
        }
//        confs.forEach((k, v) -> {
//            System.out.println( k + " : " + v );
//        });


        confs = confs.entrySet().stream().filter(x->x.getValue()>79.0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        confs.entrySet().stream()
                .sorted( (Map.Entry.<String, Double>comparingByValue().reversed()) )
                .forEach(System.out::println);


        Map<String, Long> par_count = new HashedMap();

        // ACTIVATIONS
        long num_of_softmax = confs.entrySet().stream()
                .filter((x)->x.getKey().contains("SOFTMAX"))
                .count();

        long num_of_relu = confs.entrySet().stream()
                .filter((x)->x.getKey().contains("RELU"))
                .count();

        long num_of_cube = confs.entrySet().stream()
                .filter((x)->x.getKey().contains("CUBE"))
                .count();

        par_count.put("SOFTMAX",num_of_softmax);
        par_count.put("CUBE",num_of_cube);
        par_count.put("RELU",num_of_relu);

        // OPT_ALGO

        long num_of_stochs = confs.entrySet().stream()
                .filter((x)->x.getKey().contains("STOCHASTIC_GRADIENT_DESCENT"))
                .count();

        long num_of_conj = confs.entrySet().stream()
                .filter((x)->x.getKey().contains("CONJUGATE_GRADIENT"))
                .count();

        long num_of_lbfgs = confs.entrySet().stream()
                .filter((x)->x.getKey().contains("LBFGS"))
                .count();

        par_count.put("STOCHASTIC_GRADIENT_DESCENT", num_of_stochs);
        par_count.put("CONJUGATE_GRADIENT", num_of_conj);
        par_count.put("LBFGS", num_of_lbfgs);

        // UPDATERS

        long num_of_ADAM = confs.entrySet().stream()
                .filter((x)->x.getKey().startsWith("0,"))
                .count();

        long num_of_MOMENTUM = confs.entrySet().stream()
                .filter((x)->x.getKey().startsWith("1,"))
                .count();
        long num_of_ADA_GRAD = confs.entrySet().stream()
                .filter((x)->x.getKey().startsWith("2,"))
                .count();
        long num_of_RMS_PROP = confs.entrySet().stream()
                .filter((x)->x.getKey().startsWith("3,"))
                .count();

        par_count.put("ADAM",num_of_ADAM);
        par_count.put("MOMENTUM",num_of_MOMENTUM);
        par_count.put("ADA_GRAD",num_of_ADA_GRAD);
        par_count.put("RMS_PROP",num_of_RMS_PROP);

        // LAYERS
        long num_of_two_lay = confs.entrySet().stream()
                .filter((x)->x.getKey().contains(",2,"))
                .count();

        long num_of_three_lay = confs.entrySet().stream()
                .filter((x)->x.getKey().contains(",3,"))
                .count();

        par_count.put("Two hidden layers", num_of_two_lay);
        par_count.put("Three hidden layers", num_of_three_lay);

        // LEARNING RATES

        long num_of_001 = confs.entrySet().stream()
                .filter((x)->x.getKey().contains("0.001"))
                .count();

        long num_of_005 = confs.entrySet().stream()
                .filter((x)->x.getKey().contains("0.005"))
                .count();

        long num_of_01 = confs.entrySet().stream()
                .filter((x)->x.getKey().contains("0.01"))
                .count();

        par_count.put("0.001", num_of_001);
        par_count.put("0.005", num_of_005);
        par_count.put("0.01", num_of_01);

        // hid layer inputs
        long num_of_100_inputs = confs.entrySet().stream()
                .filter((x)->x.getKey().split(",")[x.getKey().split(",").length - 2].equals("100"))
                .count();

        long num_of_300_inputs = confs.entrySet().stream()
                .filter((x)->x.getKey().split(",")[x.getKey().split(",").length - 2].equals("300"))
                .count();

        long num_of_500_inputs = confs.entrySet().stream()
                .filter((x)->x.getKey().split(",")[x.getKey().split(",").length - 2].equals("500"))
                .count();

        long num_of_1000_inputs = confs.entrySet().stream()
                .filter((x)->x.getKey().split(",")[x.getKey().split(",").length - 2].equals("1000"))
                .count();

        par_count.put("100 inputs", num_of_100_inputs);
        par_count.put("300 inputs", num_of_300_inputs);
        par_count.put("500 inputs", num_of_500_inputs);
        par_count.put("1000 inputs", num_of_1000_inputs);

        // num of iter


        long num_of_1000 = confs.entrySet().stream()
                .filter((x)->x.getKey().endsWith("1000"))
                .count();

        long num_of_500 = confs.entrySet().stream()
                .filter((x)->x.getKey().endsWith("500"))
                .count();

        long num_of_100 = confs.entrySet().stream()
                .filter((x)->x.getKey().endsWith("100"))
                .count();

        long num_of_50 = confs.entrySet().stream()
                .filter((x)->x.getKey().endsWith("50"))
                .count();

        long num_of_20 = confs.entrySet().stream()
                .filter((x)->x.getKey().endsWith("20"))
                .count();

        long num_of_5 = confs.entrySet().stream()
                .filter((x)->x.getKey().endsWith("5"))
                .count();

        par_count.put("1000 iter", num_of_1000);
        par_count.put("500 iter", num_of_500);
        par_count.put("100 iter", num_of_100);
        par_count.put("50 iter", num_of_50);
        par_count.put("20 iter", num_of_20);
        par_count.put("5 iter", num_of_5);



        System.out.println( "stochs :" + num_of_stochs );
        System.out.println( "conjf :" + num_of_conj );
        System.out.println( "0.005 :" + num_of_005 );
        System.out.println( "0.001 :" + num_of_001 );
        System.out.println( "500 :" + num_of_500 );
        System.out.println( "1000 :" + num_of_1000 );
        System.out.println( "SOFTMAX :" + num_of_softmax );
        System.out.println( "RELU :" + num_of_relu );
        System.out.println( "CUBE :" + num_of_cube);
        System.out.println( "two :" + num_of_two_lay);
        System.out.println( "three :" + num_of_three_lay);


        par_count.entrySet().stream()
                .sorted( (Map.Entry.<String, Long>comparingByValue().reversed()) )
                .forEach(x->{
            System.out.println( x.getKey() + " & " + x.getValue() + " & " + ( Math.round((x.getValue() / 460.0)*100)) + "\\%\\\\");
        });

        System.out.println("SIZE: " + confs.size());
    }
}
