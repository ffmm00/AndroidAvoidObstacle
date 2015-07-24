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
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AvoidObstacleView extends SurfaceView implements SurfaceHolder.Callback,Runnable{


    private static final int GOAL_HEIGHT =150;
    private static final int START_HEIGHT=150;

    private static final int JUMP_HEIGHT=120;

    private static final int OUT_WIDTH=50;
    private static final int CHARA_WIDTH=100;

private int mWidth;
    private int mHeight;

    private boolean mIsGoal=false;
    private boolean mIsGone=false;

    private boolean mIsAttached;
    private Thread mThread;

    private SurfaceHolder mHolder;
    private Canvas mCanvas=null;
    private Paint mPaint=null;

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

    private List<Obstacle> mObstacleList =new ArrayList<Obstacle>(20);

    private Random mRand;

    public AvoidObstacleView(Context context){
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mWidth = getWidth();
        mHeight=getHeight();

        Resources rsc = getResources();
        mBitmapChara= BitmapFactory.decodeResource(rsc,R.mipmap.ic_launcher);
        mBitmapObstacle=BitmapFactory.decodeResource(rsc, R.mipmap.rock);

        mRand =new Random();

    }

    @Override
    public void run(){
        while(mIsAttached){
        }
    }

    private void zoneDecide(){
        mRegionWholeScreen=new Region(0,0,mWidth,mHeight);

        mGoalZone=new Path();
        mGoalZone.addRect(OUT_WIDTH,0,mWidth-OUT_WIDTH,GOAL_HEIGHT,Path.Direction.CW);
        mRegionGoalZone=new Region();
        mRegionGoalZone.setPath(mGoalZone,mRegionWholeScreen);

        mStartZone=new Path();
        mStartZone.addRect(OUT_WIDTH,mHeight-START_HEIGHT,mWidth-OUT_WIDTH,mHeight, Path.Direction.CW);
        mRegionStartZone=new Region();
        mRegionStartZone.setPath(mStartZone,mRegionWholeScreen);

        mOutZoneL=new Path();
        mOutZoneL.addRect(0,0,OUT_WIDTH,mHeight,Path.Direction.CW);
        mRegionOutZoneL=new Region();
        mRegionOutZoneL.setPath(mOutZoneL,mRegionWholeScreen);

        mOutZoneR=new Path();
        mOutZoneR.addRect(mWidth-OUT_WIDTH,0,mWidth,mHeight,Path.Direction.CW);
        mRegionOutZoneR=new Region();
        mRegionOutZoneR.setPath(mOutZoneR,mRegionWholeScreen);

    }

    public void drawGameBoard(){
        if((mIsGone)||(mIsGoal)){
            return;
        }

        mChara.move(MainActivity.role,MainActivity.pitch);
        if(mChara.getBottom()>mHeight){
            mChara.setLocate(mChara.getLeft(),(int)(mHeight-JUMP_HEIGHT));
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

            mCanvas.drawText(getResources().getString(R.string.goal), (int) mWidth / 2 - 50, 100, mPaint);
            mCanvas.drawText(getResources().getString(R.string.start), (int) mWidth / 2 - 50, mHeight - 50, mPaint);

        }
        }
}
