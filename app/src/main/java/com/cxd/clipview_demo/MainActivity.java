package com.cxd.clipview_demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.cxd.clipview.ClipImageView;

public class MainActivity extends AppCompatActivity {

    private ClipImageView clipImageView ;
    private Button cropButton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        clipImageView = findViewById(R.id.clipImageView);
        clipImageView.setCropWindowSize(400,400);

        cropButton = findViewById(R.id.cropButton);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bm =clipImageView.getCropBitmapWithZip() ;
                if(bm != null){
                    new CropPop(MainActivity.this).show(bm);
                }
            }
        });


        Glide.with(this).load(R.mipmap.test_heng).into(clipImageView);
    }

    public void onClick(View v){
        clipImageView.invalidate();
        switch (v.getId()){
            case R.id.heng:
                Glide.with(this).load(R.mipmap.test_heng).into(clipImageView);
                break;
            case R.id.shu:
                Glide.with(this).load(R.mipmap.test_shu).into(clipImageView);
                break;
            case R.id.fang:
                Glide.with(this).load(R.mipmap.test_fang).into(clipImageView);
                break;
        }
    }

    class CropPop extends ShadowPopupWindow{

        ImageView iv ;
        public CropPop(Activity context) {
            super(context);
            this.setWidth(getScreenWidth());
            this.setHeight(getScreenWidth());
        }

        @Override
        protected void onCreateView(View view) {
            iv = (ImageView) view;
        }

        public void show(Bitmap bm){
            iv.setImageBitmap(bm);
            showCenteral(R.layout.activity_main);
        }

        @Override
        protected long getAnimatorDuration() {
            return 400;
        }

        @Override
        protected Object getLayoutIdOrView() {
            ImageView iv = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,-1);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return iv ;
        }

        /**
         * 获取屏幕宽度
         *
         * @return
         */
        public int getScreenWidth(){
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
            return metrics.widthPixels;
        }
    }
}
