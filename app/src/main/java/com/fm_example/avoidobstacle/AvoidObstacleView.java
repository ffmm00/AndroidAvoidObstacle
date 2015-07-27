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
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

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

    private boolean touchWall = false;

    private boolean mIsAttached;
    private Thread mThread;

    private SurfaceHolder mHolder;
    private Canvas mCanvas = null;
    private Paint mPaint = null;

    private Path mGoalZone;
    private Path mStartZone;
    private Path mOutZoneL;
    private Path mOutZoneR;
    private Path mWallLowerLeft;
    private Path mWallUpperLeft;
    private Path mWallLowerRight;
    private Path mWallUpperRight;
    private Path mWallCenter;
    private Path mWallStartBlock;

    private Region mRegionGoalZone;
    private Region mRegionStartZone;
    private Region mRegionOutZoneL;
    private Region mRegionOutZoneR;
    private Region mRegionWallLowerLeft;
    private Region mRegionWallUpperLeft;
    private Region mRegionWallLowerRight;
    private Region mRegionWallUpperRight;
    private Region mRegionWallCenter;
    private Region mRegionWallStartBlock;

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

        mWallLowerRight = new Path();
        mWallLowerRight.addRect(mWidth / 2, mHeight * 3 / 4 - 80, mWidth / 2 + 200, mHeight * 3 / 4 - 30, Path.Direction.CW);
        mRegionWallLowerRight = new Region();
        mRegionWallLowerRight.setPath(mWallLowerRight, mRegionWholeScreen);

        mWallLowerLeft = new Path();
        mWallLowerLeft.addRect(OUT_WIDTH, mHeight * 3 / 4 - 90, OUT_WIDTH + 200, mHeight * 3 / 4 - 40, Path.Direction.CW);
        mRegionWallLowerLeft = new Region();
        mRegionWallLowerLeft.setPath(mWallLowerLeft, mRegionWholeScreen);

        mWallUpperRight = new Path();
        mWallUpperRight.addRect(mWidth - 50, mHeight / 4, mWidth - 300, mHeight / 4 - 50, Path.Direction.CW);
        mRegionWallUpperRight = new Region();
        mRegionWallUpperRight.setPath(mWallUpperRight, mRegionWholeScreen);

        mWallUpperLeft = new Path();
        mWallUpperLeft.addRect(mWidth / 4 - 50, mHeight / 5, mWidth / 4 + 200, mHeight / 5 + 60, Path.Direction.CW);
        mRegionWallUpperLeft = new Region();
        mRegionWallUpperLeft.setPath(mWallUpperLeft, mRegionWholeScreen);

        mWallCenter = new Path();
        mWallCenter.addRect(mWidth / 2 - 130, mHeight / 2, mWidth / 2 + 70, mHeight / 2 + 50, Path.Direction.CW);
        mRegionWallCenter = new Region();
        mRegionWallCenter.setPath(mWallCenter, mRegionWholeScreen);

        mWallStartBlock = new Path();
        mWallStartBlock.addRect(mWidth / 2, mHeight - START_HEIGHT - 10, mWidth - 50, mHeight - START_HEIGHT - 25, Path.Direction.CW);
        mRegionWallStartBlock = new Region();
        mRegionWallStartBlock.setPath(mWallStartBlock, mRegionWholeScreen);

    }


    public void drawGameBoard() {
        if ((mIsGone) || (mIsGoal)) {
            return;
        }

        mChara.move(MainActivity.role, MainActivity.pitch);
        if (mChara.getBottom() > mHeight) {
            mChara.setLocate(mChara.getLeft(), (mHeight - JUMP_HEIGHT));
        }

        try {
            for (Obstacle obstacle : mObstacleList) {
                if (obstacle != null) {
                    obstacle.move();
                }
            }

            Path[] WallZone = {mWallUpperRight, mWallUpperLeft, mWallLowerLeft, mWallLowerRight,
                    mWallCenter, mWallStartBlock};

            Region[] RegionZone = {mRegionWallLowerLeft, mRegionWallLowerRight, mRegionWallUpperLeft,
                    mRegionWallUpperRight, mRegionWallStartBlock, mRegionWallCenter};

            mCanvas = getHolder().lockCanvas();
            mCanvas.drawColor(Color.LTGRAY);

            mPaint.setColor(Color.MAGENTA);
            mCanvas.drawPath(mGoalZone, mPaint);
            mPaint.setColor(Color.GRAY);

            mCanvas.drawPath(mStartZone, mPaint);

            mPaint.setColor(Color.BLACK);
            mCanvas.drawPath(mOutZoneL, mPaint);
            mCanvas.drawPath(mOutZoneR, mPaint);

            for (int i = 0; i < WallZone.length; i++)
                mCanvas.drawPath(WallZone[i], mPaint);

            mPaint.setTextSize(50);
            mCanvas.drawText(getResources().getString(R.string.goal), mWidth / 2 - 50, 100, mPaint);
            mCanvas.drawText(getResources().getString(R.string.start), mWidth / 2 - 50, mHeight - 50, mPaint);


            if (mRegionOutZoneL.contains(mChara.getCenterX(), mChara.getCenterY())) {
                mIsGone = true;
                mPaint.setColor(Color.LTGRAY);
                for (int i = 0; i < WallZone.length; i++)
                    mCanvas.drawPath(WallZone[i], mPaint);
                String msg = failed();
                mPaint.setColor(Color.BLACK);
                mCanvas.drawText(msg, mWidth / 3, mHeight / 2, mPaint);
            }

            if (mRegionOutZoneR.contains(mChara.getCenterX(), mChara.getCenterY())) {
                mIsGone = true;
                mPaint.setColor(Color.LTGRAY);
                for (int i = 0; i < WallZone.length; i++)
                    mCanvas.drawPath(WallZone[i], mPaint);
                String msg = failed();
                mPaint.setColor(Color.BLACK);
                mCanvas.drawText(msg, mWidth / 3, mHeight / 2, mPaint);
            }

            for (int i = 0; i < RegionZone.length; i++) {
                if (RegionZone[i].contains(mChara.getCenterX(), mChara.getCenterY())) {
                    mIsGone = true;
                    mPaint.setColor(Color.LTGRAY);
                    for (int m = 0; m < WallZone.length; m++)
                        mCanvas.drawPath(WallZone[m], mPaint);
                    String msg = wall();
                    mPaint.setColor(Color.BLACK);
                    mCanvas.drawText(msg, mWidth / 3, mHeight / 2, mPaint);
                }
            }

            if (mRegionGoalZone.contains(mChara.getCenterX(), mChara.getCenterY())) {
                mIsGoal = true;
                mPaint.setColor(Color.LTGRAY);
                for (int i = 0; i < WallZone.length; i++)
                    mCanvas.drawPath(WallZone[i], mPaint);
                String msg = goaled();
                mPaint.setColor(Color.BLACK);
                mCanvas.drawText(msg, mWidth / 2 - 50, mHeight / 2, mPaint);
            }

            for (Obstacle obstacle : mObstacleList) {
                if (mRegionStartZone.contains(obstacle.getLeft(), obstacle.getBottom())) {
                    obstacle.setLocate(obstacle.getLeft(), 0);
                }
            }

            if (!mIsGoal) {
                for (Obstacle obstacle : mObstacleList) {
                    if (mChara.collisionCheck(obstacle)) {
                        mPaint.setColor(Color.LTGRAY);
                        for (int i = 0; i < WallZone.length; i++)
                            mCanvas.drawPath(WallZone[i], mPaint);
                        String msg = getResources().getString(R.string.collision);
                        mPaint.setColor(Color.BLACK);
                        mCanvas.drawText(msg, mWidth / 3, mHeight / 2, mPaint);
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

    private String failed() {
        return "コースアウトしました";
    }

    private String wall() {
        return "壁に衝突しました";
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

            int speed = mRand.nextInt(4) + 1;
            obstacle = new Obstacle(left, top, mBitmapObstacle.getWidth(), mBitmapObstacle.getHeight(), speed);
            mObstacleList.add(obstacle);
        }
    }

}