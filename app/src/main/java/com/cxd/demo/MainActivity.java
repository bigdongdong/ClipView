package com.cxd.demo;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.cxd.clipview.ClipImageView;

public class MainActivity extends AppCompatActivity {

    private ClipImageView clipImageView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        clipImageView = findViewById(R.id.clipImageView);

        heng();
//        shu();
//        Glide.with(this)
//                .asGif()
//                .load("https://image.soudlink.com/pictrue/202004101550.webp")
//                .into(clipImageView);

    }

    private void heng(){
        /*横图*/
        Glide.with(this).load("https://timgsa.baidu.com/timg?image&quality=8" +
                "0&size=b9999_10000&sec=1586515095891&di=1050e3fd18a9f85c306a" +
                "93d2c9ca070c&imgtype=0&src=http%3A%2F%2Fwx3.sinaimg.cn%2Fwap720%" +
                "2F6709d712ly1g9er5n2nvbj20k00b8jsh.jpg").into(clipImageView);
    }

    private void shu(){
        /*竖图*/
        Glide.with(this).load("https://timgsa.baidu.com/timg?image&quality=80" +
                "&size=b9999_10000&sec=1586517745515&di=eb110ca93cc78e8a8adb8e01e1784195&imgt" +
                "ype=0&src=http%3A%2F%2Fpics3.baidu.com%2Ffeed%2F7acb0a46f21fbe094117aa301ba843" +
                "358644ad93.jpeg%3Ftoken%3Dfd7a95476f7ad96be6243fab844eaa23%26s%3D32C2914C9EB1" +
                "D5DC02AC4CB80300D093").into(clipImageView);
    }
}
