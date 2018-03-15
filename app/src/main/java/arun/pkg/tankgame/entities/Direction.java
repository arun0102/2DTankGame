package arun.pkg.tankgame.entities;

public enum Direction {
    DIRECTION_UNKNOWN(-1), DIRECTION_LEFT(0), DIRECTION_UP(1), DIRECTION_RIGHT(2), DIRECTION_DOWN(3);

    private final int direction;

    Direction(int direction) {
        this.direction = direction;
    }

    public int getResponseCode() {
        return direction;
    }

    public static Direction getDirection(int direction) {
        for (Direction type : Direction.values())
            if (type.getResponseCode() == direction)
                return type;

        return Direction.DIRECTION_UNKNOWN;
    }
}
