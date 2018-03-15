package arun.pkg.tankgame.enums;

public enum Level {
    LEVEL_BEGINNER(0), LEVEL_MEDIUM(1), LEVEL_HARD(2), LEVEL_EXPERT(3);

    private final int level;

    Level(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static Level getDirection(int level) {
        for (Level type : Level.values()) {
            if (type.getLevel() == level) {
                return type;
            }
        }
        return Level.LEVEL_BEGINNER;
    }
}
