package arun.pkg.tankgame.entities;

import arun.pkg.tankgame.constants.Constants;
import arun.pkg.tankgame.enums.BulletType;

public class Bullet {
    private float bulletXPos = 0;
    private float bulletYPos = 0;
    private float bulletSpeed = Constants.SPEED_SOLDIER_BULLET;
    private Direction bulletDirection = Direction.DIRECTION_UNKNOWN;
    private BulletType bulletType = BulletType.BULLET_UNKNOWN;

    public float getBulletXPos() {
        return bulletXPos;
    }

    public void setBulletXPos(float bulletXPos) {
        this.bulletXPos = bulletXPos;
    }

    public float getBulletYPos() {
        return bulletYPos;
    }

    public void setBulletYPos(float bulletYPos) {
        this.bulletYPos = bulletYPos;
    }

    public Direction getBulletDirection() {
        return bulletDirection;
    }

    public void setBulletDirection(Direction bulletDirection) {
        this.bulletDirection = bulletDirection;
    }

    public float getBulletSpeed() {
        return bulletSpeed;
    }

    public void setBulletSpeed(float bulletSpeed) {
        this.bulletSpeed = bulletSpeed;
    }

    public BulletType getBulletType() {
        return bulletType;
    }

    public void setBulletType(BulletType bulletType) {
        this.bulletType = bulletType;
    }
}
