package agh.edu.utils;

import java.util.ArrayList;
import java.util.List;

public class Combination
{
    public static  List<int[]> perms = new ArrayList<>();

    static void combinationUtil(int arr[], int data[], int start,
                                int end, int index, int r)
    {
        // Current combination is ready to be printed, print it
        if (index == r)
        {
            int[] x = new int[ r ];
            for (int j=0; j<r; j++){
                System.out.print(data[j]+" ");
                x[j] = data[j];
            }
            perms.add( x );

            System.out.println("");
            return;
        }

        for (int i=start; i<=end && end-i+1 >= r-index; i++)
        {
            data[index] = arr[i];
            combinationUtil(arr, data, i+1, end, index+1, r);
        }
    }

    static void printCombination(int arr[], int n, int r)
    {
        // A temporary array to store all combination one by one
        int data[]=new int[r];

        // Print all combination using temprary array 'data[]'
        combinationUtil(arr, data, 0, n-1, 0, r);
    }

    public static void main (String[] args)
    {
        int arr[] = {0, 1, 2, 3, 4, 5, 6};
//        int r = ;
        int n = arr.length;
//        printCombination(arr, n, r);

        for (int i = 2; i < 6; i++)
        {
            printCombination( arr, n, i );
        }

        System.out.println( perms.size() );
        for (int i = 0; i < perms.size(); i++) {
            int[] x = perms.get(i);
            for (int i1 = 0; i1 < x.length; i1++)
            {
                System.out.print( x[i1] + ", " );
            }
            System.out.println();
        }
    }
}
