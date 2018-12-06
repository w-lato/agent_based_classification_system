package agh.edu.utils;

import com.sun.media.sound.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


/**
 *
 * https://www.callicoder.com/java-read-excel-file-apache-poi/
 *
 */
public class XLSParser {

    public ArrayList<ArrayList<Double>> rows;

    ArrayList<ArrayList<Double>> values; // X values red from file
    public ArrayList<ArrayList<Double>> normalized_values; // X values red from file
    ArrayList<Double> listedValues;
    ArrayList<String> classes;
    public ArrayList<Integer> int_classes;
    ArrayList<Integer> actual_outputs;

    public ArrayList<Double> getListedValues() {
        return listedValues;
    }

    public ArrayList<Integer> getInt_classes() {
        return int_classes;
    }

    public void readIrisTrainingValues(String path)
    {
        try {
            Workbook workbook = WorkbookFactory.create(new File(path));
            System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

            System.out.println("Retrieving Sheets using Java 8 forEach with lambda");

            workbook.forEach(sheet -> {
                System.out.println("=> " + sheet.getSheetName());
            });

            // 3. Or you can use Java 8 forEach loop with lambda
            System.out.println("\n\nIterating over Rows and Columns using Java 8 forEach with lambda\n");


            // Getting the Sheet at index zero
            Sheet sheet = workbook.getSheetAt(0);

            // Create a DataFormatter to format and get each cell's value as String
            DataFormatter dataFormatter = new DataFormatter();

            listedValues = new ArrayList<Double>();

            rows = new ArrayList<ArrayList<Double>>();
            values = new ArrayList<ArrayList<Double>>();
            values.add(new ArrayList<Double>()); // 1st col
            values.add(new ArrayList<Double>()); // 2nd
            values.add(new ArrayList<Double>()); // 3rd
            values.add(new ArrayList<Double>()); // 4th
            classes = new ArrayList<String>();

            sheet.forEach(row -> {
                rows.add( new ArrayList<Double>() );
                for (int i = 0; i < row.getLastCellNum() - 1; i++) {
                    values.get(i).add(new Double(row.getCell(i).getNumericCellValue()));
                    listedValues.add( new Double( row.getCell(i).getNumericCellValue()  ));
                    rows.get( rows.size() - 1).add( row.getCell(i).getNumericCellValue()  );
                }
                classes.add( new String( row.getCell(row.getLastCellNum() - 1).getStringCellValue()) );
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void printValues() {
        for (int i = 0; i < values.get(0).size(); i++) {
            System.out.println(
                    values.get(0).get(i) + "\t" + values.get(1).get(i) +
                            "\t" + values.get(2).get(i) + "\t" + values.get(3).get(i)
                + "\t" + classes.get(i)
            );
        }
    }

    public static void main(String[] args) throws IOException, InvalidFormatException {

        try {

            String path = "/home/wl/Downloads/IrisDataTrain.xls";

            // Creating a Workbook from an Excel file (.xls or .xlsx)
            Workbook workbook = WorkbookFactory.create(new File(path));

            // Retrieving the number of sheets in the Workbook
            System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

        /*
           =============================================================
           Iterating over all the sheets in the workbook (Multiple ways)
           =============================================================
        */

            // 1. You can obtain a sheetIterator and iterate over it
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
            System.out.println("Retrieving Sheets using Iterator");
            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                System.out.println("=> " + sheet.getSheetName());
            }

            // 2. Or you can use a for-each loop
            System.out.println("Retrieving Sheets using for-each loop");
            for(Sheet sheet: workbook) {
                System.out.println("=> " + sheet.getSheetName());
            }

            // 3. Or you can use a Java 8 forEach with lambda
            System.out.println("Retrieving Sheets using Java 8 forEach with lambda");
            workbook.forEach(sheet -> {
                System.out.println("=> " + sheet.getSheetName());
            });

        /*
           ==================================================================
           Iterating over all the rows and columns in a Sheet (Multiple ways)
           ==================================================================
        */

            // Getting the Sheet at index zero
            Sheet sheet = workbook.getSheetAt(0);

            // Create a DataFormatter to format and get each cell's value as String
            DataFormatter dataFormatter = new DataFormatter();

            // 1. You can obtain a rowIterator and columnIterator and iterate over them
            System.out.println("\n\nIterating over Rows and Columns using Iterator\n");
            Iterator<Row> rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Now let's iterate over the columns of the current row
                Iterator<Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String cellValue = dataFormatter.formatCellValue(cell);
                    System.out.print(cellValue + "\t");
                }
                System.out.println();
            }

            // 2. Or you can use a for-each loop to iterate over the rows and columns
            System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");
            for (Row row: sheet) {
                for(Cell cell: row) {
                    String cellValue = dataFormatter.formatCellValue(cell);
                    System.out.print(cellValue + "\t");
                }
                System.out.println();
            }

            // 3. Or you can use Java 8 forEach loop with lambda
//            System.out.println("\n\nIterating over Rows and Columns using Java 8 forEach with lambda\n");
//
//            sheet.forEach(row -> {
//                row.forEach(cell -> {
//                    String cellValue = dataFormatter.formatCellValue(cell);
//                    System.out.print(cellValue + "\t");
//                });
//                System.out.println();
//            });

            // Closing the workbook
            workbook.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

//    public ArrayList<Neuron> getInputs() {
//
//        ArrayList<Neuron> inputs= new ArrayList<Neuron>();
//        for (int i = 0; i < this.values.size(); i++) {
//            for (int j = 0; j < this.values.get(i).size(); j++) {
//
//                inputs.add( new Neuron( this.values.get(i).get(j) ));
//
//            }
//        }
//        return  inputs;
//
//    }

    public void stringToIntClasses() {

        int IRIS_SETOSA = 0;
        int IRIS_VERSICOLOR = 1;
        int IRIS_VIRGINICA = 2;

        this.int_classes = new ArrayList<Integer>();
        this.actual_outputs = new ArrayList<Integer>();
        for (int i = 0; i < classes.size(); i++) {

            actual_outputs.add(-1);

            switch (classes.get(i)) {
                case "Iris-setosa" :
                    int_classes.add(IRIS_SETOSA);
                    break;
                case "Iris-versicolor"  :
                    int_classes.add(IRIS_VERSICOLOR);
                    break;
                default:
                    int_classes.add(IRIS_VIRGINICA);
                    break;
            }
        }

    }

    public void setOutputAt(int id, int val) {

        //System.out.println(actual_outputs.size());
        this.actual_outputs.set( id, val );
    }

    public void compareOutputs() {
        Double correctRatio = 0.0;

        System.out.println("    CORRECT\t\t\tACTUAL");
        for (int i = 0; i < int_classes.size(); i++) {

            System.out.print(i + ". ");
            switch ( int_classes.get(i)) {
                case 0 :
                    System.out.printf("Iris-setosa    ");
                    break;
                case 1 :
                    System.out.printf("Iris-versicolor");
                    break;
                case 2 :
                    System.out.printf("Iris-virginica");
                    break;
                default:
                    System.out.printf("NaN ");
                    break;
            }
            System.out.print("\t  ");
            switch ( actual_outputs.get(i)) {
                case 0 :
                    System.out.printf("         Iris-setosa");
                    break;
                case 1 :
                    System.out.printf("         Iris-versicolor");
                    break;
                case 2 :
                    System.out.printf("         Iris-virginica");
                    break;
                default:
                    System.out.printf("         NaN");
                    break;
            }

            if( int_classes.get(i) == actual_outputs.get(i) ) {
                correctRatio += 1.0;

            } else {
                System.out.print( "    X");
            }
            System.out.println();

        }
        System.out.println();
        System.out.println();
        System.out.println("====== RESULT ======= ");
        System.out.println(" " + ((correctRatio  / int_classes.size()) * 100.0)  + " %");
    }
    public Double getRatio() {
        Double correctRatio = 0.0;

        for (int i = 0; i < int_classes.size(); i++) {

            if (int_classes.get(i) == actual_outputs.get(i)) {
                correctRatio += 1.0;
            }
        }
        return (correctRatio / int_classes.size() ) * 100.0;
    }

    /**
     * Max should be global or local ??
     *
     *
     */
    public void normalizeInput() {
        this.normalized_values =  new ArrayList<ArrayList<Double>>();

        Double max_val = 0.1;
        Double min_val = 7.8;
        for (int i = 0; i < rows.size(); i++) {

            ArrayList<Double> list = rows.get(i);

            normalized_values.add( new ArrayList<Double>());
            for (int j = 0; j < list.size(); j++) {

                Double aux = ( list.get(j) - min_val ) / ( max_val - min_val);
                normalized_values.get(normalized_values.size() - 1).add( aux);
            }
        }

    }


}