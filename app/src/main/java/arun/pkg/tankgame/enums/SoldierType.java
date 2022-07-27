package arun.pkg.tankgame.enums;

public enum SoldierType {
    SOLDIER_NORMAL(0), SOLDIER_MEDIUM(1), SOLDIER_HARD(2), SOLDIER_SMART(3);

    private final int type;

    SoldierType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static SoldierType getSoldierType(int type) {
        for (SoldierType sType : SoldierType.values()) {
            if (sType.getType() == type) {
                return sType;
            }
        }
        return SoldierType.SOLDIER_NORMAL;
    }
}
