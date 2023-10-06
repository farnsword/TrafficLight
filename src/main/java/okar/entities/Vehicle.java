package okar.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class Vehicle {

    private final boolean isPublicTransport;

    private final boolean isRightTurnLightEnabled;
    private final boolean isLeftTurnLightEnabled;
}
