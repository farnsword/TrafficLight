package okar.comms;

import okar.entities.Direction;
import okar.entities.Vehicle;
import okar.entities.WindRose;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrafficNotifier {

    private static final List<TrafficChangeListener> trafficChangeListeners = new ArrayList<>();
    private static final List<TrafficLightListener> trafficLightListeners = new ArrayList<>();

    private static final List<UserInputListener> userInputListeners = new ArrayList<>();

    public static void registerTrafficChangeListener(TrafficChangeListener listener) {
        trafficChangeListeners.add(listener);
    }

    public static void trafficChanged(Map<WindRose, Map<String, List<Vehicle>>> newTrafficState) {
        trafficChangeListeners.forEach(listener -> listener.vehiclesArrived(newTrafficState));
    }




    public static void registerTrafficLightListener(TrafficLightListener listener) {
        trafficLightListeners.add(listener);
    }

    public static void switchTrafficLight(Direction direction) {
        trafficLightListeners.forEach(listener -> listener.changeDirection(direction));
    }



    public static void registerUserInputListener(UserInputListener listener) {
        userInputListeners.add(listener);
    }

    public static void startTraffic() {
        userInputListeners.forEach(UserInputListener::startTrafficDrivethrough);
    }

    public static void startVehiclesArrival() {
        userInputListeners.forEach(UserInputListener::startVehiclesArrival);
    }
}
