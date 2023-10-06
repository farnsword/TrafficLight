package okar.entities;

import okar.comms.TrafficLightListener;
import okar.comms.TrafficNotifier;
import okar.comms.UserInputListener;
import okar.generator.VehicleGenerator;
import okar.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Crossroad implements TrafficLightListener, UserInputListener {

    private static final int LANES_COUNT = 2;
    private final Map<WindRose, Map<String, List<Vehicle>>> vehiclesQueues = new ConcurrentHashMap<>();
//    private final Map<WindRose, LinkedList<List<Vehicle>>> vehiclesQueues = new ConcurrentHashMap<>();
    private final Object VEHICLES_QUEUES_LOCK = new Object();

    private ScheduledExecutorService scheduledExecutorService;
    private volatile Direction currentDirection = Direction.EAST_WEST;
    private final Object CURRENT_DIRECTION_LOCK = new Object();

    public void start() {
        TrafficNotifier.registerUserInputListener(this);
        TrafficNotifier.registerTrafficLightListener(this);
        initDefaultTraffic();
        scheduledExecutorService = Executors.newScheduledThreadPool(3);
//        startTraffic();
//        scheduleVehicleArrival();
//        tempChangeDirection();
    }

    @Override
    public void startVehiclesArrival() {
        scheduleVehicleArrival();
    }

    @Override
    public void startTrafficDrivethrough() {
//        tempChangeDirection();
        startTraffic();
    }



    public void stop() {
        System.out.println("Stopping the crossroad");
        scheduledExecutorService.shutdownNow();
    }

    private void initDefaultTraffic() {
        vehiclesQueues.put(WindRose.NORTH, initLanes());
        vehiclesQueues.put(WindRose.SOUTH, initLanes());
        vehiclesQueues.put(WindRose.EAST, initLanes());
        vehiclesQueues.put(WindRose.WEST, initLanes());
    }

    private Map<String, List<Vehicle>> initLanes() {
        ConcurrentHashMap<String, List<Vehicle>> lanes = new ConcurrentHashMap<>();

        lanes.put(Constants.PUBLIC_LANE_NAME, new ArrayList<>());
        lanes.put(Constants.REGULAR_LANE_NAME, new ArrayList<>());

        //  TODO: 24.09.2023 for generic lanes number
//        for (int i = 0; i < LANES_COUNT - 1; i++) {
//            lanes.put(REGULAR_LANE_NAME + "_" + i, new ArrayList<>());
//        }

        return lanes;
    }


    @Override
    public void changeDirection(Direction direction) {
        synchronized (CURRENT_DIRECTION_LOCK) {
            this.currentDirection = direction;
        }
    }

    public Direction getCurrentDirection() {
        synchronized (CURRENT_DIRECTION_LOCK) {
            return this.currentDirection;
        }
    }

    public Map<WindRose, Map<String, List<Vehicle>>> getVehiclesQueues() {
        synchronized (VEHICLES_QUEUES_LOCK) {
            return vehiclesQueues;
        }
    }

    private void tempChangeDirection() {
         scheduledExecutorService.scheduleAtFixedRate(() ->
                        changeDirection(Direction.values()[Math.abs(getCurrentDirection().ordinal() - 1)]),
                0,
                4,
                TimeUnit.SECONDS);
    }

    private void startTraffic() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Direction current = getCurrentDirection();
            for (WindRose direction : current.getDirections()) {
                Map<String, List<Vehicle>> lanes = vehiclesQueues.get(direction);
                lanes.forEach((lane, vehiclesInLane) -> {
                    if (!vehiclesInLane.isEmpty()) {
                        vehiclesInLane.remove(0);
                    }
                });
                synchronized (VEHICLES_QUEUES_LOCK) {
                    TrafficNotifier.trafficChanged(vehiclesQueues);
                }
//                printCurrentTraffic();


//                if (Objects.nonNull(vehicles) && !vehicles.isEmpty()) {
//                    vehicles.pop();
//                    System.out.println();
//                    System.out.println("Vehicle went through from - " + direction);
//                    System.out.println();
//                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void printCurrentTraffic() {

        System.out.printf("     | %3d | %3d |     |     |     %n",
                getCurrentLaneLoad(WindRose.NORTH, Constants.PUBLIC_LANE_NAME),
                getCurrentLaneLoad(WindRose.NORTH, Constants.REGULAR_LANE_NAME)); // north
        System.out.println("_____|_____|_____|_____|_____|_____");
        System.out.printf("     |     |     |     |     | %d%n",
                getCurrentLaneLoad(WindRose.EAST, Constants.PUBLIC_LANE_NAME)); // east
        System.out.println("-----|-----|-----|-----|-----|----");
        System.out.printf("     |     |     |     |     | %d%n",
                getCurrentLaneLoad(WindRose.EAST, Constants.REGULAR_LANE_NAME)); // east
        System.out.println("-----|-----|-----|-----|-----|----");
        System.out.printf(" %3d |     |     |     |     |    %n",
                getCurrentLaneLoad(WindRose.WEST, Constants.REGULAR_LANE_NAME)); // west
        System.out.println("-----|-----|-----|-----|-----|----");
        System.out.printf(" %3d |     |     |     |     |    %n",
                getCurrentLaneLoad(WindRose.WEST, Constants.PUBLIC_LANE_NAME)); // west
        System.out.println("_____|_____|_____|_____|_____|_____");
        System.out.println("     |     |     |     |     |     ");
        System.out.printf("     |     |     | %3d | %3d |     %n",
                getCurrentLaneLoad(WindRose.SOUTH, Constants.REGULAR_LANE_NAME),
                getCurrentLaneLoad(WindRose.SOUTH, Constants.PUBLIC_LANE_NAME)); // south
        System.out.println();


//        |  7 | 9 |    |    |
//    ____|____|___|____|____|____
//        |    |   |    |    | 3
//    ----|----|---|----|----|----
//        |    |   |    |    | 4
//    ----|----|---|----|----|----
//      6 |    |   |    |    |
//    ----|----|---|----|----|----
//      1 |    |   |    |    |
//    ____|___ |___|____|____|____
//        |    |   |    |    |
//        |    |   |  5 |  1 |
    }

    private int getCurrentLaneLoad(WindRose direction, String lane) {
        return vehiclesQueues.get(direction).get(lane).size();
    }

    private void scheduleVehicleArrival() {
        scheduledExecutorService.scheduleAtFixedRate(this::vehiclesArrive, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Generates vehicles for the number of lanes defined by {@link #LANES_COUNT}.
     * For public transport lane - 75% chance of arrival of a single vehicle.
     * For other transport lanes - 100% chance of arrival of 1-5 vehicles.
     */
    private void vehiclesArrive() {
        WindRose direction = WindRose.random();
        synchronized (VEHICLES_QUEUES_LOCK) {

            if (ThreadLocalRandom.current().nextInt(4) != 0) {
                vehiclesQueues.get(direction).get(Constants.PUBLIC_LANE_NAME).add(VehicleGenerator.randomPublicTransport());
                System.out.println("Public + 1");
            }

            int regularCarsNumber = ThreadLocalRandom.current().nextInt(6);
            for (int i = 1; i < regularCarsNumber; i++) {
                vehiclesQueues.get(direction).get(Constants.REGULAR_LANE_NAME).add(VehicleGenerator.randomRegularTransport());
            }
            System.out.println("Regular + " + regularCarsNumber);

//            for generic lanes number
//            for (int i = 0; i < LANES_COUNT - 1; i++) {
//                // generate 1-7 regular vehicles
//            }
        }

        System.out.println();
//        System.out.println(LocalTime.now() + " - Vehicles arrived: " + vehicle + " from " + direction);
        getVehiclesQueues().forEach((w, v) -> System.out.println(w + " : " + v.get(Constants.PUBLIC_LANE_NAME).size() + " / " + v.get(Constants.REGULAR_LANE_NAME).size()));
        System.out.println();

        TrafficNotifier.trafficChanged(getVehiclesQueues());
    }
}
