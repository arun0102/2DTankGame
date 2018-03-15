package arun.pkg.tankgame.enums;

public enum PowerUpsType {
    POWER_DOUBLE_KILL(0), POWER_HEALTH_PLUS_30(1), POWER_IMMUNITY_15_SECS(2), POWER_KILL_ALL_NOW(3), POWER_BULLET_NON_STOP(4);

    private final int power;

    PowerUpsType(int power) {
        this.power = power;
    }

    public int getPower() {
        return power;
    }

    public static PowerUpsType getPowerUpsType(int gameMode) {
        for (PowerUpsType type : PowerUpsType.values()) {
            if (type.getPower() == gameMode) {
                return type;
            }
        }
        return PowerUpsType.POWER_DOUBLE_KILL;
    }
}
