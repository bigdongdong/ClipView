package com.cxd.clipview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

public class ClipImageView extends android.support.v7.widget.AppCompatImageView {

    private final String TAG = "ClipImageView";

    private Matrix mMartix ; //只控制布局的Martix
    private int W , H , //view's
            dW , dH; //drawablw's
    private float mScale ;


    public ClipImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setClickable(true);
        this.setScaleType(ScaleType.MATRIX);

        mMartix = new Matrix();
        mMartix.set(this.getMatrix());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if(this.getDrawable() != null){
            W = getMeasuredWidth();
            H = getMeasuredHeight();
            dW = this.getDrawable().getIntrinsicWidth();
            dH = this.getDrawable().getIntrinsicHeight();

            /*缩放*/
            float ldw = (float)lW/(float)dW;
            float ldh = (float)lH/(float)dH;
            mScale = Math.max(ldw,ldh);
            mMartix.postScale(mScale,mScale);

            if(mScale == ldh){
                /*横屏图片->移动到中心*/
                int w = (int) (dW * mScale);
                mMartix.postTranslate(-(w - W)/2,(H-lH)/2);
            }else{
                /*竖屏图片 -> 移动到中心*/
                int h = (int) (dH * mScale);
                mMartix.postTranslate((W-lW)/2,(H - h)/2);
            }

            this.setImageMatrix(mMartix);
        }

    }

    /*TODO 根据手机尺寸做微调*/
    private final int lW  = 1080 ;
    private final int lH  = 1080 ;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(dW != 0){ //证明获取到drawable尺寸
            /*上层加遮罩，中心加亮窗*/
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#88333333"));

            /*亮窗*/
            Path path = new Path();
            path.moveTo(0,0);
            path.lineTo(W,0);
            path.lineTo(W,H);
            path.lineTo(0,H);
            path.rLineTo((W-lW)/2,-(H-lH)/2);
            path.rLineTo(lW,0);
            path.rLineTo(0,-lH);
            path.rLineTo(-lW,0);
            path.close();
            canvas.drawPath(path,paint);

            path.reset();
            path.moveTo(0,0);
            path.lineTo(0,H);
            path.rLineTo((W-lW)/2,-(H-lH)/2);
            path.rLineTo(0,-lH);
            path.close();
            canvas.drawPath(path,paint);

            /*窗框*/
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            path.reset();
            path.moveTo((W-lW)/2,(H-lH)/2);
            path.rLineTo(lW,0);
            path.rLineTo(0,lH);
            path.rLineTo(-lW,0);
            path.close();
            canvas.drawPath(path,paint);
        }

    }


    private float lastX = 0f , lastY = 0f ;

    private float lastFingerDistance ;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "ACTION_DOWN: ");
                lastX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount() == 1){
                    float curX = event.getX() ;
                    float curY = event.getY();
                    float dx = curX - lastX ;
                    float dy = curY - lastY ;
                    mMartix.postTranslate(dx,dy);
                    this.setImageMatrix(mMartix);
                    lastX = curX;
                    lastY = curY;
                }else if(event.getPointerCount() == 2){//判断当前是否有两个手指
                    PointF finger1 = new PointF(event.getX(0),event.getY(0));
                    PointF finger2 = new PointF(event.getX(1),event.getY(1));

                    float finger1X = event.getX(0);
                    float finger2X = event.getX(1);
                    float curD = Math.abs(finger2X - finger1X);
//                    Log.i(TAG, "finger2X - finger1X: "+Math.abs(finger2X - finger1X));

                    if(curD < lastFingerDistance){
                        /*缩小*/
                        mScale = 0.98f ;
                        Log.i(TAG, "缩小: ");
                    }else {
                        mScale = 1.02f ;
                        Log.i(TAG, "放大: ");
                    }

                    float px = (float) Math.sqrt((double)finger1.x * finger1.x + finger2.x * finger2.x);
                    float py = (float) Math.sqrt(finger1.y * finger1.y + finger2.y * finger2.y);
                    mMartix.postScale(mScale,mScale,px,py);
                    this.setImageMatrix(mMartix);
                    lastFingerDistance = curD ;
                }

                break;
                /*最后一根手指离开*/
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boundarySpringBack();
//                Log.i(TAG, "ACTION_UP:  ="+finger1);
                break;
//                /*前一根手指，离开后再落下会触发*/
//            case MotionEvent.ACTION_POINTER_DOWN:
////                Log.i(TAG, "ACTION_POINTER_DOWN: ");
//                break;
//                /*前一根落下的手指离开时调用（如果前一根没离开，后一根离开不会调用）*/
//            case MotionEvent.ACTION_POINTER_UP:
////                Log.i(TAG, "ACTION_POINTER_UP: "+finger2);
//                break;
        }

        return super.onTouchEvent(event);

    }

    /*边界回弹*/
    private void boundarySpringBack(){
        /*边界值*/
        int lleft = (W-lW)/2;
        int lTop = (H-lH)/2;

        float[] f = new float[9] ;
        mMartix.getValues(f);

        Float dx = null ;
        Float dy = null ;

        int transx = (int) f[2];
        if(transx > lleft){
            /*脱离了左侧*/
            dx = (lleft - transx)*1.0f;
        }

        if(transx + getCurImgWidth() < lW + lleft){
            /*脱离了右侧*/
            dx = (lW + lleft) - (transx + getCurImgWidth()) ;
        }

        int transy = (int) f[5];
        if(transy > lTop){
            /*脱离了上侧*/
            dy = (lTop - transy )*1.0f;
        }

        if(transy + getCurImgHeight() < lH + lTop){
            /*脱离了下侧*/
            dy = (lH + lTop) - (transy + getCurImgHeight()) ;
        }

        if(dx != null){
            mMartix.postTranslate(dx,0);
        }
        if(dy != null){
            mMartix.postTranslate(0,dy);
        }

        ClipImageView.this.setImageMatrix(mMartix);

    }

    private float getCurImgWidth(){
        return mScale * dW ;
    }
    private float getCurImgHeight(){
        return mScale * dH ;
    }

}
