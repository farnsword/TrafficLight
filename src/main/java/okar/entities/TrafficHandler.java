package okar.entities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okar.comms.TrafficChangeListener;
import okar.comms.TrafficNotifier;
import okar.util.Constants;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrafficHandler implements TrafficChangeListener {

    private static final int MIN_TRAFFIC_LIGHT_TIME = 3; // seconds
    private Direction currentDirection;
    private Instant lastDirectionChange = Instant.now();

    public static void initialize() {
        TrafficNotifier.registerTrafficChangeListener(new TrafficHandler());
    }

    @Override
    public void vehiclesArrived(Map<WindRose, Map<String, List<Vehicle>>> newTrafficState) {
        // compare vehicles count
        int publicComparisonResult = compareDirectionsVehiclesCount(newTrafficState, Constants.PUBLIC_LANE_NAME);
        Direction newDirection = null;
        if (publicComparisonResult < 0) {
            newDirection = Direction.NORTH_SOUTH;
        } else if (publicComparisonResult > 0) {
            newDirection = Direction.EAST_WEST;
        } else {
            int regularComparisonResult = compareDirectionsVehiclesCount(newTrafficState, Constants.REGULAR_LANE_NAME);
            if (regularComparisonResult < 0) {
                newDirection = Direction.NORTH_SOUTH;
            } else if (regularComparisonResult > 0) {
                newDirection = Direction.EAST_WEST;
            }
            // will not change the direction if the regular transport count is equals
        }

        if (this.currentDirection != newDirection
                && this.lastDirectionChange.until(Instant.now(), ChronoUnit.SECONDS) >= MIN_TRAFFIC_LIGHT_TIME) {
            this.currentDirection = newDirection;
            this.lastDirectionChange = Instant.now();
            TrafficLight.getInstance().changeDirection(this.currentDirection);
        }
    }

    private int compareDirectionsVehiclesCount(Map<WindRose, Map<String, List<Vehicle>>> trafficState, String lane) {
        Map<String, List<Vehicle>> east = trafficState.get(WindRose.EAST);
        Map<String, List<Vehicle>> west = trafficState.get(WindRose.WEST);

        Integer horizontal = east.get(lane).size() + west.get(lane).size();


        Map<String, List<Vehicle>> north = trafficState.get(WindRose.NORTH);
        Map<String, List<Vehicle>> south = trafficState.get(WindRose.SOUTH);

        Integer vertical = north.get(lane).size() + south.get(lane).size();

//        horizontal < vertical = -1
//        horizontal > vertical = 1

        return horizontal.compareTo(vertical);
    }
}
