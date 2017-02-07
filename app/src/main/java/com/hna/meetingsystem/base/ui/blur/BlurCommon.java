package com.hna.meetingsystem.base.ui.blur;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

public class BlurCommon {

    private int mColorFilter = 0xffffffff;
    private int mAlpha = 0x66;//透明度

    private float radius = 20;//半径
    private float scaleFactor = 8;// 图像缩小为原来的1/scaleFactor再模糊处理
    private View bgImage;//背景图view
    private View targetView;//目标图view
    private Context context;

    private CallBack callBack;

    private BlurCommon(View bgImage, View targetView) {
        super();
        this.bgImage = bgImage;
        this.targetView = targetView;
    }

//设置颜色
    public BlurCommon setColorFilter(int colorFilter) {
        this.mColorFilter = colorFilter;
        return this;
    }
//设置透明度为0x66
    public BlurCommon setAlpha(int alpha) {
        this.mAlpha = alpha;
        return this;
    }
//设置callBack接口
    public BlurCommon setCallBack(CallBack callBack) {
        this.callBack = callBack;
        return this;
    }
//设置缩小比例为1/8
    public BlurCommon setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        return this;
    }
//设置半径为20
    public BlurCommon setRadius(float radius) {
        this.radius = radius;
        return this;
    }
//提供实例
    public static BlurCommon newInstance(View backgroundView, View targetView) {
        return new BlurCommon(backgroundView, targetView);
    }

    public void applyBlur(Context context) {
        applyBlur(context, true);
    }
//把一张图片背景预先高斯毛玻璃处理
    public void applyBlur(Context context, final boolean isFilter) {
        this.context = context;
        try {
            if (bgImage == null || targetView == null)
                return;

            bgImage.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            bgImage.getViewTreeObserver()
                                    .removeOnPreDrawListener(this);
                            bgImage.buildDrawingCache();
                            Bitmap bmp = bgImage.getDrawingCache();
                            blur(bmp, isFilter);
                            return true;
                        }
                    });
        } catch (Exception e) {
            Log.d("Blur", "Blur失败");
        }
    }

    //4.1版本
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void blur(Bitmap bkg, boolean isFilter) {
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), getBlurBitmap(bkg));
        if (isFilter) {
            bd.setAlpha(mAlpha);
            //PorterDuff.Mode.DST_ATOP，取上层非交集部分与下层交集部分
            bd.setColorFilter(mColorFilter, PorterDuff.Mode.DST_ATOP);
        }
        targetView.setBackground(bd);
        if(callBack != null) {
            callBack.call(targetView);
        }
    }
//返回毛玻璃效果后的图片
    private Bitmap getBlurBitmap(Bitmap bkg) {
        Bitmap overlay = Bitmap.createBitmap(
                (int) (targetView.getMeasuredWidth() / scaleFactor),
                (int) (targetView.getMeasuredHeight() / scaleFactor),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-targetView.getLeft() / scaleFactor,
                -targetView.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);//是使位图过滤的位掩码标志
        canvas.drawBitmap(bkg, 0, 0, paint);
        //毛玻璃处理
        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        return overlay;
    }

//返回接口
    public interface CallBack {
        void call(View tragetView);
    }
}
