package okar.entities;

import static okar.entities.WindRose.EAST;
import static okar.entities.WindRose.NORTH;
import static okar.entities.WindRose.SOUTH;
import static okar.entities.WindRose.WEST;

import lombok.Getter;

public enum Direction {

    EAST_WEST(EAST, WEST),
    NORTH_SOUTH(NORTH, SOUTH);

    @Getter
    private final WindRose[] directions;

    Direction(WindRose... directions) {
        this.directions = directions;
    }
}
