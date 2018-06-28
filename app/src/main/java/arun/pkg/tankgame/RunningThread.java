package arun.pkg.tankgame;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.Random;

import arun.pkg.tankgame.entities.Soldier;
import arun.pkg.tankgame.util.Util;

public class RunningThread extends Thread {
    private static final String TAG = RunningThread.class.getSimpleName();
    private GameScreenView mGameScreen = null;
    private SurfaceHolder mHolder = null;
    private boolean mRun = false;

    public RunningThread(GameScreenView mGameScreen) {
        this.mGameScreen = mGameScreen;
        mHolder = mGameScreen.getHolder();
    }

    public void setRunning(boolean run) {
        mRun = run;
    }

    public SurfaceHolder getSurfaceHolder() {
        return mHolder;
    }

    private long oldTime = System.currentTimeMillis();

    @SuppressLint("WrongCall")
    @Override
    public void run() {
        Canvas canvas = null;
        while (mRun) {
            canvas = null;
            try {
                canvas = mHolder.lockCanvas(null);
                synchronized (mHolder) {
                    if (System.currentTimeMillis() - oldTime > 5000) {
                        mGameScreen.getSoldierArr().add(getNewSoldier(mGameScreen));
                        oldTime = System.currentTimeMillis();
                    }
                    mGameScreen.onDraw(canvas);
                }
            } finally {
                if (canvas != null) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private Soldier getNewSoldier(GameScreenView screen) {
        Soldier soldier = new Soldier();

        Random r = new Random();
        int xPos = r.nextInt(screen.getMeasuredWidth() + 1);
        soldier.setSoldierXPos(xPos);
        // Log.e(TAG, "soldier xPos : " + xPos);

        int yPos = r.nextInt(screen.getMeasuredHeight() + 1);
        soldier.setSoldierYPos(yPos);
        // Log.e(TAG, "soldier yPos : " + yPos);

        soldier.setSoldierDirection(Util.getRandomDirection());

        return soldier;
    }

}
