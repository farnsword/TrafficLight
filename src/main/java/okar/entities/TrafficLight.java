package okar.entities;

import okar.comms.TrafficNotifier;

import java.util.Objects;

public class TrafficLight {

    // 1 seconds to switch
    // 3 seconds minimum work time - time to cross the crossroad

    private static volatile TrafficLight instance;
    private static final Object LOCK = new Object();
    private Direction currentDirection;

    public static TrafficLight getInstance() {
        TrafficLight lockedInstance = instance;
        if (Objects.isNull(lockedInstance)) {
            synchronized (LOCK) {
                lockedInstance = instance;
                if (Objects.isNull(lockedInstance)) {
                    instance = lockedInstance = new TrafficLight();
                }
            }
        }
        return lockedInstance;
    }

    public void changeDirection(Direction direction) {
        this.currentDirection = direction;
        TrafficNotifier.switchTrafficLight(this.currentDirection);
    }
}
