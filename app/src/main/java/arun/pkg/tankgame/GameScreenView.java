package arun.pkg.tankgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import arun.pkg.tankgame.constants.Constants;
import arun.pkg.tankgame.entities.Bullet;
import arun.pkg.tankgame.entities.Direction;
import arun.pkg.tankgame.entities.JoyStickMovedListener;
import arun.pkg.tankgame.entities.Soldier;
import arun.pkg.tankgame.enums.BulletType;
import arun.pkg.tankgame.util.Util;

public class GameScreenView extends SurfaceView implements SurfaceHolder.Callback, JoyStickMovedListener {
    private static final String TAG = GameScreenView.class.getSimpleName();
    private RunningThread mThread = null;
    private Context mContext;
    private JoyStickMovedListener listener;

    private Direction playerDirection = Direction.DIRECTION_UP;
    private float sensitivity = 10;
    private float playerSpeed = Constants.SPEED_HIGH;

    private double touchX, touchY;
    private float handleRadius;
    private float handleInnerBoundaries;
    float playerXPos = 0;
    float playerYPos = 0;
    private boolean isPlayerMove = false;
    private boolean isStartedWithInsideCircle = false;

    private ArrayList<Soldier> soldierArr = new ArrayList<Soldier>();
    private ArrayList<Bullet> bulletPlayerArray = new ArrayList<Bullet>();
    private ArrayList<Bullet> bulletSoldierArray = new ArrayList<Bullet>();

    private Handler mHandler = new Handler();

    public GameScreenView(Context context) {
        super(context);
        init(context);
    }

    public GameScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        getHolder().addCallback(this);
        mThread = new RunningThread(this);
        setOnJostickMovedListener(this);
    }

    public void setOnJostickMovedListener(JoyStickMovedListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (mThread.getSurfaceHolder()) {
            // Log.e(TAG, "onTouchEvent");

            if (1 == event.getPointerCount()) {
                int actionType = event.getAction();
                if (actionType == MotionEvent.ACTION_DOWN) {
                    if (Util.isInsideCircle(getMeasuredWidth() * 4 / 5, getMeasuredHeight() * 5 / 6, (int) event.getX(), (int) event.getY(),
                            getMeasuredWidth() / 10)) {
                        // Log.e(TAG, "touched bullet area");
                        addBulletFromPlayerToBulletArray();
                    } else {
                        // /check joystick area
                        int innerPadding = 20;
                        int px1 = getMeasuredWidth() / 4;
                        int radius1 = px1 - innerPadding;
                        if (Util.isInsideCircle(getMeasuredWidth() * 1 / 4, getMeasuredHeight() * 5 / 6, (int) event.getX(), (int) event.getY(), radius1)) {
                            isStartedWithInsideCircle = true;
                        }
                    }
                } else if (actionType == MotionEvent.ACTION_MOVE) {
                    // / need to check if touching inside circle
                    if (isStartedWithInsideCircle) {
                        float px = getMeasuredWidth() * 1 / 4;
                        float py = getMeasuredHeight() * 5 / 6;
                        float radius = px - handleInnerBoundaries;

                        touchX = (event.getX() - px);
                        touchX = Math.max(Math.min(touchX, radius), -radius);

                        touchY = (event.getY() - py);
                        touchY = Math.max(Math.min(touchY, radius), -radius);

                        if (listener != null) {
                            listener.onJoystickMoved((int) (touchX / radius * sensitivity), (int) (touchY / radius * sensitivity));
                        }
                    }
                } else if (actionType == MotionEvent.ACTION_UP) {
                    Handler handler = new Handler();
                    int numberOfFrames = 5;
                    final double intervalsX = (0 - touchX) / numberOfFrames;
                    final double intervalsY = (0 - touchY) / numberOfFrames;

                    for (int i = 0; i < numberOfFrames; i++) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                touchX += intervalsX;
                                touchY += intervalsY;
                                invalidate();
                            }
                        }, i * 40);
                    }
                    if (listener != null) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                listener.onJoystickReleased();
                                isStartedWithInsideCircle = false;
                            }
                        }, 100);
                    }
                }
            } else if (2 == event.getPointerCount()) {
                // / pointer touch > 1
                int x1 = (int) event.getX(0);
                int y1 = (int) event.getY(0);

                int x2 = (int) event.getX(1);
                int y2 = (int) event.getY(1);

                if (event.getAction() == MotionEvent.ACTION_POINTER_1_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_2_DOWN) {
                    if (Util.isInsideCircle(getMeasuredWidth() * 4 / 5, getMeasuredHeight() * 5 / 6, x1, y1, getMeasuredWidth() / 10)
                            || Util.isInsideCircle(getMeasuredWidth() * 4 / 5, getMeasuredHeight() * 5 / 6, x2, y2, getMeasuredWidth() / 10)) {
                        // Log.e(TAG, "touched bullet area");
                        addBulletFromPlayerToBulletArray();
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Log.e(TAG, "onDraw");
        if (null != canvas) {
            canvas.drawColor(0xffcccccc);

            drawPlayer(canvas, playerDirection);

            drawJoystick(canvas);

            drawBulletThrower(canvas);

            drawSoldiersFromArray(canvas);

            drawBulletsFromArray(canvas);

            super.onDraw(canvas);
        }
    }

    private void drawBulletsFromArray(Canvas canvas) {
        for (int i = 0; i < getBulletSoldierArray().size(); i++) {
            if (isSoldierBulletTouchingPlayer(getBulletSoldierArray().get(i))) {
                // // remove bullet and decrease health
                getBulletSoldierArray().remove(i);

                ((GameScreenActivity) mContext).decreaseHealth(10);
                break;
            }
            drawBullet(canvas, getBulletSoldierArray().get(i));
        }
        boolean isExit2 = false;
        for (int i = 0; i < getBulletPlayerArray().size(); i++) {
            // Log.e(TAG, "bullet array size : " + getBulletArray().size());
            for (int j = 0; j < getSoldierArr().size(); j++) {
                if (isPlayerBulletTouchingSoldier(getBulletPlayerArray().get(i), getSoldierArr().get(j))) {
                    // Log.e(TAG, "soldier is killed now");
                    // // remove bullet and soldier if normal
                    getSoldierArr().remove(j);
                    getBulletPlayerArray().remove(i);

                    ((GameScreenActivity) mContext).incrementPoints(1);

                    isExit2 = true;
                    break;
                }
            }
            if (isExit2) {
                // Log.e(TAG, "exit : "+playerXPos +", "+ playerYPos);
                break;
            }
            drawBullet(canvas, getBulletPlayerArray().get(i));
        }
    }

    private void drawBullet(Canvas canvas, Bullet bullet) {
        // draw bullet bottom rectangle
        Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(Color.DKGRAY);
        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setAntiAlias(true);
        handlePaint.setDither(true);
        handlePaint.setFilterBitmap(true);
        if (Direction.DIRECTION_UP == bullet.getBulletDirection()) {
            RectF bulletRectF = new RectF(bullet.getBulletXPos() - 5, bullet.getBulletYPos() - 5, bullet.getBulletXPos() + 5, bullet.getBulletYPos() + 10);
            canvas.drawRect(bulletRectF, handlePaint);

            // draw bullet top
            Path path = new Path();
            path.moveTo(bulletRectF.left, bulletRectF.top);
            path.lineTo(bulletRectF.left + 5, bulletRectF.top - 10);
            path.lineTo(bulletRectF.right, bulletRectF.top);
            path.lineTo(bulletRectF.left, bulletRectF.top);
            path.close();

            canvas.drawPath(path, handlePaint);
        } else if (Direction.DIRECTION_DOWN == bullet.getBulletDirection()) {
            RectF bulletRectF = new RectF(bullet.getBulletXPos() - 5, bullet.getBulletYPos() + 5, bullet.getBulletXPos() + 5, bullet.getBulletYPos() - 10);
            canvas.drawRect(bulletRectF, handlePaint);

            // draw bullet top
            Path path = new Path();
            path.moveTo(bulletRectF.left, bulletRectF.top);
            path.lineTo(bulletRectF.left + 5, bulletRectF.top + 10);
            path.lineTo(bulletRectF.right, bulletRectF.top);
            path.lineTo(bulletRectF.left, bulletRectF.top);
            path.close();

            canvas.drawPath(path, handlePaint);
        } else if (Direction.DIRECTION_LEFT == bullet.getBulletDirection()) {
            RectF bulletRectF = new RectF(bullet.getBulletXPos() - 5, bullet.getBulletYPos() - 5, bullet.getBulletXPos() + 10, bullet.getBulletYPos() + 5);
            canvas.drawRect(bulletRectF, handlePaint);

            // draw bullet top
            Path path = new Path();
            path.moveTo(bulletRectF.left, bulletRectF.top);
            path.lineTo(bulletRectF.left - 10, bulletRectF.top + 5);
            path.lineTo(bulletRectF.left, bulletRectF.bottom);
            path.lineTo(bulletRectF.left, bulletRectF.top);
            path.close();

            canvas.drawPath(path, handlePaint);
        } else if (Direction.DIRECTION_RIGHT == bullet.getBulletDirection()) {
            RectF bulletRectF = new RectF(bullet.getBulletXPos() - 5, bullet.getBulletYPos() - 5, bullet.getBulletXPos() + 10, bullet.getBulletYPos() + 5);
            canvas.drawRect(bulletRectF, handlePaint);

            // draw bullet top
            Path path = new Path();
            path.moveTo(bulletRectF.right, bulletRectF.top);
            path.lineTo(bulletRectF.right + 10, bulletRectF.top + 5);
            path.lineTo(bulletRectF.right, bulletRectF.bottom);
            path.lineTo(bulletRectF.right, bulletRectF.top);
            path.close();

            canvas.drawPath(path, handlePaint);
        }

        if (Direction.DIRECTION_UP == bullet.getBulletDirection()) {
            if (bullet.getBulletYPos() - bullet.getBulletSpeed() > 0) {
                bullet.setBulletYPos(bullet.getBulletYPos() - bullet.getBulletSpeed());
            } else {
                if (bullet.getBulletType() == BulletType.BULLET_SOLDIER_NORMAL) {
                    getBulletSoldierArray().remove(bullet);
                } else {
                    getBulletPlayerArray().remove(bullet);
                }
            }
        } else if (Direction.DIRECTION_DOWN == bullet.getBulletDirection()) {
            if (bullet.getBulletYPos() + bullet.getBulletSpeed() < getMeasuredHeight()) {
                bullet.setBulletYPos(bullet.getBulletYPos() + bullet.getBulletSpeed());
            } else {
                if (bullet.getBulletType() == BulletType.BULLET_SOLDIER_NORMAL) {
                    getBulletSoldierArray().remove(bullet);
                } else {
                    getBulletPlayerArray().remove(bullet);
                }
            }
        } else if (Direction.DIRECTION_LEFT == bullet.getBulletDirection()) {
            if (bullet.getBulletXPos() - bullet.getBulletSpeed() > 0) {
                bullet.setBulletXPos(bullet.getBulletXPos() - bullet.getBulletSpeed());
            } else {
                if (bullet.getBulletType() == BulletType.BULLET_SOLDIER_NORMAL) {
                    getBulletSoldierArray().remove(bullet);
                } else {
                    getBulletPlayerArray().remove(bullet);
                }
            }
        } else if (Direction.DIRECTION_RIGHT == bullet.getBulletDirection()) {
            if (bullet.getBulletXPos() + bullet.getBulletSpeed() < getMeasuredWidth()) {
                bullet.setBulletXPos(bullet.getBulletXPos() + bullet.getBulletSpeed());
            } else {
                if (bullet.getBulletType() == BulletType.BULLET_SOLDIER_NORMAL) {
                    getBulletSoldierArray().remove(bullet);
                } else {
                    getBulletPlayerArray().remove(bullet);
                }
            }
        }
    }

    private void drawJoystick(Canvas canvas) {
        // // draw joystick

        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.GRAY);
        circlePaint.setStrokeWidth(2);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setDither(true);
        circlePaint.setFilterBitmap(true);

        Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(Color.DKGRAY);
        handlePaint.setStrokeWidth(1);
        handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        handlePaint.setDither(true);
        handlePaint.setFilterBitmap(true);

        float innerPadding = 20;

        float px = getMeasuredWidth() / 6;
        float py = getMeasuredHeight() * 5 / 6;
        float radius = Math.min(px, py);
        handleRadius = (float) (radius * 0.30);
        px = getMeasuredWidth() / 4;
        handleInnerBoundaries = (float) (Math.min(px, py) * 0.30);

        // Draw the background
        canvas.drawCircle(getMeasuredWidth() * 1 / 4, getMeasuredHeight() * 5 / 6, px - innerPadding, circlePaint);

        // Draw the handle
        canvas.drawCircle((float) touchX + getMeasuredWidth() * 1 / 4, (float) touchY + getMeasuredHeight() * 5 / 6, handleRadius, handlePaint);
    }

    private void drawBulletThrower(Canvas canvas) {
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.GRAY);
        circlePaint.setStrokeWidth(2);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setDither(true);
        circlePaint.setFilterBitmap(true);

        // Draw the background
        canvas.drawCircle(getMeasuredWidth() * 4 / 5, getMeasuredHeight() * 5 / 6, getMeasuredWidth() / 10, circlePaint);

        // draw bullet bottom rectangle
        Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(Color.DKGRAY);
        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setAntiAlias(true);
        RectF bulletRectF = new RectF(getMeasuredWidth() * 4 / 5 - 10, getMeasuredHeight() * 5 / 6 - 10, getMeasuredWidth() * 4 / 5 + 10,
                getMeasuredHeight() * 5 / 6 + 15);

        canvas.drawRect(bulletRectF, handlePaint);

        // draw bullet top
        Path path = new Path();
        path.moveTo(bulletRectF.left, bulletRectF.top);
        path.lineTo(bulletRectF.left + 10, bulletRectF.top - 20);
        path.lineTo(bulletRectF.right, bulletRectF.top);
        path.lineTo(bulletRectF.left, bulletRectF.top);
        path.close();

        canvas.drawPath(path, handlePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.e(TAG, "onMeasure : " + playerXPos + ", " + playerYPos);
        if (0 == playerXPos && 0 == playerYPos) {
            float playerWidth = getMeasuredWidth() / 15;
            float playerHeight = getMeasuredHeight() / 15;

            playerXPos = getMeasuredWidth() / 2 - playerWidth / 2;
            playerYPos = getMeasuredHeight() / 2 - playerHeight / 2;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void drawPlayer(Canvas canvas, Direction playerDirection) {
        float playerWidth = getMeasuredWidth() / 15;
        // float playerHeight = getMeasuredHeight() / 15;
        // Log.e(TAG, "" + playerXPos + ", " + playerYPos);
        float leftAdd = 0;
        float topAdd = 0;

        if (Direction.DIRECTION_UP == playerDirection) {
            if (playerYPos - playerWidth / 2 - 8 - 10 - playerSpeed > 0) {
                topAdd = topAdd - playerSpeed;
            }
        } else if (Direction.DIRECTION_LEFT == playerDirection) {
            if (playerXPos - playerWidth / 2 - 8 - 10 - playerSpeed > 0) {
                leftAdd = leftAdd - playerSpeed;
            }
        } else if (Direction.DIRECTION_RIGHT == playerDirection) {
            if (playerXPos + playerWidth / 2 + 8 + 10 + playerSpeed < getMeasuredWidth()) {
                leftAdd = leftAdd + playerSpeed;
            }
        } else if (Direction.DIRECTION_DOWN == playerDirection) {
            if (playerYPos + playerWidth / 2 + 8 + 10 + playerSpeed < getMeasuredHeight()) {
                topAdd = topAdd + playerSpeed;
            }
        }
        if (isPlayerMove) {
            playerXPos = playerXPos + leftAdd;
            playerYPos = playerYPos + topAdd;
        }
        // // background image dimensions
        RectF rectPlayerBackground = new RectF(playerXPos - playerWidth / 2 - 8, playerYPos - playerWidth / 2 - 8, playerXPos + playerWidth / 2 + 8, playerYPos
                + playerWidth / 2 + 8);

        Paint playerPaint = new Paint();
        playerPaint.setDither(true);
        playerPaint.setFilterBitmap(true);
        playerPaint.setStyle(Style.FILL);
        playerPaint.setAntiAlias(true);

        // // draw player background image
        playerPaint.setColor(0xff333333);
        canvas.drawRect(rectPlayerBackground, playerPaint);

        if (Direction.DIRECTION_UP == playerDirection) {
            // // draw left strip
            playerPaint.setColor(0xff666633);
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.top, rectPlayerBackground.left + 8, rectPlayerBackground.bottom, playerPaint);

            // // draw right strip
            canvas.drawRect(rectPlayerBackground.right - 8, rectPlayerBackground.top, rectPlayerBackground.right, rectPlayerBackground.bottom, playerPaint);

            // // draw center circle
            playerPaint.setColor(0xff999999);
            canvas.drawCircle(playerXPos, playerYPos, playerWidth / 2, playerPaint);

            // // draw fire rod
            playerPaint.setColor(0xff666666);
            canvas.drawRect(playerXPos - 4, rectPlayerBackground.top - 4, playerXPos + 4, playerYPos, playerPaint);

            // // draw fire rod top
            playerPaint.setColor(0xff333333);
            canvas.drawRect(playerXPos - 4, rectPlayerBackground.top - 10, playerXPos + 4, rectPlayerBackground.top - 4, playerPaint);

        } else if (Direction.DIRECTION_LEFT == playerDirection) {
            // // draw bottom strip
            playerPaint.setColor(0xff666633);
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.bottom - 8, rectPlayerBackground.right, rectPlayerBackground.bottom, playerPaint);

            // // draw top strip
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.top, rectPlayerBackground.right, rectPlayerBackground.top + 8, playerPaint);

            // // draw center circle
            playerPaint.setColor(0xff999999);
            canvas.drawCircle(playerXPos, playerYPos, playerWidth / 2, playerPaint);

            // // draw fire rod
            playerPaint.setColor(0xff666666);
            canvas.drawRect(rectPlayerBackground.left - 4, playerYPos - 4, playerXPos, playerYPos + 4, playerPaint);

            // // draw fire rod top
            playerPaint.setColor(0xff333333);
            canvas.drawRect(rectPlayerBackground.left - 10, playerYPos - 4, rectPlayerBackground.left - 4, playerYPos + 4, playerPaint);

        } else if (Direction.DIRECTION_RIGHT == playerDirection) {
            // // draw bottom strip
            playerPaint.setColor(0xff666633);
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.bottom - 8, rectPlayerBackground.right, rectPlayerBackground.bottom, playerPaint);

            // // draw top strip
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.top, rectPlayerBackground.right, rectPlayerBackground.top + 8, playerPaint);

            // // draw center circle
            playerPaint.setColor(0xff999999);
            canvas.drawCircle(playerXPos, playerYPos, playerWidth / 2, playerPaint);

            // // draw fire rod
            playerPaint.setColor(0xff666666);
            canvas.drawRect(playerXPos, playerYPos - 4, rectPlayerBackground.right + 4, playerYPos + 4, playerPaint);

            // // draw fire rod top
            playerPaint.setColor(0xff333333);
            canvas.drawRect(rectPlayerBackground.right + 4, playerYPos - 4, rectPlayerBackground.right + 10, playerYPos + 4, playerPaint);
        } else if (Direction.DIRECTION_DOWN == playerDirection) {
            // // draw left strip
            playerPaint.setColor(0xff666633);
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.top, rectPlayerBackground.left + 8, rectPlayerBackground.bottom, playerPaint);

            // // draw right strip
            canvas.drawRect(rectPlayerBackground.right - 8, rectPlayerBackground.top, rectPlayerBackground.right, rectPlayerBackground.bottom, playerPaint);

            // // draw center circle
            playerPaint.setColor(0xff999999);
            canvas.drawCircle(playerXPos, playerYPos, playerWidth / 2, playerPaint);

            // // draw fire rod
            playerPaint.setColor(0xff666666);
            canvas.drawRect(playerXPos - 4, playerYPos, playerXPos + 4, rectPlayerBackground.bottom + 4, playerPaint);

            // // draw fire rod top
            playerPaint.setColor(0xff333333);
            canvas.drawRect(playerXPos - 4, rectPlayerBackground.bottom + 4, playerXPos + 4, rectPlayerBackground.bottom + 10, playerPaint);
        }
    }

    private void drawSoldiersFromArray(Canvas canvas) {
        for (int i = 0; i < getSoldierArr().size(); i++) {
            if (System.currentTimeMillis() - getSoldierArr().get(i).getShootTime() > 3000) {
                addBulletFromSoldierToBulletArray(getSoldierArr().get(i));
                getSoldierArr().get(i).setShootTime(System.currentTimeMillis());
            }
            if (System.currentTimeMillis() - getSoldierArr().get(i).getResetDirectionTime() > 10000) {
                getSoldierArr().get(i).setSoldierDirection(Util.getRandomDirection());
                getSoldierArr().get(i).setResetDirectionTime(System.currentTimeMillis());
            }
            drawSoldier(canvas, getSoldierArr().get(i));
        }
    }

    private synchronized void drawSoldier(Canvas canvas, Soldier soldier) {
        float playerWidth = getMeasuredWidth() / 19;
        // float playerHeight = getMeasuredHeight() / 15;

        float leftAdd = 0;
        float topAdd = 0;

        if (Direction.DIRECTION_UP == soldier.getSoldierDirection()) {
            if (soldier.getSoldierYPos() - playerWidth / 2 - 8 - 10 - soldier.getSoldierSpeed() > 0) {
                topAdd = topAdd - soldier.getSoldierSpeed();
            } else {
                soldier.setSoldierDirection(Util.getRandomDirection());
                return;
            }
        } else if (Direction.DIRECTION_LEFT == soldier.getSoldierDirection()) {
            if (soldier.getSoldierXPos() - playerWidth / 2 - 8 - 10 - soldier.getSoldierSpeed() > 0) {
                leftAdd = leftAdd - soldier.getSoldierSpeed();
            } else {
                soldier.setSoldierDirection(Util.getRandomDirection());
                return;
            }
        } else if (Direction.DIRECTION_RIGHT == soldier.getSoldierDirection()) {
            if (soldier.getSoldierXPos() + playerWidth / 2 + 8 + 10 + soldier.getSoldierSpeed() < getMeasuredWidth()) {
                leftAdd = leftAdd + soldier.getSoldierSpeed();
            } else {
                soldier.setSoldierDirection(Util.getRandomDirection());
                return;
            }
        } else if (Direction.DIRECTION_DOWN == soldier.getSoldierDirection()) {
            if (soldier.getSoldierYPos() + playerWidth / 2 + 8 + 10 + soldier.getSoldierSpeed() < getMeasuredHeight()) {
                topAdd = topAdd + soldier.getSoldierSpeed();
            } else {
                soldier.setSoldierDirection(Util.getRandomDirection());
                return;
            }
        }

        soldier.setSoldierXPos(soldier.getSoldierXPos() + leftAdd);
        soldier.setSoldierYPos(soldier.getSoldierYPos() + topAdd);

        // // background image dimensions
        RectF rectPlayerBackground = new RectF(soldier.getSoldierXPos() - playerWidth / 2 - 8, soldier.getSoldierYPos() - playerWidth / 2 - 8,
                soldier.getSoldierXPos() + playerWidth / 2 + 8, soldier.getSoldierYPos() + playerWidth / 2 + 8);

        Paint playerPaint = new Paint();
        playerPaint.setDither(true);
        playerPaint.setFilterBitmap(true);
        playerPaint.setStyle(Style.FILL);
        playerPaint.setAntiAlias(true);

        // // draw player background image
        playerPaint.setColor(0xff333333);
        canvas.drawRect(rectPlayerBackground, playerPaint);

        if (Direction.DIRECTION_UP == soldier.getSoldierDirection()) {
            // // draw left strip
            playerPaint.setColor(0xff666633);
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.top, rectPlayerBackground.left + 8, rectPlayerBackground.bottom, playerPaint);

            // // draw right strip
            canvas.drawRect(rectPlayerBackground.right - 8, rectPlayerBackground.top, rectPlayerBackground.right, rectPlayerBackground.bottom, playerPaint);

            // // draw center circle
            playerPaint.setColor(0xff999999);
            canvas.drawCircle(soldier.getSoldierXPos(), soldier.getSoldierYPos(), playerWidth / 2, playerPaint);

            // // draw fire rod
            playerPaint.setColor(0xff666666);
            canvas.drawRect(soldier.getSoldierXPos() - 4, rectPlayerBackground.top - 4, soldier.getSoldierXPos() + 4, soldier.getSoldierYPos(), playerPaint);

            // // draw fire rod top
            playerPaint.setColor(0xff333333);
            canvas.drawRect(soldier.getSoldierXPos() - 4, rectPlayerBackground.top - 10, soldier.getSoldierXPos() + 4, rectPlayerBackground.top - 4,
                    playerPaint);

        } else if (Direction.DIRECTION_LEFT == soldier.getSoldierDirection()) {
            // // draw bottom strip
            playerPaint.setColor(0xff666633);
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.bottom - 8, rectPlayerBackground.right, rectPlayerBackground.bottom, playerPaint);

            // // draw top strip
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.top, rectPlayerBackground.right, rectPlayerBackground.top + 8, playerPaint);

            // // draw center circle
            playerPaint.setColor(0xff999999);
            canvas.drawCircle(soldier.getSoldierXPos(), soldier.getSoldierYPos(), playerWidth / 2, playerPaint);

            // // draw fire rod
            playerPaint.setColor(0xff666666);
            canvas.drawRect(rectPlayerBackground.left - 4, soldier.getSoldierYPos() - 4, soldier.getSoldierXPos(), soldier.getSoldierYPos() + 4, playerPaint);

            // // draw fire rod top
            playerPaint.setColor(0xff333333);
            canvas.drawRect(rectPlayerBackground.left - 10, soldier.getSoldierYPos() - 4, rectPlayerBackground.left - 4, soldier.getSoldierYPos() + 4,
                    playerPaint);

        } else if (Direction.DIRECTION_RIGHT == soldier.getSoldierDirection()) {
            // // draw bottom strip
            playerPaint.setColor(0xff666633);
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.bottom - 8, rectPlayerBackground.right, rectPlayerBackground.bottom, playerPaint);

            // // draw top strip
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.top, rectPlayerBackground.right, rectPlayerBackground.top + 8, playerPaint);

            // // draw center circle
            playerPaint.setColor(0xff999999);
            canvas.drawCircle(soldier.getSoldierXPos(), soldier.getSoldierYPos(), playerWidth / 2, playerPaint);

            // // draw fire rod
            playerPaint.setColor(0xff666666);
            canvas.drawRect(soldier.getSoldierXPos(), soldier.getSoldierYPos() - 4, rectPlayerBackground.right + 4, soldier.getSoldierYPos() + 4, playerPaint);

            // // draw fire rod top
            playerPaint.setColor(0xff333333);
            canvas.drawRect(rectPlayerBackground.right + 4, soldier.getSoldierYPos() - 4, rectPlayerBackground.right + 10, soldier.getSoldierYPos() + 4,
                    playerPaint);
        } else if (Direction.DIRECTION_DOWN == soldier.getSoldierDirection()) {
            // // draw left strip
            playerPaint.setColor(0xff666633);
            canvas.drawRect(rectPlayerBackground.left, rectPlayerBackground.top, rectPlayerBackground.left + 8, rectPlayerBackground.bottom, playerPaint);

            // // draw right strip
            canvas.drawRect(rectPlayerBackground.right - 8, rectPlayerBackground.top, rectPlayerBackground.right, rectPlayerBackground.bottom, playerPaint);

            // // draw center circle
            playerPaint.setColor(0xff999999);
            canvas.drawCircle(soldier.getSoldierXPos(), soldier.getSoldierYPos(), playerWidth / 2, playerPaint);

            // // draw fire rod
            playerPaint.setColor(0xff666666);
            canvas.drawRect(soldier.getSoldierXPos() - 4, soldier.getSoldierYPos(), soldier.getSoldierXPos() + 4, rectPlayerBackground.bottom + 4, playerPaint);

            // // draw fire rod top
            playerPaint.setColor(0xff333333);
            canvas.drawRect(soldier.getSoldierXPos() - 4, rectPlayerBackground.bottom + 4, soldier.getSoldierXPos() + 4, rectPlayerBackground.bottom + 10,
                    playerPaint);
        }
    }

    private boolean isPlayerBulletTouchingSoldier(Bullet bullet, Soldier soldier) {
        // Log.e(TAG, "isSoldierTouchingPlayerBullet");
        boolean isTouching = false;
        // if ((Direction.DIRECTION_DOWN == bullet.getBulletDirection() && Direction.DIRECTION_UP == soldier.getSoldierDirection())
        // || (Direction.DIRECTION_LEFT == bullet.getBulletDirection() && Direction.DIRECTION_RIGHT == soldier.getSoldierDirection())
        // || (Direction.DIRECTION_UP == bullet.getBulletDirection() && Direction.DIRECTION_DOWN == soldier.getSoldierDirection())
        // || (Direction.DIRECTION_RIGHT == bullet.getBulletDirection() && Direction.DIRECTION_LEFT == soldier.getSoldierDirection()))
        // {
        // // // means if they are colliding going in opposite directions
        // } else
        // {
        float soldierWidthHeight = getMeasuredWidth() / 19 + 16;
        float soldierLeftTopPointX = soldier.getSoldierXPos() - soldierWidthHeight / 2;
        float soldierLeftTopPointY = soldier.getSoldierYPos() - soldierWidthHeight / 2;

        float soldierRightTopPointX = soldier.getSoldierXPos() + soldierWidthHeight / 2;

        float soldierLeftBottomPointY = soldier.getSoldierYPos() + soldierWidthHeight / 2;

        float bulletTopXPos = 0;
        float bulletTopYPos = 0;

        switch (bullet.getBulletDirection()) {
            case DIRECTION_UP:
                bulletTopXPos = bullet.getBulletXPos() - 5;
                bulletTopYPos = bullet.getBulletYPos() - 5;
                break;
            case DIRECTION_DOWN:
                bulletTopXPos = bullet.getBulletXPos() - 5;
                bulletTopYPos = bullet.getBulletYPos() + 5;
                break;
            case DIRECTION_LEFT:
                bulletTopXPos = bullet.getBulletXPos() - 5;
                bulletTopYPos = bullet.getBulletYPos() - 5;
                break;
            case DIRECTION_RIGHT:
                bulletTopXPos = bullet.getBulletXPos() + 5;
                bulletTopYPos = bullet.getBulletYPos() - 5;
                break;
        }

        if (soldierLeftTopPointX < bulletTopXPos && soldierRightTopPointX > bulletTopXPos) {
            // Log.e(TAG, "inside x range");
            if (soldierLeftTopPointY < bulletTopYPos && soldierLeftBottomPointY > bulletTopYPos) {
                // Log.e(TAG, "soldier touched");
                isTouching = true;
            }
            // }
        }
        return isTouching;
    }

    private boolean isSoldierBulletTouchingPlayer(Bullet bullet) {
        boolean isTouching = false;

        float playerWidthHeight = getMeasuredWidth() / 15 + 16;
        float playerLeftTopPointX = playerXPos - playerWidthHeight / 2;
        float playerLeftTopPointY = playerYPos - playerWidthHeight / 2;

        float playerRightTopPointX = playerXPos + playerWidthHeight / 2;

        float playerLeftBottomPointY = playerYPos + playerWidthHeight / 2;

        float bulletTopXPos = 0;
        float bulletTopYPos = 0;

        switch (bullet.getBulletDirection()) {
            case DIRECTION_UP:
                bulletTopXPos = bullet.getBulletXPos() - 5;
                bulletTopYPos = bullet.getBulletYPos() - 5;
                break;
            case DIRECTION_DOWN:
                bulletTopXPos = bullet.getBulletXPos() - 5;
                bulletTopYPos = bullet.getBulletYPos() + 5;
                break;
            case DIRECTION_LEFT:
                bulletTopXPos = bullet.getBulletXPos() - 5;
                bulletTopYPos = bullet.getBulletYPos() - 5;
                break;
            case DIRECTION_RIGHT:
                bulletTopXPos = bullet.getBulletXPos() + 5;
                bulletTopYPos = bullet.getBulletYPos() - 5;
                break;
        }

        if (playerLeftTopPointX < bulletTopXPos && playerRightTopPointX > bulletTopXPos) {
            // Log.e(TAG, "inside x range");
            if (playerLeftTopPointY < bulletTopYPos && playerLeftBottomPointY > bulletTopYPos) {
                // Log.e(TAG, "player touched");
                isTouching = true;
            }
            // }
        }
        return isTouching;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Log.e(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Log.e(TAG, "surfaceCreated");
        if (!mThread.isAlive()) {
            mThread = new RunningThread(this);
            mThread.setRunning(true);
            mThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        if (mThread.isAlive()) {
            mThread.setRunning(false);
        }
    }

    public void setGameOver() {
        if (mThread.isAlive()) {
            mThread.setRunning(false);
        }
    }

    @Override
    public void onJoystickMoved(int x, int y) {
        // Log.e(TAG, "OnMoved x : " + x + "  y : " + x);
        isPlayerMove = true;
        if (0 < y) {
            if (0 < x) {
                if (y > x) {
                    // Log.e(TAG, "OnMoved going down");
                    playerDirection = Direction.DIRECTION_DOWN;
                } else {
                    // Log.e(TAG, "OnMoved going right");
                    playerDirection = Direction.DIRECTION_RIGHT;
                }
            } else {
                if (y > Math.abs(x)) {
                    // Log.e(TAG, "OnMoved going down");
                    playerDirection = Direction.DIRECTION_DOWN;
                } else {
                    // Log.e(TAG, "OnMoved going left");
                    playerDirection = Direction.DIRECTION_LEFT;
                }
            }
        } else {
            if (0 < x) {
                if (x > Math.abs(y)) {
                    // Log.e(TAG, "OnMoved going right");
                    playerDirection = Direction.DIRECTION_RIGHT;
                } else {
                    // Log.e(TAG, "OnMoved going up");
                    playerDirection = Direction.DIRECTION_UP;
                }
            } else {
                if (Math.abs(x) > Math.abs(y)) {
                    // Log.e(TAG, "OnMoved going left");
                    playerDirection = Direction.DIRECTION_LEFT;
                } else {
                    // Log.e(TAG, "OnMoved going up");
                    playerDirection = Direction.DIRECTION_UP;
                }
            }
        }
    }

    private void addBulletFromPlayerToBulletArray() {
        float playerWidth = getMeasuredWidth() / 15;
        RectF rectPlayerBackground = new RectF(playerXPos - playerWidth / 2 - 8, playerYPos - playerWidth / 2 - 8, playerXPos + playerWidth / 2 + 8, playerYPos
                + playerWidth / 2 + 8);

        Bullet bullet = new Bullet();
        bullet.setBulletDirection(playerDirection);
        bullet.setBulletSpeed(Constants.SPEED_PLAYER_BULLET);
        bullet.setBulletType(BulletType.BULLET_PLAYER_NORMAL);

        if (Direction.DIRECTION_UP == playerDirection) {
            bullet.setBulletXPos(rectPlayerBackground.left + rectPlayerBackground.width() / 2);
            bullet.setBulletYPos(rectPlayerBackground.top - 10);
        } else if (Direction.DIRECTION_DOWN == playerDirection) {
            bullet.setBulletXPos(rectPlayerBackground.left + rectPlayerBackground.width() / 2);
            bullet.setBulletYPos(rectPlayerBackground.bottom + 10);
        } else if (Direction.DIRECTION_LEFT == playerDirection) {
            bullet.setBulletXPos(rectPlayerBackground.left - 10);
            bullet.setBulletYPos(rectPlayerBackground.top + rectPlayerBackground.width() / 2);
        } else if (Direction.DIRECTION_RIGHT == playerDirection) {
            bullet.setBulletXPos(rectPlayerBackground.right + 10);
            bullet.setBulletYPos(rectPlayerBackground.top + rectPlayerBackground.width() / 2);
        }

        getBulletPlayerArray().add(bullet);
    }

    private void addBulletFromSoldierToBulletArray(Soldier soldier) {
        float playerWidth = getMeasuredWidth() / 19;
        RectF rectPlayerBackground = new RectF(soldier.getSoldierXPos() - playerWidth / 2 - 8, soldier.getSoldierYPos() - playerWidth / 2 - 8,
                soldier.getSoldierXPos() + playerWidth / 2 + 8, soldier.getSoldierYPos() + playerWidth / 2 + 8);

        Bullet bullet = new Bullet();
        bullet.setBulletDirection(soldier.getSoldierDirection());
        bullet.setBulletSpeed(Constants.SPEED_SOLDIER_BULLET);
        bullet.setBulletType(BulletType.BULLET_SOLDIER_NORMAL);

        if (Direction.DIRECTION_UP == soldier.getSoldierDirection()) {
            bullet.setBulletXPos(rectPlayerBackground.left + rectPlayerBackground.width() / 2);
            bullet.setBulletYPos(rectPlayerBackground.top - 10);
        } else if (Direction.DIRECTION_DOWN == soldier.getSoldierDirection()) {
            bullet.setBulletXPos(rectPlayerBackground.left + rectPlayerBackground.width() / 2);
            bullet.setBulletYPos(rectPlayerBackground.bottom + 10);
        } else if (Direction.DIRECTION_LEFT == soldier.getSoldierDirection()) {
            bullet.setBulletXPos(rectPlayerBackground.left - 10);
            bullet.setBulletYPos(rectPlayerBackground.top + rectPlayerBackground.width() / 2);
        } else if (Direction.DIRECTION_RIGHT == soldier.getSoldierDirection()) {
            bullet.setBulletXPos(rectPlayerBackground.right + 10);
            bullet.setBulletYPos(rectPlayerBackground.top + rectPlayerBackground.width() / 2);
        }
        getBulletSoldierArray().add(bullet);
    }

    @Override
    public void onJoystickReleased() {
        isPlayerMove = false;
    }

    public ArrayList<Soldier> getSoldierArr() {
        return soldierArr;
    }

    public ArrayList<Bullet> getBulletSoldierArray() {
        return bulletSoldierArray;
    }

    public ArrayList<Bullet> getBulletPlayerArray() {
        return bulletPlayerArray;
    }
}
