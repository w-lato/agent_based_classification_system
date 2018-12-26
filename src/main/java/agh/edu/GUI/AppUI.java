package agh.edu.GUI;

import agh.edu.agents.Master;
import agh.edu.agents.RunConf;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;


public class AppUI extends Application
{


    static ActorRef master;
    ProgressIndicator pb = new ProgressBar();
    private String default_train  = "DATA/mnist_train.arff";
    private String default_test  = "DATA/mnist_test.arff";


    Button conf_path = new Button("Conf path");
    Label path_label = new Label("/CONF/default");

    Button init = new Button("Init");
    Button test = new Button("Test");
    TableView agents_table = new TableView();

    public static AppUI self;
    public AppUI() { self = this; }
    public  synchronized static AppUI getInstance() {
        if (self == null)
        {
            Thread thread = new Thread(() -> Application.launch(AppUI.class));
            thread.start();
            while (self == null)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return self;
    }

    public void setMaster(ActorRef master)
    {
        this.master = master;
    }

    private void setFileChooserAction( Button b, Stage s )
    {
        b.setOnAction(event ->
        {
            master.tell("ASD", ActorRef.noSender());
//            FileChooser chooser = new FileChooser();
//            File file = chooser.showOpenDialog(s);
//            if (file != null) {
//                String fileAsString = file.toString();
//                System.out.println( fileAsString );
////                chosen.setText("Chosen: " + fileAsString);
//            } else {
//                System.out.println(" NUTHIN" );
////                chosen.setText(null);
//            }
        });
    }

    private void setInitAction(Button b)
    {
        b.setOnAction(event ->
        {
            test.setDisable( true );
            pb.setProgress(0);
            RunConf rc = ConfParser.getConfFrom( path_label.getText() );
            master.tell( rc, ActorRef.noSender() );
        });
    }

    public void updateProgressOnTrain(double x)
    {
        if( x == 1.0 )
        {
            pb.setProgress(100);
            test.setDisable( false );
        } else {
            pb.setProgress( x );
        }
    }

//    public void train()
//    {
//        RunConf rc = ConfParser.getConfFrom( "CONF/default" );
//        master.tell( rc, ActorRef.noSender() );
//        pi.setProgress(0);
//        pi.setVisible(true);
//    }
//
//    public void setTrainTofinished()
//    {
//        pi.setProgress(100);
//    }
//
//
//    public void hideTrain()
//    {
//        pi.setVisible( false );
//        pi.setProgress( 100 );
//    }

    public Instances getTrain() throws Exception
    {
        return getFileContent( default_train );
    }

    public Instances getTest() throws Exception
    {
        return getFileContent( default_test );
    }

    private Instances getFileContent(String path) throws Exception
    {
        ConverterUtils.DataSource src = new ConverterUtils.DataSource( path );
        return src.getDataSet();
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("Agent-based classification");
        GridPane gp = new GridPane();
        gp.setHgap(15);
        gp.setVgap(15);
        gp.setPadding(new Insets(10));
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        column1.setHalignment( HPos.LEFT );
//        column1.setMaxWidth( 200 );
//        column1.setHgrow( Priority.ALWAYS );
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        column2.setHalignment( HPos.LEFT);
//        column2.setHgrow( Priority.ALWAYS );
        gp.getColumnConstraints().addAll(column1, column2);
        conf_path.setAlignment( Pos.CENTER_LEFT );
        gp.add( conf_path, 0,0 );
        gp.add( path_label, 1,0 );
        gp.add( init,0,1);
        gp.add( pb ,1, 1 );
        pb.setMinWidth(288);
        gp.add( test,0,2);
        gp.setGridLinesVisible( true );

        init.setMinWidth(100);
        conf_path.setMinWidth(100);
        test.setMinWidth(100);

        // table
        Label table_title = new Label("Agents");;
        table_title.setFont(new Font("Arial", 20));
        gp.add( table_title, 0, 4  );
        agents_table.setEditable( false );
        TableColumn ID = new TableColumn("ID");
        ID.setCellValueFactory( new PropertyValueFactory<>("ID"));
        TableColumn type = new TableColumn("Type");
        type.setCellValueFactory( new PropertyValueFactory<>("type"));
        TableColumn eval_rate = new TableColumn("Eval. rate");
        eval_rate.setCellValueFactory( new PropertyValueFactory<>("eval"));
        ID.setMinWidth( 200 );
        type.setMinWidth( 200 );
        eval_rate.setMinWidth( 200 );
        agents_table.getColumns().addAll( ID, type, eval_rate );
        gp.add( agents_table,0, 5, 2, 2);
        agents_table.getItems().addAll( new SlaveRow("asd","asd",123.32) );
        agents_table.getItems().addAll( new SlaveRow("asd","asd",123.32) );
        agents_table.getItems().addAll( new SlaveRow("asd","asd",123.32) );
        Group root = new Group();
        Scene scene = new Scene(root, 625, 620);

        root.getChildren().add( gp );
        primaryStage.setScene(scene);
        primaryStage.show();
        setInitAction( init );
    }

    public static void main(String[] args)
    {
        ActorSystem system = ActorSystem.create("testSystem");
        ActorRef m = system.actorOf( Master.props(true) ,"master" );
    }
}
