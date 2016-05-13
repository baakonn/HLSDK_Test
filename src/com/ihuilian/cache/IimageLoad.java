package com.ihuilian.cache;

import android.graphics.Bitmap;

/**
 * @ClassName: ImageCache
 * @Description: 图片读取接口，为了二级缓存
 * @author Bakon 762713299@qq.com
 * @date May 6, 2016
 */
public interface IimageLoad {

	public Bitmap getBitmap(String url);

	public void putBitmap(String url, Bitmap bitmap);

}
