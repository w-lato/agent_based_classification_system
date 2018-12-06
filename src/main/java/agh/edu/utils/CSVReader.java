package agh.edu.utils;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVReader {

    public static ArrayList<ArrayList<Double>> readDiabetesRetinopathyData()
    {
//        String path = "/home/vm/Downloads/diabetic_retinopathy.csv";

        String path = System.getProperty("user.dir") + "/DATA/diabetic_retinopathy.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";


        ArrayList< ArrayList<Double> > rows = new ArrayList<ArrayList<Double>>();
        int ctr = 0;
        try
        {
            br = new BufferedReader(new FileReader( path ));
            while ((line = br.readLine()) != null)
            {
                // use comma as separator
                String[] str_data = line.split(cvsSplitBy);
                ArrayList<Double> data = new ArrayList<>();
                for( String it : str_data )
                {
                    data.add( Double.valueOf( it ) );
                }
                rows.add( new ArrayList<Double>(data) );
                System.out.print( ctr + " : ");
                for(Double it : data)
                {
                    System.out.print(it + " ");
                }
                System.out.println();
                ctr++;
            }

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return rows;
    }


    public static ArrayList<ArrayList<Double>> readVehiclesData()
    {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        String path = System.getProperty("user.dir") + "/DATA/vehicles.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";


        ArrayList< ArrayList<Double> > rows = new ArrayList<ArrayList<Double>>();
        int ctr = 0;
        try
        {
            br = new BufferedReader(new FileReader( path ));
            while ((line = br.readLine()) != null)
            {
                // use comma as separator
                String[] str_data = line.split(cvsSplitBy);
                ArrayList<Double> data = new ArrayList<>();
                for( String it : str_data )
                {
//                    System.out.println(it + " " + it.equals("van") + it.length());
                    if(it.equals("saab") || it.equals("van") || it.equals("bus") || it.equals("opel") )
                    {
                        if( it.equals("saab") )
                        {
                            data.add( 0.0 );
                        }
                        else if( it.equals("van") )
                        {
                            data.add( 1.0 );
                        }
                        else if( it.equals("bus") )
                        {
                            data.add( 2.0 );
                        }
                        else
                        {
                            data.add( 3.0 );
                        }
                    }
                    else
                    {
                        data.add( Double.valueOf( it ) );
                    }

                }
                rows.add( new ArrayList<Double>(data) );
                System.out.print( ctr + " : ");
                for(Double it : data)
                {
                    System.out.print(it + " ");
                }
                System.out.println();
                ctr++;
            }

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return rows;
    }


    public static ArrayList<ArrayList<Double>> readFlareData()
    {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        String path = System.getProperty("user.dir") + "/DATA/flare.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";


        ArrayList< ArrayList<Double> > rows = new ArrayList<ArrayList<Double>>();
        int ctr = 0;
        try
        {
            br = new BufferedReader(new FileReader( path ));
            while ((line = br.readLine()) != null)
            {
                // use comma as separator
                String[] str_data = line.split(cvsSplitBy);
                ArrayList<Double> data = new ArrayList<>();
                for( String it : str_data )
                {
                    System.out.println( Double.valueOf( (int)(it.charAt(0)) ));
                    if(it.matches("[A-Z]"))
                    {
                       data.add( Double.valueOf( (int)(it.charAt(0)) ) );
                    }
                    else
                    {
                        data.add( Double.valueOf( it ) );
                    }

                }
                rows.add( new ArrayList<Double>(data) );
                System.out.print( ctr + " : ");
                for(Double it : data)
                {
                    System.out.print(it + " ");
                }
                System.out.println();
                ctr++;
            }

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return rows;
    }

    private static ArrayList<ArrayList<Double>> readDoubleData(String path)
    {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";


        ArrayList<ArrayList<Double>> rows = new ArrayList<>();
        try
        {
            br = new BufferedReader(new FileReader( path ));
            while ((line = br.readLine()) != null)
            {
                // use comma as separator
                String[] str_data = line.split(cvsSplitBy);
                ArrayList<Double> data = new ArrayList<>();
                for( String it : str_data )
                {
                    data.add( Double.valueOf( it ) );
                }
                rows.add( data );

                for(Double it : data)
                {
                    System.out.print(it + " ");
                }
                System.out.println();
            }

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return rows;
    }


    public static void main(String[] args) {

        CSVReader.readDiabetesRetinopathyData().forEach( row ->
                {
                    for (int i = 0; i < row.size(); i++)
                    {
                        System.out.print(row.get(i) + ", ");
                    }
                    System.out.println();
                }
        );

    }
}
