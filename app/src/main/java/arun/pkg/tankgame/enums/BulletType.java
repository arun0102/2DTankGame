package arun.pkg.tankgame.enums;

public enum BulletType {
    BULLET_UNKNOWN(-1), BULLET_PLAYER_NORMAL(0), BULLET_SOLDIER_NORMAL(1);

    private final int bulletType;

    BulletType(int bulletType) {
        this.bulletType = bulletType;
    }

    public int getType() {
        return bulletType;
    }

    public static BulletType getBulletType(int type) {
        for (BulletType btype : BulletType.values()) {
            if (btype.getType() == type) {
                return btype;
            }
        }
        return BulletType.BULLET_UNKNOWN;
    }
}
