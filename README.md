# ClipView
轻量型图片裁剪框架，自适应图片显示，支持移动缩放，边界回弹，宽高尺寸压缩

# demo下载
右击 -> 链接另存为</br>
[clipview.apk](https://github.com/bigdongdong/ClipView/blob/master/preview/clipview.apk)

# 截图预览
## 自适应图片显示（横图、竖图、方图）
<img  width = "300" src = "https://github.com/bigdongdong/ClipView/blob/master/preview/heng.jpg"></img>
<img  width = "300" src = "https://github.com/bigdongdong/ClipView/blob/master/preview/shu.jpg"></img>
<img  width = "300" src = "https://github.com/bigdongdong/ClipView/blob/master/preview/fang.jpg"></img></br>

## 移动、缩放和边界回弹

<img  width = "300" src = "https://github.com/bigdongdong/ClipView/blob/master/preview/pre.gif"></img>
<img  width = "300" src = "https://github.com/bigdongdong/ClipView/blob/master/preview/scale.gif"></img>
<img  width = "300" src = "https://github.com/bigdongdong/ClipView/blob/master/preview/springback.gif"></img></br>

# 项目配置

```
  allprojects {
      repositories {
          ...
          maven { url 'https://jitpack.io' }  //添加jitpack仓库
      }
  }
  
  dependencies {
	  implementation 'com.github.bigdongdong:ClipView:1.1' //添加依赖
  }
```

# 使用说明
## xml :
```xml
	<com.cxd.clipview.ClipImageView
		android:id="@+id/civ"
		android:layout_width="match_parent"
		android:layout_height="match_parent"/>
```
## java :
```java
	civ = findViewById(R.id.civ);
        civ.setCropWindowSize(400,400);         //设置需要裁减的尺寸，单位：px
        Glide.with(this).load().into(civ);      //选择喜欢的图片加载框架
	
	civ.getCropBitmap();       		//获取裁减后的源bitmap
        civ.getCropBitmapWithZip(); 		//获取裁剪并进行尺寸压缩后的bitmap
```
