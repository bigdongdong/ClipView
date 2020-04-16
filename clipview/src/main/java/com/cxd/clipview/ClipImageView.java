package com.cxd.clipview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

/**
 * create by cxd on 2020/4/15
 *
 * 图片裁剪view
 * 响应手势，移动，缩放，边界回弹
 * 裁剪后返回原bitmap，或返回宽高压缩的bitmap
 */
public class ClipImageView extends android.support.v7.widget.AppCompatImageView implements SizeView{

    private final String TAG = "ClipImageView";
    private Matrix mMartix ;
    private int W , H ; //view's
    private int dW , dH; //drawablw's
    private int cropW = 500 , cropH = 500; //crop's
    private int wW = 1080 , wH = 1080 ;//window's


    private float mMinScale = 0f; //最小缩放值

    /*有可能出现图片过小，mMinScale > 0 的情况，则保证 mMaxScale 恒 >= mMinScale*/
    private float mMaxScale = 0f; //最大缩放值

    private ValueAnimator mAnimator ; //边界回弹动画

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
            float ldw = (float)wW/(float)dW;
            float ldh = (float)wH/(float)dH;
            mMinScale = Math.max(ldw,ldh);
            mMartix.setScale(mMinScale,mMinScale);
            if(mMinScale == ldh){
                /*横屏图片->移动到中心*/
                int w = (int) (dW * mMinScale);
                mMartix.postTranslate(-(w - W)/2,(H-wH)/2);

            }else{
                /*竖屏图片 -> 移动到中心*/
                int h = (int) (dH * mMinScale);
                mMartix.postTranslate((W-wW)/2,(H - h)/2);
            }

            this.setImageMatrix(mMartix);


            /*计算允许的最大缩放值*/
            mMaxScale = Math.min((float)wW / cropW , (float)wH / cropH);
            mMaxScale = Math.max(mMinScale , mMaxScale);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(dW != 0){ //证明获取到drawable尺寸
            /*上层加遮罩，中心加亮窗*/
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#AA444444"));

            /*亮窗*/
            Path path = new Path();
            path.moveTo(0,0);
            path.lineTo(W,0);
            path.lineTo(W,H);
            path.lineTo(0,H);
            path.rLineTo((W-wW)/2,-(H-wH)/2);
            path.rLineTo(wW,0);
            path.rLineTo(0,-wH);
            path.rLineTo(-wW,0);
            path.close();
            canvas.drawPath(path,paint);

            path.reset();
            path.moveTo(0,0);
            path.lineTo(0,H);
            path.rLineTo((W-wW)/2,-(H-wH)/2);
            path.rLineTo(0,-wH);
            path.close();
            canvas.drawPath(path,paint);

            /*窗框*/
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            path.reset();
            path.moveTo((W-wW)/2,(H-wH)/2);
            path.rLineTo(wW,0);
            path.rLineTo(0,wH);
            path.rLineTo(-wW,0);
            path.close();
            canvas.drawPath(path,paint);
        }

    }


    private boolean canMove = true ;//预防移动和缩放的冲突
    private float lastX = 0f , lastY = 0f ; //移动的坐标
    private float onDownDistance ; //第二根手指放下时，两坐标的距离
    protected float onDownScale ;
    private PointF f1 , f2 ; //两个手指的坐标
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_2_DOWN:
                onDownScale = getMScale() ;

                f1 = new PointF(event.getX(0),event.getY(0)) ;
                f2 = new PointF(event.getX(1),event.getY(1)) ;
                onDownDistance = getFingersCenterDistance(f1,f2);
                canMove = false ;
                break;
            case MotionEvent.ACTION_POINTER_2_UP:
                onDownScale = 0;
                onDownDistance = 0 ;
                canMove = false ;
                break;
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount() == 1 && canMove){
                    /*移动*/
                    float curX = event.getX() ;
                    float curY = event.getY();
                    float dx = curX - lastX ;
                    float dy = curY - lastY ;
                    mMartix.postTranslate(dx,dy);
                    this.setImageMatrix(mMartix);
                    lastX = curX;
                    lastY = curY;
                }else if(event.getPointerCount() == 2){//当前是两个手指
                    f1 = new PointF(event.getX(0),event.getY(0)) ;
                    f2 = new PointF(event.getX(1),event.getY(1)) ;

                    PointF cp = getFingersCenterPoint(f1,f2);
                    int distance = getFingersCenterDistance(f1,f2);

                    /*根据当前二指距离，与初始距离比较，得到缩放比例*/
                    float scale = (float)distance / onDownDistance  * onDownScale ;
                    if(Math.abs(scale - mMinScale) < 0.005f){ //边界值优化
                        scale = mMinScale ;
                    }

                    if(scale >= mMinScale && scale <= mMaxScale){
                        /*使用setScale会将手指中心变成martix中心点从而进行位移*/
                        mMartix.postScale(scale / getMScale(),scale / getMScale(),cp.x,cp.y);
                        this.setImageMatrix(mMartix);
                    }
                }

                break;
                /*最后一根手指离开*/
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boundarySpringBack();
                canMove = true ;
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);

    }

    /*边界回弹*/
    private void boundarySpringBack(){
        /*边界值*/
        int bl = (W-wW)/2;
        int bt = (H-wH)/2;

        /*记录初始位移*/
        final int transx = getMTranslationX();
        final int transy = getMTranslationY();

        int dx = 0 ;
        int dy = 0 ;

        /*脱离了左侧*/
        if(getMTranslationX() > bl){
            dx = bl - getMTranslationX();
        }

        /*脱离了右侧*/
        if(getMTranslationX() + getCurImgWidth() < wW + bl){
            dx = (wW + bl) - (getMTranslationX() + getCurImgWidth()) ;
        }

        /*脱离了上侧*/
        if(getMTranslationY() > bt){
            dy =  bt - getMTranslationY();
        }

        /*脱离了下侧*/
        if(getMTranslationY() + getCurImgHeight() < wH + bt){
            dy = (wH + bt) - (getMTranslationY() + getCurImgHeight()) ;
        }

        if(dx != 0 || dy != 0){
            mAnimator = ValueAnimator.ofObject(new TypeEvaluator<PointF>() {
                @Override
                public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
                    float x = fraction * endValue.x;
                    float y = fraction * endValue.y;
                    return new PointF(x,y);
                }

            },new PointF(0,0), new PointF(dx,dy));
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    PointF p = (PointF) animation.getAnimatedValue();
                    float[] f = new float[9];
                    mMartix.getValues(f);
                    f[2] = transx + p.x ;
                    f[5] = transy + p.y ;
                    mMartix.setValues(f);
                    ClipImageView.this.setImageMatrix(mMartix);
                }
            });

            mAnimator.setDuration(400);
            mAnimator.setInterpolator(new DecelerateInterpolator());
            mAnimator.start();
        }
    }

    private PointF getFingersCenterPoint(PointF f1 , PointF f2){
        return new PointF((f1.x+f2.x)/2,(f1.y+f2.y)/2);
    }

    private int getFingersCenterDistance(PointF f1 , PointF f2){
        return (int) Math.sqrt(Math.pow(f1.x - f2.x,2) + Math.pow(f1.y - f2.y , 2));
    }

    /**
     * 设置裁剪亮窗尺寸
     * @param w
     * @param h
     */
    public void setCropWindowSize(final int w ,final int h){
        cropW = w ;
        cropH = h ;

        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int tempW = (int) (getMeasuredWidth() * 0.85f);
                float b = (float) tempW / w ;
                wW = tempW ;
                wH = (int) (b * h);
            }
        });

    }

    /**
     * 获取裁剪后的bitmap
     * @return
     */
    public Bitmap getCropBitmap(){
        int bl = (W-wW)/2;
        int bt = (H-wH)/2;

        if(mAnimator != null && mAnimator.isRunning() || getMTranslationX() > bl
                ||getMTranslationX() + getCurImgWidth() < wW + bl ||getMTranslationY() > bt
                ||getMTranslationY() + getCurImgHeight() < wH + bt){
            return null ;
        }

        BitmapDrawable bd = (BitmapDrawable) this.getDrawable();
        Bitmap bitmap = bd.getBitmap();

        int x = (int) ((bl - getMTranslationX()) / getMScale());
        int y = (int) ((bt - getMTranslationY()) / getMScale());

        int cropW = (int) (wW / getMScale());
        int cropH = (int) (wH / getMScale());

        bitmap = Bitmap.createBitmap(bitmap,x,y, cropW , cropH); //拿到裁剪的原图

        return bitmap;
    }

    /**
     * 获取裁剪并尺寸压缩后的bitmap
     * @return
     */
    public Bitmap getCropBitmapWithZip(){
        Bitmap bitmap = this.getCropBitmap();
        if(bitmap == null){
            return null ;
        }

        /*对原图进行宽高压缩*/
        Matrix matrix = new Matrix();
        int cropW = (int) (wW / getMScale());
        float scale = (float)this.cropW / cropW ;
        matrix.setScale(scale,scale);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        return bitmap;
    }


    @Override
    public int getCurImgWidth() {
        return (int) (getMScale() * dW);
    }

    @Override
    public int getCurImgHeight() {
        return (int) (getMScale() * dH);
    }

    @Override
    public float getMScale() {
        float[] f = new float[9];
        mMartix.getValues(f);
        return f[0];
    }

    @Override
    public int getMTranslationX() {
        float[] f = new float[9];
        mMartix.getValues(f);
        return (int) f[2];
    }

    @Override
    public int getMTranslationY() {
        float[] f = new float[9];
        mMartix.getValues(f);
        return (int) f[5];
    }
}
