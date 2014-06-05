package com.physicaloid.app.alcoholmeter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AlcoholMeterSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback{

    private static final float DEFAULT_FONT_SIZE = 30.0F;
    private static final float UP_DOWN_SPEED = 0.1F;
    private static final float FONT_SCALE = 0.8F;

    private int mScreenWidthDot;
    private int mScreenHeightDot;

    private SurfaceHolder mSurfaceHolder;
    private Thread mThread;
    private Bitmap mBmBeer;
    private Bitmap mBmBeerGray;

    private int mAlcoholPercentage;
    private String mText;
    private boolean mShowText;

    public AlcoholMeterSurfaceView(Context ctx, SurfaceView sv, String defaultText) {
        super(ctx);

        mSurfaceHolder = sv.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(this);
        setZOrderOnTop(true);

        Resources res = ctx.getResources();
        mBmBeer = BitmapFactory.decodeResource(res, R.drawable.beer_glasses);
        mBmBeerGray = BitmapFactory.decodeResource(res, R.drawable.beer_glasses_gray);

        mAlcoholPercentage = 0;

        mText = defaultText;
        mShowText = true;
    }

    public void setAlcoholPercentage(int value) {
        if(value < 0) {
            mAlcoholPercentage = 0;
        } else if(value > 100) {
            mAlcoholPercentage = 100;
        } else {
            mAlcoholPercentage = value;
        }
    }

    public void setText(String str) {
        mText = str;
    }

    public void showText() {
        mShowText = true;
    }

    public void showPercentage() {
        mShowText = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        mScreenWidthDot = width;
        mScreenHeightDot = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mThread = null;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        Paint paint= new Paint();
        int canvasWidth;
        int canvasHeight;

        Paint fontPaint= new Paint();
        fontPaint.setColor(Color.rgb(20,20,20));
        fontPaint.setTextSize(DEFAULT_FONT_SIZE);
        fontPaint.setTextAlign(Paint.Align.CENTER);
        fontPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC));

        float alcoholP=0;

        while (mThread != null) {

            try {
                canvas = mSurfaceHolder.lockCanvas();

                canvas.drawColor(Color.WHITE);

                // キャンバスのサイズ
                canvasWidth = canvas.getWidth();
                canvasHeight = canvas.getHeight();

                // ビットマップのサイズ
                int bmpWidth = this.mBmBeerGray.getWidth();
                int bmpHeight = this.mBmBeerGray.getHeight();

                // 画面サイズに合う様に縦横スケール値を求める（最大限画面に収まる様に努力する）
                float toCanvasScale = this.calcBitmapScale(canvasWidth, canvasHeight, bmpWidth, bmpHeight);
         
                // キャンバスの大きさに画像を合わせたときにサイズのずれがどれくらいあるか
                float diffX = (bmpWidth * toCanvasScale - canvasWidth);
                float diffY = (bmpHeight * toCanvasScale - canvasHeight);
                 
                // すみを残して中心から取り出す様にする
                float addX = (diffX / toCanvasScale) / 2;
                float addY = (diffY / toCanvasScale) / 2;

                // 画像の切り取り位置を調整して画像の中心がキャンバスの中心に来る様にする
                Rect rSrc = new Rect((int)addX, (int)addY,
                        (int)((canvasWidth / toCanvasScale) + addX), (int)((canvasHeight / toCanvasScale) + addY));
                Rect rDest = new Rect(0, 0, canvasWidth, canvasHeight);

                canvas.drawBitmap(mBmBeerGray, rSrc, rDest, null);

                alcoholP = alcoholP + (mAlcoholPercentage - alcoholP)*UP_DOWN_SPEED;

                rSrc = new Rect((int)addX, (int)(((canvasHeight / toCanvasScale)*(100-alcoholP))/100 + addY),
                        (int)((canvasWidth / toCanvasScale) + addX), (int)((canvasHeight / toCanvasScale) + addY));
                rDest = new Rect(0, (int)((canvasHeight*(100-alcoholP))/100), canvasWidth, canvasHeight);
                canvas.drawBitmap(mBmBeer,rSrc,rDest,null);

                if(mShowText) {
                    fontPaint.setTextSize(DEFAULT_FONT_SIZE);
                    canvas.drawText(mText,canvasWidth/2, canvasHeight/2,fontPaint);
                } else {
                    fontPaint.setTextSize(DEFAULT_FONT_SIZE+FONT_SCALE*alcoholP);
                    canvas.drawText(mAlcoholPercentage+"%",canvasWidth/2, canvasHeight/2,fontPaint);
                }

                mSurfaceHolder.unlockCanvasAndPost(canvas);

                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }

    // ////////////////////////////////////////////////////////////
    // Bitmapの拡大率を出す
    private float calcBitmapScale(int canvasWidth, int canvasHeight, int bmpWidth, int bmpHeight) {
 
        // 最初は幅で調べる
        float scale = (float)canvasWidth / (float)bmpWidth;
        float tmp = bmpHeight * scale;
 
        if (tmp > canvasHeight) {
            scale = (float)canvasHeight / (float)bmpHeight;
            return scale;
        }
         
        return scale;
    }
     
}
