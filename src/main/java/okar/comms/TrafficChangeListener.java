package okar.comms;

import okar.entities.WindRose;
import okar.entities.Vehicle;

import java.util.List;
import java.util.Map;

public interface TrafficChangeListener {
    void vehiclesArrived(Map<WindRose, Map<String, List<Vehicle>>> newTrafficState);
}
