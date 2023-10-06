package okar;

import okar.entities.Crossroad;
import okar.entities.TrafficHandler;

public class TimeContinuum {

    public static void main(String[] args) {
        System.out.println("Continuum: START");
        //  TODO: 24.09.2023 start handler and traffic light as well?
        Crossroad crossroad = new Crossroad();
        TrafficHandler.initialize();
        crossroad.start();
        MagicBall.launchGUI(args);
        crossroad.stop();
    }
}
