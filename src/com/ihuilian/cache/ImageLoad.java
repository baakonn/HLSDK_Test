package com.ihuilian.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.ihuilian.application.HLApplication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import libcore.io.DiskLruCache;
import libcore.io.DiskLruCache.Snapshot;

/**
 * @ClassName: ImageLoad
 * @Description: 加载图片
 * @author Bakon 762713299@qq.com
 * @date May 6, 2016
 * 
 */
public class ImageLoad {
	private static ImageLoad imageLoad;
	//sd卡缓存最大空间，暂设定30M
	private static final long DISK_CACHE_SIZE= 1024 *1024 * 30;
	//支持bitmap 最大宽高
	private static final int MAX_WIDTH= 2048;
	private static final int MAX_HEIGHT= 1024;

	//图片内存缓存
	private LruCache<String, Bitmap> mMemoryCache;

	//图片sd卡缓存
	private DiskLruCache mDiskLruCache;
	
	// 设置图片缓存大小为程序最大可用内存的1/8
	int maxMemory = (int) Runtime.getRuntime().maxMemory();
	int cacheSize = maxMemory / 8;
	
	public static ImageLoad getInstance() {
		if (imageLoad == null) {
			imageLoad = new ImageLoad();
		}
		return imageLoad;
	}
	
	/**
	 * 初始化
	 */
	public ImageLoad(){
		//内存缓存
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount();
			}
		};
		//sd卡缓存
		try {
			// 获取图片缓存路径
			File cacheDir = getDiskCacheDir(HLApplication.appcontext, "thumb");
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			// 创建DiskLruCache实例，初始化缓存数据
			mDiskLruCache = DiskLruCache .open(cacheDir, 1, 1, DISK_CACHE_SIZE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @Title: getDiskCacheDir 
	 * @Description: 根据传入的uniqueName获取硬盘缓存的路径地址。
	 * @param  context
	 * @param  uniqueName
	 * @return File    返回类型
	 */
	private File getDiskCacheDir(Context context, String uniqueName) {
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + uniqueName);
	}
	
	
	/**
	 * imageload 主要方法
	 */
	public Bitmap loadBitmaps(String imageUrl) {
		if (TextUtils.isEmpty(imageUrl)) {
			return null;
		}
		//内存缓存中查找
		Bitmap bitmap = getBitmapFromMemoryCache(String.valueOf(imageUrl.hashCode()));
		
		if (bitmap == null) {
			//sd卡中查找
			bitmap = getBitmapFromDiskLruCache(imageUrl);
		}
		return bitmap;
	}
	
	/**
	 * 从mMemoryCache中查找，如果不存在就返回null
	 * @param key 图片URL的hash值。
	 */
	private Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}
	/**
	 * 从DiskLruCache中查找
	 */
	private Bitmap getBitmapFromDiskLruCache(String... params) {
		//图片的URL地址
		String imageUrl = params[0];
		//url hash值
		String hashimageUrl = String.valueOf(imageUrl);
		FileDescriptor fileDescriptor = null;
		FileInputStream fileInputStream = null;
		Snapshot snapShot = null;
		// 查找key对应的缓存
		try {
			snapShot = mDiskLruCache.get(imageUrl);
			if (snapShot == null) {
				// 如果没有找到对应的缓存，则准备从网络上请求数据，并写入缓存
				DiskLruCache.Editor editor = mDiskLruCache.edit(hashimageUrl);
				if (editor != null) {
					OutputStream outputStream = editor.newOutputStream(0);
					//写入outputStream
					if (downloadUrlToStream(new URL(imageUrl), outputStream)) {
						editor.commit();
					} else {
						editor.abort();
					}
				}
				// 缓存被写入后，再次查找key对应的缓存
				snapShot = mDiskLruCache.get(imageUrl);
			}
			if (snapShot != null) {
				fileInputStream = (FileInputStream) snapShot.getInputStream(0);
				fileDescriptor = fileInputStream.getFD();
			}
			// 将缓存数据解析成Bitmap对象
			Bitmap bitmap = null;
			if (fileDescriptor != null) {
				bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
				//处理bitmap大小问题
//				bitmap = BitmapUtil.reSizeBitmap(bitmap);
			}
			if (bitmap != null) {
				// 将Bitmap对象添加到内存缓存当中
				addBitmapToMemoryCache(params[0], bitmap);
			}
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileDescriptor == null && fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	
	/**
	 * 将一张图片存储到MemoryCache中。
	 * 
	 * @param key  LruCache的键，这里传入图片的URL地址。
	 * @param bitmap LruCache的键，这里传入从网络上下载的Bitmap对象。
	 */
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * @param imageUrl 图片url
	 * @param outputStream 输出流
	 * @return isSuccess
	 * @author lisx
	 * @date 2015-5-14 下午6:21:50
	 */
	public boolean downloadUrlToStream(URL imageUrl, OutputStream outputStream) {
		//返回值
		boolean isSuccess = false;

		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			//文件大小
			int totalLength = conn.getContentLength();
			in = new BufferedInputStream(conn.getInputStream(), totalLength);
			out = new BufferedOutputStream(outputStream, totalLength);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			isSuccess = true;
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return isSuccess;
	}
	/**
	 * 请求service，并获取Bitmap对象。
	 * @param urlString 图片的URL地址
	 * @return 解析后的Bitmap对象
	 */
	/*BufferedOutputStream out = null;
	BufferedInputStream in = null;
	boolean isSuccess = false;
	private boolean downloadUrlToStream(String urlString, final OutputStream outputStream) {
		Call<ResponseBody> call = ServiceClient.getSeviceClient().getImageByUrl(urlString);
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					try {
						in = new BufferedInputStream(response.body().byteStream(), 8 * 1024);
						out = new BufferedOutputStream(outputStream, 8 * 1024);
						int b;
						while ((b = in.read()) != -1) {
							out.write(b);
						}
						isSuccess = true;
					} catch (final IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (in != null) {
								in.close();
							}
							if (out != null) {
								out.close();
							}
						} catch (final IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				
			}
		});
		return isSuccess;
	}*/


	/**
	 * @Title: hashKeyFormUrl 
	 * @Description: url中会包含特殊字符，将图片url的MD5值作为缓存的key
	 * @param url
	 * @return String   返回类型
	 */
	private String hashKeyFormUrl(String url) {
	    String cacheKey;
	    try {
	        final MessageDigest mDigest = MessageDigest.getInstance("MD5");
	        mDigest.update(url.getBytes());
	        cacheKey = bytesToHexString(mDigest.digest());
	    } catch (NoSuchAlgorithmException e) {
	        cacheKey = String.valueOf(url.hashCode());
	    }
	    // 返回将作为缓存的key值
	    return cacheKey;
	}

	// 将byte[]数组转为字符串的的方法  
	private String bytesToHexString(byte[] bytes) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < bytes.length; i++) {
	        String hex = Integer.toHexString(0xFF & bytes[i]);
	        if (hex.length() == 1) {
	            sb.append('0');
	        }
	        sb.append(hex);
	    }
	    return sb.toString();
	}
	

}
