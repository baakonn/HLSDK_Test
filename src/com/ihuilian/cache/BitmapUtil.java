/*
 * PanoramaGL library
 * Version 0.2 beta
 * Copyright (c) 2010 Javier Baez <javbaezga@gmail.com>
 *
 * mayakun add 2016/5/4
 */

package com.ihuilian.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BitmapUtil {
    //bitmap 最大宽高
    private static final int MAX_WIDTH = 2048;
    private static final int MAX_HEIGHT = 1024;


    /**
     * 限制最大宽高
     * @param image
     * @return
     */
    public static Bitmap reSizeBitmap(Bitmap image) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        int mWidth = image.getWidth();
        int mHeight = image.getHeight();
        if (mWidth > MAX_WIDTH) {
            mWidth = MAX_WIDTH;
        }
        if (mHeight > MAX_HEIGHT) {
            mHeight = MAX_HEIGHT;
        }
        return reSizeBitmap(image, mWidth, mHeight);
    }


//    public static InputStream Bitmap2InputStream(Bitmap bm, int quality) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
//        InputStream is = new ByteArrayInputStream(baos.toByteArray());
//        return is;
//    }
    /**
     * 按照尺寸比例改变大小
     * @param
     * @return
     */
//    public static Bitmap reSizeBitmap2(Bitmap bit,int reqWidth, int reqHeight) {
//        int width = bit.getWidth();
//        int height = bit.getHeight();
//
//        float scaleWidth = ((float) reqWidth) / width;
//        float scaleHeight = ((float) reqHeight) / height;
//
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth, scaleHeight);
//
//        return Bitmap.createBitmap(bit, 0, 0, width, height, matrix, true);
//    }

    /**
     * 按照尺寸比例改变大小
     * @param image
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap reSizeBitmap(Bitmap image, int reqWidth, int reqHeight) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        image.compress(Bitmap.CompressFormat.PNG, 80, baos);

//        while (baos.toByteArray().length / 1024 > 2747){
//            Log.d("tag", baos.toByteArray().length/1024 +"");
//            // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
//            baos.reset();// 重置baos即清空baos
//            options -= 10;// 每次都减少10
//            image.compress(Bitmap.CompressFormat.PNG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
//        }

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
//        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(isBm, null, newOpts);
//        newOpts.inJustDecodeBounds = false;
//        int w = newOpts.outWidth;
//        int h = newOpts.outHeight;
//
//        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
//        int be = 1;// be=1表示不缩放
//        if (w > h && w > reqWidth) {// 如果宽度大的话根据宽度固定大小缩放
//            be = (int) (newOpts.outWidth / reqWidth);
//        } else if (w < h && h > reqHeight) {// 如果高度高的话根据宽度固定大小缩放
//            be = (int) (newOpts.outHeight / reqHeight);
//        }
//        if (be <= 0){
//            be = 1;
//        }


        newOpts.inSampleSize = caculateInSampleSize(newOpts, reqWidth, reqHeight);// 设置缩放比例
        isBm = new ByteArrayInputStream(baos.toByteArray());
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        newOpts.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(isBm, null, newOpts);// 压缩好比例大小后再进行质量压缩
    }

    /**
     *图片质量压缩
     */
    private static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 1000)
        { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }
    /**
     * 获取图片的采样比例
     */
    public static int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //获取位图的宽和高
        final int height = options.outHeight;
        final int widht = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || widht > reqWidth) {
            if (widht > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) widht / (float) reqWidth);
            }
        }
        return inSampleSize+1;
    }


}