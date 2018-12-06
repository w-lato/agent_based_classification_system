package agh.edu.learning.AGDS;

import java.util.ArrayList;
import java.util.List;

public class Node {

    // INT VALUES OF GROUPS
    public static final int SLE = 0;
    public static final int SWL = 1;
    public static final int PLE = 2;
    public static final int PWL = 3;
    public static final int CLASS = 4;
    public static final int ROW = 4;

    public static int ID_COUNTER = 0;

    // uniqe id
    int ID;

    // indicates to which group the node belongs to
    int groupID;

    // for rows data
    Double value;

    // temporary value which is used to compute similarities
    Double X = 0.0;

    // how many times the value is present in given vectors
    int number_of_occurances;

    // when node is R
    Double[] vals;

    // when Node is a value from row an we are looking for connected Rs
    List<Integer> connected_Rs;

    // left and right neighbour for values
    Double left;
    Double right;




    public Node( int groupID ) {

        ID = ID_COUNTER;
        ID_COUNTER++;

        this.groupID = groupID;
    }

    public Node( int groupID, Double value ) {

        ID = ID_COUNTER;
        ID_COUNTER++;

        this.groupID = groupID;
        this.value = value;

        this.connected_Rs = new ArrayList<>();
    }

    public Node( int groupID, ArrayList<Double> vals, int cls) {

        ID = ID_COUNTER;
        ID_COUNTER++;

        this.groupID = groupID;

        this.vals = new Double[5];
        for (int i = 0; i < vals.size(); i++) {
                this.vals[i] = vals.get(i);
        }
        this.vals[4] = new Double( cls );
    }


}
