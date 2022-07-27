package arun.pkg.tankgame.entities;

import arun.pkg.tankgame.constants.Constants;
import arun.pkg.tankgame.enums.SoldierType;

public class Soldier {
    private float soldierXPos = 0;
    private float soldierYPos = 0;
    private float soldierSpeed = Constants.SPEED_LOW;
    private Direction soldierDirection = Direction.DIRECTION_RIGHT;
    private SoldierType soldierType = SoldierType.SOLDIER_NORMAL;
    private int bulletHitCount = 0;
    private long resetDirectionTime = 0;
    private long shootTime = 0;

    public float getSoldierXPos() {
        return soldierXPos;
    }

    public void setSoldierXPos(float soldierXPos) {
        this.soldierXPos = soldierXPos;
    }

    public float getSoldierYPos() {
        return soldierYPos;
    }

    public void setSoldierYPos(float soldierYPos) {
        this.soldierYPos = soldierYPos;
    }

    public float getSoldierSpeed() {
        return soldierSpeed;
    }

    public void setSoldierSpeed(float soldierSpeed) {
        this.soldierSpeed = soldierSpeed;
    }

    public Direction getSoldierDirection() {
        return soldierDirection;
    }

    public void setSoldierDirection(Direction soldierDirection) {
        this.soldierDirection = soldierDirection;
    }

    public long getResetDirectionTime() {
        return resetDirectionTime;
    }

    public void setResetDirectionTime(long resetDirectionTime) {
        this.resetDirectionTime = resetDirectionTime;
    }

    public long getShootTime() {
        return shootTime;
    }

    public void setShootTime(long shootTime) {
        this.shootTime = shootTime;
    }

    public SoldierType getSoldierType() {
        return soldierType;
    }

    public void setSoldierType(SoldierType soldierType) {
        this.soldierType = soldierType;
    }

    public int getBulletHitCount() {
        return bulletHitCount;
    }

    public void setBulletHitCount(int bulletHitCount) {
        this.bulletHitCount = bulletHitCount;
    }
}
