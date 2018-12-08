package agh.edu.learning.AGDS;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class AGDSImpl {

    public static final int SETOSA = 0;
    public static final int VERSI = 1;
    public static final int VIRGIN = 2;

    TreeMap<Double, Node>[] params;
    public ArrayList<Node> R; // rows of data

    // MAX - MIN
    public Double[] param_range;


    // how many times the value is present in given vectors
    int number_of_occurances;



    private Double computeWeight( Node v1, Node v2 , int paramID) {

        return 1.0 - Math.abs( v1.value - v2.value ) / param_range[ paramID ];

    }


    // calculate associate weights from given nodeID
    public void associateFrom( int RNodeID )
    {
        // set 0s to all Xs
        resetAllXs();
        // find all vals associated to given R and set X = 1 and calculated all lefts and rigts
        // and then add weights to connected Rs
        Node n = R.get(RNodeID);
        for (int i = 0; i < n.vals.length; i++)
        {
            iterateOverGroup(  n.vals[i], i );
        }

    }


    private void iterateOverGroup( Double srcVal, int groupID )
    {
        Node src = params[ groupID ].get( srcVal );
        //System.out.println("===========================" + srcVal + "       " + groupID);
        src.X = 1.0;
        addWeightsToR( src );

        // class
        if( groupID == 4 ) return;

        Double prevX = src.X;
        Double prevVal = src.value;
        while( src.left != null )
        {
            src = params[ groupID ].get( src.left );
            src.X = prevX * ( 1.0 -  Math.abs(prevVal - src.value)/param_range[groupID]  );
            addWeightsToR( src );
            prevX = src.X;
            prevVal = src.value;
        }


        src = params[ groupID ].get( srcVal );
        prevX = src.X;
        prevVal = src.value;
        while( src.right != null )
        {
            src = params[ groupID ].get( src.right );
            src.X = prevX * ( 1.0 -  Math.abs(prevVal - src.value)/param_range[groupID]  );
            addWeightsToR( src );
            prevX = src.X;
            prevVal = src.value;
        }


    }

    private void addWeightsToR(Node n) {
        for (int i = 0; i < n.connected_Rs.size(); i++)
        {
            R.get( n.connected_Rs.get(i) ).X += n.X / 5.0;
//            R.get( n.connected_Rs.get(i) ).X += n.X / n.connected_Rs.size();

        }

    }

    // Find the most similar object(s) to the given object or a group of objects
    public void findSimilarObj(ArrayList<ArrayList> rows) {


    }

    public void loadRdata(XLSParser parser)
    {
        this.R = new ArrayList<Node>();
        for (int i = 0; i < parser.rows.size(); i++)
        {
            this.R.add(
                new Node( Node.ROW, parser.rows.get(i), parser.int_classes.get(i) )
            );
        }

    }


    public void loadParamsData(XLSParser parser) {

        this.params = new TreeMap[5];

        this.params[0] = new TreeMap<Double, Node>();
        this.params[1] = new TreeMap<Double, Node>();
        this.params[2] = new TreeMap<Double, Node>();
        this.params[3] = new TreeMap<Double, Node>();
        this.params[4] = new TreeMap<Double, Node>();

        for (int i = 0; i < parser.rows.size(); i++) {
            ArrayList<Double> curr = parser.rows.get(i);
                // find Rs with same values
                for (int j = 0; j < 5; j++) {
                    if(j == 4) {
                        addToSpecifiedGroup( Double.valueOf(parser.int_classes.get(i) ), j );

                    } else {
                        addToSpecifiedGroup( curr.get(j) , j);
                    }
                }
            }

        for (int i = 0; i < 5; i++) {
            for (Map.Entry<Double, Node> e : params[i].entrySet()) {
                params[i].get( e.getKey() ).left = params[i].lowerKey(e.getKey()); // left
                params[i].get( e.getKey() ).right = params[i].higherKey(e.getKey()); // right
            }
        }

    }

    private void addToSpecifiedGroup(Double val, int groupID) {

        if( !params[ groupID ].containsKey( val ) ) {
//                            System.out.println("&&&&&&");
            Node n = new Node( groupID, val  );
            addAllAssociatedRs(n, groupID, val);
            params[ groupID ].put( val, n );
        }
        // UPDATE - CLASS EXISTS WE WANT TO TO ADD NEW R
        Node n = params[ groupID ].get( val );
        n.connected_Rs.clear();
        addAllAssociatedRs(n, groupID, val);

    }

    private void addAllAssociatedRs(Node n, int classID, Double value) {
        //System.out.println("R: " + R.size());
        for (int j = 0; j < this.R.size(); j++) {
          //  System.out.println( this.R.get(j).vals[ classID ] + " == " + value + " ?? " + ( this.R.get(j).vals[ classID ].compareTo(value) ));
            if( this.R.get(j).vals[ classID ].compareTo(value) == 0 ) {
                n.connected_Rs.add( j );
//                System.out.println("ADDED");
            }
        }

    }


    public void addNewR( ArrayList<Double> vals, int classID ) {

        // ADD TO Rs
        Node n = new Node(Node.ROW,  vals, classID  );
        this.R.add( n );


        // ADD TO ATTR
        for (int i = 0; i < vals.size(); i++) {
            addToSpecifiedGroup(vals.get(i), i );

            for (Map.Entry<Double, Node> e : this.params[i].entrySet()) {
                this.params[i].get( e.getKey() ).left = this.params[i].lowerKey(e.getKey()); // left
                this.params[i].get( e.getKey() ).right = this.params[i].higherKey(e.getKey()); // right
            }
        }
        addToSpecifiedGroup( Double.valueOf(classID ), 4 );

    }

    public void addAndClassify( ArrayList<Double> vals ) {

        // ADD TO Rs
        Node n = new Node(Node.ROW);
        n.vals = new Double[vals.size() + 1];
        for (int i = 0; i < vals.size(); i++) {
            n.vals[i] = vals.get(i);
        }
        n.vals[ n.vals.length - 1 ] = Double.NEGATIVE_INFINITY; // we don't know class
        this.R.add( n );


        // ADD TO ATTR
        for (int i = 0; i < vals.size(); i++) {
            addToSpecifiedGroup(vals.get(i), i );

            for (Map.Entry<Double, Node> e : this.params[i].entrySet()) {
                this.params[i].get( e.getKey() ).left = this.params[i].lowerKey(e.getKey()); // left
                this.params[i].get( e.getKey() ).right = this.params[i].higherKey(e.getKey()); // right
            }
        }


        resetAllXs();
        Node last = R.get( R.size() - 1 );
        for (int i = 0; i < n.vals.length - 1; i++) {
            iterateOverGroup(  last.vals[i], i );
        }


        ArrayList<Node> sorted = new ArrayList<Node>();
        for (int i = 0; i < R.size(); i++) {
            sorted.add( R.get(i) );
        }

        sorted.sort((Node z1, Node z2) -> {
            if (z1.X > z2.X)
                return 1;
            if (z1.X < z2.X)
                return -1;
            return 0;
        });
        int[] cls = {0,0,0};

        // take very similar first object
        if( sorted.get( sorted.size() - 2 ).X >= 0.77 )
        {
            R.get( R.size() - 1 ).vals[4] = sorted.get( sorted.size() - 2 ).vals[4];
        }
        // check others for similarity
        else
        {
            for (int i = sorted.size() - 2; i >= sorted.size() - 0.04 * sorted.size(); i--) {

                switch ( (sorted.get(i).vals[4].intValue() ) ) {
                    case AGDSImpl.SETOSA: cls[ 0 ]++; break;
                    case AGDSImpl.VERSI: cls[ 1 ]++; break;
                    default: cls[ 2 ]++; break;
                }
            }

            if ( cls[0] > cls[1] && cls[0] > cls[2]) {

                //System.out.println("SETOSA");
                R.get( R.size() - 1 ).vals[4] = 0.0;
            } else if( cls[1] > cls[0] && cls[1] > cls[2] ) {

                //System.out.println("VERSICOLOR");
                R.get( R.size() - 1 ).vals[4] = 1.0;
            } else {

                // System.out.println("VIRGIN");
                R.get( R.size() - 1 ).vals[4] = 2.0;
            }
        }


    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //
    //                                      CALC

    public void calculateRangesForGroups() {

        param_range = new Double[4];
        for (int i = 0; i < 4; i++) {
            Double max = Double.NEGATIVE_INFINITY;
            Double min = Double.POSITIVE_INFINITY;
            for (Map.Entry<Double, Node> e : params[i].entrySet()) {
                if( e.getKey() < min ) min = e.getKey();
                if( e.getKey() > max ) max = e.getKey();
            }
            param_range[i] = max - min;
        }

    }

    private Double calcWeight( Double v1, Double v2, int groupID ) {

        return 1.0 - Math.abs( v1 - v2 ) / param_range[groupID];

    }


    public void resetAllXs() {
        for (int i = 0; i < R.size(); i++) {
            R.get(i).X = 0.0;
        }
        for (int i = 0; i < 5; i++) {
            for (Map.Entry<Double, Node> e : params[i].entrySet()) {
                params[i].get( e.getKey() ).X = 0.0;
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //
    //                                    PRINTING

    public void printR() {

        for (int i = 0; i < R.size(); i++) {
            System.out.print(i + " " + R.get(i).ID + " ");
            for (int j = 0; j < R.get(i).vals.length; j++) {
                System.out.print( R.get(i).vals[j] + " " );
            }
            System.out.println();
        }

    }
    
    public void printParams() {

        for (int i = 0; i < 5; i++) {
            System.out.println(i);
            for (Map.Entry<Double, Node> e : params[i].entrySet()) {

                Node n = params[i].get( e.getKey() );
                System.out.print( e.getKey() + " : " );
                for (int j = 0; j < n.connected_Rs.size(); j++) {
                    System.out.print(" " + n.connected_Rs.get(j) );
//                    System.out.println( n.connected_Rs.size() );
                }
                System.out.print(" left = " + n.left + " right " + n.right);
                System.out.println();

            }
        }
        
    }

    public void printSimilarRs() {

        ArrayList<Node> sorted = new ArrayList<Node>();
        for (int i = 0; i < R.size(); i++) {
            sorted.add( R.get(i) );
        }

        sorted.sort((Node z1, Node z2) -> {
            if (z1.X > z2.X)
                return 1;
            if (z1.X < z2.X)
                return -1;
            return 0;
        });

        for (int i = sorted.size() - 1; i >= 0; i--) {
            System.out.println(sorted.size() - i  +" : " + " : " + sorted.get(i).ID + " x: " + sorted.get(i).X);
        }

    }
    

    public static void main(String[] args) {
        String path = "/home/vm/Downloads/IrisDataTrain.xls";
        XLSParser parser = new XLSParser();

        parser.readIrisTrainingValues(path);
        parser.stringToIntClasses();

        AGDSImpl agds = new AGDSImpl();
        agds.loadRdata( parser );
        agds.printR();

        System.out.println( agds.R.size() );
        agds.loadParamsData( parser );
        agds.printParams();
        agds.calculateRangesForGroups();

        for (int i = 0; i < 4; i++) {
            System.out.println( agds.param_range[i] );
        }


        // FIND SIMILAR OBJECT TO REPRESENTANTO OF THE CLASS
        // setosa
//        agds.associateFrom( 0 );
//        agds.printSimilarRs();
//
//        for (int i = 0; i < 4; i++) {
//            System.out.println( agds.param_range[i] );
//        }
//
//
//        // versicolor
//        agds.associateFrom( 43 );
//        agds.printSimilarRs();
//
//        // virgin
//        agds.associateFrom( 86 );
//        agds.printSimilarRs();


        System.out.println("=============================" + agds.R.size());
        System.out.println("=============================");

        String path1 = "/home/vm/Downloads/IrisData.xls";
        XLSParser testCheck = new XLSParser();
        testCheck.readIrisTrainingValues(path1);
        testCheck.stringToIntClasses();


        int invalid_ctr = 0;
        for (int i = 0; i < testCheck.rows.size(); i++)
        {
            ArrayList<Double> row = testCheck.rows.get( i );

            agds.addAndClassify( new ArrayList<Double>() {{ // 102
                add( row.get(0));
                add( row.get(1));
                add( row.get(2));
                add( row.get(3));
            }});

            Node x = agds.R.get( agds.R.size() - 1 );
            System.out.print(i + ": " + x.vals[4]  + "  " + testCheck.int_classes.get(i));
            if( (x.vals[4].intValue() != testCheck.int_classes.get(i)))
            {
                System.out.print("    X");
                invalid_ctr++;
            }
            System.out.println();

        }
        System.out.println("RESULT: " + (1.0 - (Double.valueOf( invalid_ctr ) / Double.valueOf( testCheck.rows.size()))) );


//        agds.addNewR( new ArrayList<Double>() {{ // 102
//            add(5.8);
//            add(2.7);
//            add(5.1);
//            add(1.9);
//        }},
//                2
//
//        );
//
//        System.out.println("=============================" + agds.R.size());
//        agds.printParams();
//
//
//        agds.associateFrom( 127 );
//        agds.printSimilarRs();
//
//
//        // 25 4.80,3.40,1.90,0.20,Iris-setosa
//        agds.addAndClassify(
//                new ArrayList<Double>() {{ // 102
//                    add(4.8);
//                    add(3.4);
//                    add(1.9);
//                    add(0.2);
//                }}
//        );
//
//        // 6.90,3.10,4.90,1.50,Iris-versicolor
//        agds.addAndClassify(
//                new ArrayList<Double>() {{
//                    add(6.9);
//                    add(3.1);
//                    add(4.9);
//                    add(1.5);
//                }}
//        );

    }
}
