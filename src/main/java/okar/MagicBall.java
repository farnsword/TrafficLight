package okar;

import static javafx.scene.paint.Color.LIGHTGREEN;
import static javafx.scene.paint.Color.TOMATO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import okar.comms.TrafficChangeListener;
import okar.comms.TrafficLightListener;
import okar.comms.TrafficNotifier;
import okar.entities.Direction;
import okar.entities.Vehicle;
import okar.entities.WindRose;
import okar.util.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MagicBall extends Application implements TrafficLightListener, TrafficChangeListener {


    private final List<Label> north = new ArrayList<>();
    private final List<Label> south = new ArrayList<>();
    private final List<Label> east = new ArrayList<>();
    private final List<Label> west = new ArrayList<>();

    private final Timer timer = new Timer();

    private final Label trafficLightSwitchTimer = new Label("0");
    private double trafficLightSwitchCounter = 0.0;
    private final DecimalFormat DF = new DecimalFormat("0.0");


    public static void launchGUI(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        TrafficNotifier.registerTrafficLightListener(this);
        TrafficNotifier.registerTrafficChangeListener(this);

        //        6 * 6
        GridPane grid = prepareGrid();
        List<Node> controls = prepareControls();
        HBox statistics = prepareStatistics();

        VBox vbox = new VBox(grid, statistics);
        vbox.getChildren().addAll(controls);

        Scene scene = new Scene(vbox);
        primaryStage.setTitle("Crossroad");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane prepareGrid() {
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                Label label = new Label();
                label.setMinWidth(50);
                label.setMinHeight(50);
                label.setTextAlignment(TextAlignment.CENTER);
                label.setAlignment(Pos.CENTER);
                grid.add(label, i, j);

                if ((i == 1 || i == 2) && j == 0) {
                    north.add(label);
                } else if ((j == 3 || j == 4) && i == 0) {
                    west.add(label);
                } else if ((j == 1 || j == 2) && i == 5) {
                    east.add(label);
                } else if ((i == 3 || i == 4) && j == 5) {
                    south.add(label);
                } else if ((i == 0 || i == 5) && (j == 0 || j == 5)) {
                    turn(label, Color.GREY);
                }
            }
        }

        return grid;
    }

    private List<Node> prepareControls() {
        Button startVehiclesArrivalButton = new Button("Start vehicles arrival");
        startVehiclesArrivalButton.setMaxWidth(Double.MAX_VALUE);

        Button startTrafficButton = new Button("Start traffic drivethrough");
        startTrafficButton.setMaxWidth(Double.MAX_VALUE);
        startTrafficButton.setDisable(true);

        startVehiclesArrivalButton.setOnAction(event -> {
            TrafficNotifier.startVehiclesArrival();
            startVehiclesArrivalButton.setDisable(true);
            startTrafficButton.setDisable(false);
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    trafficLightSwitchCounter += 0.1;
                    Platform.runLater(() -> trafficLightSwitchTimer.setText(
                            String.valueOf(DF.format(trafficLightSwitchCounter))));
                }
            };
            timer.schedule(task, 0, 100);
        });

        startTrafficButton.setOnAction(event -> {
            TrafficNotifier.startTraffic();
            startTrafficButton.setDisable(true);
        });

        List<Node> controls = new ArrayList<>();
        controls.add(startVehiclesArrivalButton);
        controls.add(startTrafficButton);

        return controls;
    }

    private HBox prepareStatistics() {

        Label statsLabel = new Label("Last traffic light switch (seconds ago): ");
        return new HBox(statsLabel, trafficLightSwitchTimer);
    }

    @Override
    public void vehiclesArrived(Map<WindRose, Map<String, List<Vehicle>>> newTrafficState) {

        Platform.runLater(() -> {

//            ScaleTransition scaleTransition = new ScaleTransition();
//
//            scaleTransition.setDuration(Duration.millis(300));
//
//            scaleTransition.setNode(north.get(0));
//
//            scaleTransition.setByY(1.5);
//            scaleTransition.setByX(1.5);
//
//            scaleTransition.setCycleCount(1);
//
//            scaleTransition.setAutoReverse(true);
//
//            scaleTransition.play();


            north.get(0).setText(String.valueOf(newTrafficState.get(WindRose.NORTH).get(Constants.PUBLIC_LANE_NAME).size()));
            north.get(1).setText(String.valueOf(newTrafficState.get(WindRose.NORTH).get(Constants.REGULAR_LANE_NAME).size()));

            south.get(0).setText(String.valueOf(newTrafficState.get(WindRose.SOUTH).get(Constants.REGULAR_LANE_NAME).size()));
            south.get(1).setText(String.valueOf(newTrafficState.get(WindRose.SOUTH).get(Constants.PUBLIC_LANE_NAME).size()));

            east.get(0).setText(String.valueOf(newTrafficState.get(WindRose.EAST).get(Constants.PUBLIC_LANE_NAME).size()));
            east.get(1).setText(String.valueOf(newTrafficState.get(WindRose.EAST).get(Constants.REGULAR_LANE_NAME).size()));

            west.get(0).setText(String.valueOf(newTrafficState.get(WindRose.WEST).get(Constants.REGULAR_LANE_NAME).size()));
            west.get(1).setText(String.valueOf(newTrafficState.get(WindRose.WEST).get(Constants.PUBLIC_LANE_NAME).size()));
        });
    }

    @Override
    public void changeDirection(Direction direction) {

        System.out.println("new direction is " + direction);

        if (direction == Direction.EAST_WEST) {
            north.forEach(l -> turn(l, TOMATO));
            south.forEach(l -> turn(l, TOMATO));
            east.forEach(l -> turn(l, LIGHTGREEN));
            west.forEach(l -> turn(l, LIGHTGREEN));
        } else {
            east.forEach(l -> turn(l, TOMATO));
            west.forEach(l -> turn(l, TOMATO));
            north.forEach(l -> turn(l, LIGHTGREEN));
            south.forEach(l -> turn(l, LIGHTGREEN));
        }

        trafficLightSwitchCounter = 0.0;
        Platform.runLater(() -> trafficLightSwitchTimer.setText(
                String.valueOf(DF.format(trafficLightSwitchCounter))));
    }

    private void turn(Label label, Color color) {
        label.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        timer.cancel();
        timer.purge();
    }
}
