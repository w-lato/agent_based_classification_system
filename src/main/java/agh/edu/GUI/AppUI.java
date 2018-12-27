package agh.edu.GUI;

import agh.edu.agents.Master;
import agh.edu.agents.RunConf;
import agh.edu.agents.enums.Vote;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.util.List;


public class AppUI extends Application
{


    static ActorRef master;

    private String default_train  = "DATA/mnist_train.arff";
    private String default_test  = "DATA/mnist_test.arff";


    Button conf_path = new Button("Conf path");
    Label path_label = new Label("/CONF/default");
    ComboBox<String > list;

    Button init = new Button("Init");
    ProgressIndicator pb_train = new ProgressBar();
    Button test = new Button("Test");
    ProgressIndicator pb_test = new ProgressBar();

    TableView agents_table = new TableView();
    Label acc;

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
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(s);
            if (file != null) {
                path_label.setText( file.getPath() );
            } else {
                System.out.println(" Nothing selected " );
            }
        });
    }

    private void setInitAction(Button b)
    {
        b.setOnAction(event ->
        {
            pb_train.setProgress(0);
            pb_test.setProgress(0);
            init.setDisable( true );
            test.setDisable( true );
            RunConf rc = ConfParser.getConfFrom( path_label.getText() );
            list.getSelectionModel().select(rc.getClass_method().toString());
            acc.setText("--");
            master.tell( rc, ActorRef.noSender() );
            agents_table.getItems().clear();
//            Thread thread = new Thread(() -> {
//
//            });
//            thread.start();
        });
    }

    private void setTestAction(Button b)
    {
        b.setOnAction(event ->
        {
            init.setDisable( true );
            test.setDisable( true );
            Thread thread = new Thread(() -> {
                master.tell( new Master.EvaluateTest(), ActorRef.noSender() );
            });
            thread.start();
        });
    }

    public void updateProgressOnTrain(double x)
    {
        if( x >= 1.0 )
        {
            pb_train.setProgress(100);
            test.setDisable( false );
            init.setDisable( false );
        } else {
            pb_train.setProgress( x );
        }
    }

    public void updateProgressOnTest(double x)
    {
        if( x >= 1.0 )
        {
            pb_test.setProgress(100);
            test.setDisable( false );
            init.setDisable( false );
        } else {
            pb_test.setProgress( x );
        }
    }


    public void addAgentsToTable(List<SlaveRow> l)
    {
        agents_table.getItems().clear();
        for (SlaveRow x : l)
        {
            agents_table.getItems().addAll(x);
        }
    }

    public void setAccText(double x)
    {
        acc.setText( String.valueOf(x) + "%" );
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
        path_label.setMaxWidth(290);
        gp.add( conf_path, 0,0 );
        gp.add( path_label, 1,0 );
        gp.add( init,0,1);
        gp.add(pb_train,1, 1 );
        pb_train.setMinWidth(290);
        pb_test.setMinWidth(290);
        gp.add( test,0,2);
        gp.add(pb_test,1, 2 );
//        gp.setGridLinesVisible( true ); // TODO

        init.setMinWidth(100);
        init.setAlignment(Pos.CENTER);
        conf_path.setMinWidth(100);
        conf_path.setAlignment(Pos.CENTER);
        test.setMinWidth(100);
        test.setAlignment(Pos.CENTER);
        test.setDisable( true );

        // table
        Label table_title = new Label("Agents");;
        table_title.setFont(new Font("Arial", 20));
        gp.add( table_title, 0, 6  );
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
        gp.add( agents_table,0, 7, 2, 2);
        Group root = new Group();
        Scene scene = new Scene(root, 625, 620);


        //
        Label vote_meth = new Label("Vote strategy:");
        vote_meth.setFont(new Font("Arial", 20));
        gp.add( vote_meth,0, 4);

        ObservableList<String> items = FXCollections.observableArrayList (
                Vote.MAJORITY.name(), Vote.WEIGHTED.name(), Vote.AVERAGE.name()
        );
        list = new ComboBox(items);
        list.setMinWidth(290);
        list.setEditable(false);
        list.getSelectionModel().selectFirst();
        list.getSelectionModel().selectedItemProperty()
            .addListener(new ChangeListener<String>() {
                public void changed(
                        ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    master.tell( new Master.SetVoting(Vote.valueOf(newValue)), ActorRef.noSender() );
                }
            });
        gp.add( list,1, 4);

        Label model_acc = new Label("Model acc.");
        model_acc.setFont(new Font("Arial", 20));
        acc = new Label("--");
        model_acc.setFont(new Font("Arial", 20));
        gp.add( model_acc,0, 5);
        gp.add( acc,1, 5);

        root.getChildren().add( gp );
        primaryStage.setScene(scene);
        primaryStage.show();
        setFileChooserAction( conf_path, primaryStage );
        setInitAction( init );
        setTestAction( test );
    }

    public static void main(String[] args)
    {
        ActorSystem system = ActorSystem.create("testSystem");
        ActorRef m = system.actorOf( Master.props(true) ,"master" );
    }
}
