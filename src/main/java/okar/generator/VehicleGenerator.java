package okar.generator;

import okar.entities.Vehicle;

import java.util.concurrent.ThreadLocalRandom;

public class VehicleGenerator {

    public static Vehicle randomVehicle() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return randomVehicle(random.nextInt(4) == 1);
    }

    public static Vehicle randomPublicTransport() {
        return randomVehicle(true);
    }
    public static Vehicle randomRegularTransport() {
        return randomVehicle(false);
    }

    private static Vehicle randomVehicle(boolean isPublicTransport) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        return new Vehicle(isPublicTransport,
                random.nextBoolean(),
                random.nextBoolean());
    }
}
