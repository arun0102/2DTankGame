package arun.pkg.tankgame.enums;

public enum GameMode {
    GAME_MODE_SURVIVAL(0), GAME_MODE_MISSION(1), GAME_MODE_MULTIPLAYER(2);

    private final int gameMode;

    GameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public int getGameMode() {
        return gameMode;
    }

    public static GameMode getDirection(int gameMode) {
        for (GameMode type : GameMode.values()) {
            if (type.getGameMode() == gameMode) {
                return type;
            }
        }
        return GameMode.GAME_MODE_SURVIVAL;
    }
}
