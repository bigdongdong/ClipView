package com.cxd.clipview_demo;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.PopupWindow;

/**
 * Create by cxd on 2019/11/25
 *
 * 自带渐变阴影的popupwindow
 * 默认阴影透明度是50%，渐变动画持续200ms
 */
public abstract class ShadowPopupWindow extends PopupWindow {
    protected final String TAG = "ShadowPopWindow_TAG";

    protected Activity context ;
    protected View view ;
    private ValueAnimator animator ;

    /**
     * 如果需要对pop进行一些设置，比如动态设置宽度，设置弹出动画，
     * 需要在构造器中设置，在onCreateView中设置无效
     * @param context
     */
    public ShadowPopupWindow(Activity context) {
        super(context);
        this.context = context ;

        if(getLayoutIdOrView() instanceof Integer){
            view = LayoutInflater.from(context).inflate((Integer) getLayoutIdOrView(),null,false);
        }else if(getLayoutIdOrView() instanceof View){
            view = (View)  getLayoutIdOrView();
        }

        this.setContentView(view);
        this.onCreateView(view);

        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //设置null会造成5.0及以下点击外部不消失问题

        //以下解决5.0及以下不显示问题
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        animator = new ValueAnimator();
        animator.setInterpolator(new AccelerateDecelerateInterpolator()); //设置动画为先加速在减速(开始速度最快 逐渐减慢)
        animator.setDuration(getAnimatorDuration());
        animator.addUpdateListener(
                value -> setBackgroundAlpha((float)value.getAnimatedValue())
        );
    }

    protected abstract void onCreateView(View view);

    protected abstract Object getLayoutIdOrView();

    public void showCenteral(int layoutId){
        this.showAtLocation(LayoutInflater.from(context).inflate(layoutId,null,false),
                Gravity.CENTER,0,0);
    }

    public void showCenteral(View parent){
        this.showAtLocation(parent, Gravity.CENTER,0,0);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if(context != null && context.isFinishing() == false){
            super.showAtLocation(parent, gravity, x, y);
            animator.setFloatValues(1.0f,getAnimatorAlpha());
            animator.start();
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if(context != null && context.isFinishing() == false){
            super.showAsDropDown(anchor, xoff, yoff, gravity);
            animator.setFloatValues(1.0f,getAnimatorAlpha());
            animator.start();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        animator.setFloatValues(getAnimatorAlpha(),1.0f);
        animator.start();
    }

    /**
     * 设置背景渐变的持续时间
     * @return
     */
    protected  long getAnimatorDuration(){
        return 200 ;
    }

    /**
     * 取值范围: 0.0f - 1.0f
     * 0.0f是不透明，1.0f是全透明
     * @return
     */
    protected  float getAnimatorAlpha(){
        return 0.5f ;
    }


    /**
     * 设置页面的透明度
     * @param bgAlpha 1.0f表示全透明  0.0f表示不透明
     */
    private void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        context.getWindow().setAttributes(lp);
    }

}
