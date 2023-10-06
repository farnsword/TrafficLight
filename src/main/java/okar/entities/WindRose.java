package okar.entities;

import java.util.concurrent.ThreadLocalRandom;

public enum WindRose {

    SOUTH,
    NORTH,
    EAST,
    WEST;

    public static WindRose random() {
        return values() [ThreadLocalRandom.current().nextInt(4)];
    }
}
