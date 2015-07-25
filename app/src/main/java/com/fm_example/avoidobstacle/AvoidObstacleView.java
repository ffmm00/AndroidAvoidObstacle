package com.fm_example.avoidobstacle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AvoidObstacleView extends SurfaceView implements SurfaceHolder.Callback, Runnable {


    private static final int GOAL_HEIGHT = 150;
    private static final int START_HEIGHT = 150;

    private static final int JUMP_HEIGHT = 120;

    private static final int OUT_WIDTH = 50;
    private static final int CHARA_POS = 100;

    private int mWidth;
    private int mHeight;

    private boolean mIsGoal = false;
    private boolean mIsGone = false;

    private boolean mIsAttached;
    private Thread mThread;

    private SurfaceHolder mHolder;
    private Canvas mCanvas = null;
    private Paint mPaint = null;

    private Path mGoalZone;
    private Path mStartZone;
    private Path mOutZoneL;
    private Path mOutZoneR;

    private Region mRegionGoalZone;
    private Region mRegionStartZone;
    private Region mRegionOutZoneL;
    private Region mRegionOutZoneR;

    private Region mRegionWholeScreen;

    private long startTime;
    private long endTime;

    private Bitmap mBitmapChara;
    private Character mChara;

    private Bitmap mBitmapObstacle;
    private Obstacle mObstacle;

    private List<Obstacle> mObstacleList = new ArrayList<Obstacle>(20);

    private Random mRand;

    public AvoidObstacleView(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mWidth = getWidth();
        mHeight = getHeight();

        Resources rsc = getResources();
        mBitmapChara = BitmapFactory.decodeResource(rsc, R.mipmap.ic_launcher);
        mBitmapObstacle = BitmapFactory.decodeResource(rsc, R.mipmap.rock);

        mRand = new Random();

        zoneDecide();

        newChara();
        newObstacle();

        mIsAttached = true;
        mThread = new Thread(this);
        mThread.start();

    }

    @Override
    public void run() {
        while (mIsAttached) {
            drawGameBoard();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mRegionStartZone.contains((int) event.getX(), (int) event.getY())) {
                    newChara();
                    newObstacle();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void zoneDecide() {
        mRegionWholeScreen = new Region(0, 0, mWidth, mHeight);

        mGoalZone = new Path();
        mGoalZone.addRect(OUT_WIDTH, 0, mWidth - OUT_WIDTH, GOAL_HEIGHT, Path.Direction.CW);
        mRegionGoalZone = new Region();
        mRegionGoalZone.setPath(mGoalZone, mRegionWholeScreen);

        mStartZone = new Path();
        mStartZone.addRect(OUT_WIDTH, mHeight - START_HEIGHT, mWidth - OUT_WIDTH, mHeight, Path.Direction.CW);
        mRegionStartZone = new Region();
        mRegionStartZone.setPath(mStartZone, mRegionWholeScreen);

        mOutZoneL = new Path();
        mOutZoneL.addRect(0, 0, OUT_WIDTH, mHeight, Path.Direction.CW);
        mRegionOutZoneL = new Region();
        mRegionOutZoneL.setPath(mOutZoneL, mRegionWholeScreen);

        mOutZoneR = new Path();
        mOutZoneR.addRect(mWidth - OUT_WIDTH, 0, mWidth, mHeight, Path.Direction.CW);
        mRegionOutZoneR = new Region();
        mRegionOutZoneR.setPath(mOutZoneR, mRegionWholeScreen);

    }

    public void drawGameBoard() {
        if ((mIsGone) || (mIsGoal)) {
            return;
        }

        mChara.move(MainActivity.role, MainActivity.pitch);
        if (mChara.getBottom() > mHeight) {
            mChara.setLocate(mChara.getLeft(),(mHeight - JUMP_HEIGHT));
        }

        try {
            for (Obstacle obstacle : mObstacleList) {
                if (obstacle != null) {
                    obstacle.move();
                }
            }
            mCanvas = getHolder().lockCanvas();
            mCanvas.drawColor(Color.LTGRAY);

            mPaint.setColor(Color.MAGENTA);
            mCanvas.drawPath(mGoalZone, mPaint);
            mPaint.setColor(Color.GRAY);
            mCanvas.drawPath(mStartZone, mPaint);
            mPaint.setColor(Color.BLACK);
            mCanvas.drawPath(mOutZoneL, mPaint);
            mCanvas.drawPath(mOutZoneR, mPaint);

            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(50);

            mCanvas.drawText(getResources().getString(R.string.goal), mWidth / 2 - 50, 100, mPaint);
            mCanvas.drawText(getResources().getString(R.string.start),mWidth / 2 - 50, mHeight - 50, mPaint);

            if (mRegionOutZoneL.contains(mChara.getCenterX(), mChara.getCenterY())) {
                mIsGone = true;
            }
            if (mRegionOutZoneR.contains(mChara.getCenterX(), mChara.getCenterY())) {
                mIsGone = true;
            }
            if (mRegionGoalZone.contains(mChara.getCenterX(), mChara.getCenterY())) {
                mIsGoal = true;
                String msg = goaled();
                mPaint.setColor(Color.WHITE);
                mCanvas.drawText(msg, OUT_WIDTH + 10, GOAL_HEIGHT - 100, mPaint);
            }

            for (Obstacle obstacle : mObstacleList) {
                if (mRegionStartZone.contains(obstacle.getLeft(), obstacle.getBottom())) {
                    obstacle.setLocate(obstacle.getLeft(), 0);
                }
            }

            if (!mIsGoal) {
                for (Obstacle obstacle : mObstacleList) {
                    if (mChara.collisionCheck(obstacle)) {
                        String msg = getResources().getString(R.string.collision);
                        mPaint.setColor(Color.WHITE);
                        mCanvas.drawText(msg, OUT_WIDTH + 10, GOAL_HEIGHT - 100, mPaint);
                        mIsGone = true;
                    }
                }
            }

            if (!((mIsGone) || (mIsGoal))) {
                mPaint.setColor(Color.DKGRAY);
                for (Obstacle obstacle : mObstacleList) {
                    mCanvas.drawBitmap(mBitmapObstacle, obstacle.getLeft(), obstacle.getTop(), null);
                }

                mCanvas.drawBitmap(mBitmapChara, mChara.getLeft(), mChara.getTop(), null);
            }

            getHolder().unlockCanvasAndPost(mCanvas);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String goaled() {
        endTime = System.currentTimeMillis();
        long erapsedTime = endTime - startTime;
        int secTime = (int) (erapsedTime / 1000);
        return ("Goal!" + secTime + "秒");
    }


    public void surfaceChanged(SurfaceHolder arg0, int ard1, int arg2, int arg3) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mBitmapChara != null) {
            mBitmapChara.recycle();
            mBitmapChara = null;
        }
        if (mBitmapObstacle != null) {
            mBitmapObstacle.recycle();
            mBitmapObstacle = null;
        }
        mIsAttached = false;
        while (mThread.isAlive()) ;
    }


    private void newChara() {
        mChara = new Character(CHARA_POS, mHeight - JUMP_HEIGHT, mBitmapChara.getWidth(), mBitmapChara.getHeight());
        mIsGoal = false;
        mIsGone = false;
        startTime = System.currentTimeMillis();
    }

    private void newObstacle() {
        Obstacle obstacle;
        mObstacleList.clear();

        for (int i = 0; i < 20; i++) {
            int left = mRand.nextInt(mWidth - (OUT_WIDTH * 2 + mBitmapObstacle.getWidth())) + OUT_WIDTH;
            int top = mRand.nextInt(mHeight - mBitmapObstacle.getHeight() * 2);

            int speed = mRand.nextInt(3) + 1;
            obstacle = new Obstacle(left, top, mBitmapObstacle.getWidth(), mBitmapObstacle.getHeight(), speed);
            mObstacleList.add(obstacle);
        }
    }

}