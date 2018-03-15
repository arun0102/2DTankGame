package arun.pkg.tankgame.util;

import java.util.Random;

import arun.pkg.tankgame.entities.Direction;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static Direction getRandomDirection() {
        Random r = new Random();
        int dir = r.nextInt(4);
        // Log.e(TAG, "Random direction : " + dir);

        return Direction.getDirection(dir);
    }

    public static boolean isInsideCircle(int x1, int y1, int x2, int y2, int radius) {
        boolean isInside = false;
        x1 = Math.abs(x1);
        y1 = Math.abs(y1);
        x2 = Math.abs(x2);
        y2 = Math.abs(y2);

        int distanceBetweenPoints = (int) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        if (distanceBetweenPoints < radius) {
            isInside = true;
        }
        return isInside;
    }

}
