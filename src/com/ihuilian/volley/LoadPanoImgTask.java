/*
 * @Title LoadPanoImgTask.java
 * @Copyright Copyright 2010-2014 Careland Software Co,.Ltd All Rights Reserved.
 * @author lisx
 * @date 2015-5-14 下午6:19:52
 * @version 1.0
 */
package com.ihuilian.volley;

import android.graphics.Bitmap;

import com.ihuilian.cache.ImageLoad;
import com.ihuilian.entity.MSG;
import com.ihuilian.entity.ScenicPanoramaBean.PanoramaBean;
import com.ihuilian.task.ITask;
import com.panoramagl.PLImage;
import com.panoramagl.PLSpherical2Panorama;
import com.panoramagl.enumerations.PLCubeFaceOrientation;

import java.io.File;
import java.util.LinkedList;

/** 
 * @author lisx
 * @date 2015-5-14 下午6:19:52 
 */
public class LoadPanoImgTask extends ITask {
	private PLSpherical2Panorama spher2Panorama;
	private PanoramaBean panoramaBean;

	public LoadPanoImgTask(int taskId, PanoramaBean panoramaBean, PLSpherical2Panorama spher2Panorama) {
		super(taskId);
		this.panoramaBean = panoramaBean;
		this.spher2Panorama = spher2Panorama;
	}

	@Override
	public MSG doTask() {
		if(panoramaBean.sources.sphere != null){
			//MSG 返回的数据
			LinkedList<Object> dataList = new LinkedList<Object>();
			String imgURl = "";
			//TODO 默认high，有待确认  如果是球形的话是1张图片，立方体的话是6张，确认是只会时球体
			switch (panoramaBean.resolution){
				case 512:
					imgURl = panoramaBean.sources.sphere.sphere_512.high;
					break;
				case 1024:
					imgURl = panoramaBean.sources.sphere.sphere_1024.high;
					break;
				case 2048:
					imgURl = panoramaBean.sources.sphere.sphere_2048.high;
					break;
				case 4096:
					imgURl = panoramaBean.sources.sphere.sphere_4096.high;
					break;
			}
			spher2Panorama.removeAllHotspots();

			//缓存中查找，没有的话网络获取图片，并缓存起来
			Bitmap bitmap = ImageLoad.getInstance().loadBitmaps(imgURl);

			if(bitmap!=null){
				spher2Panorama.setImage(new PLImage(bitmap, false));
			}

			dataList.add(spher2Panorama);
			dataList.add(panoramaBean);
			return new MSG(true, dataList);

			/*for(String imageUrl : panorama.getImages()){
				if(TextUtils.isEmpty(imageUrl)){
					return new MSG(false,"没有全景图片Url地址");
				}
				File cacheFile = new File(FileUtil.getDirectory(Constant.CACHE_IMG), String.valueOf(imageUrl.hashCode()));
				if(!cacheFile.exists()){
					try {
						PLViewModelUtils.saveImageFromUrl(new URL(imageUrl), cacheFile);
					} catch (Exception e) {
						return new MSG(false,"全景图片加载失败，请检查网络连接");
					}
				}
				if(!setPanoImg(imageUrl,cacheFile)){
					return new MSG(false,"全景图片加载失败");
				}
			}*/
//			绗竴鐗堟湰鏃犵儹鐐?//	
//			List<PanoramaHotspots> panoramaHotspotList = panorama.getHotspots();
//			if(panoramaHotspotList==null){
//				panoramaHotspotList = new ArrayList<PanoramaHotspots>();
//			}
//			PanoramaHotspots panoramaHotspots = new PanoramaHotspots();
//			panoramaHotspots.setAth("52.211");
//			panoramaHotspots.setAtv("20.938");
//			panoramaHotspots.setTo_pano_id(panorama.getPano_id());
//			panoramaHotspotList.add(panoramaHotspots);
//			
//			panoramaHotspots = new PanoramaHotspots();
//			panoramaHotspots.setAth("-86.381");
//			panoramaHotspots.setAtv("18.205");
//			panoramaHotspots.setTo_pano_id(panorama.getPano_id());
//			panoramaHotspotList.add(panoramaHotspots);
//			
//			for(PanoramaHotspots panoHotspots : panoramaHotspotList){
//				if(MathUtil.isNumber(panoHotspots.getAth()) && MathUtil.isNumber(panoHotspots.getAtv())){
//					PLImage plImage = PLImage.imageWithBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.test99),false);
//					HLPLHotspot plHotspot = new HLPLHotspot(0, PLTexture.textureWithImage(plImage), Float.parseFloat(panoHotspots.getAtv()), Float.parseFloat(panoHotspots.getAth()), 0.07f, 0.07f);
//					plHotspot.setTag(panoHotspots);
//					spher2Panorama.addHotspot(plHotspot);
//				}
//			}
			
//			spher2Panorama.setImage(gl, PLImage.imageWithBitmap(PLUtils.getBitmap(context, R.raw.pano_f), false), PLCubeFaceOrientation.PLCubeFaceOrientationFront);
//			spher2Panorama.setImage(gl, PLImage.imageWithBitmap(PLUtils.getBitmap(context, R.raw.pano_b), false), PLCubeFaceOrientation.PLCubeFaceOrientationBack);
//			spher2Panorama.setImage(gl, PLImage.imageWithBitmap(PLUtils.getBitmap(context, R.raw.pano_l), false), PLCubeFaceOrientation.PLCubeFaceOrientationLeft);
//			spher2Panorama.setImage(gl, PLImage.imageWithBitmap(PLUtils.getBitmap(context, R.raw.pano_r), false), PLCubeFaceOrientation.PLCubeFaceOrientationRight);
//			spher2Panorama.setImage(gl, PLImage.imageWithBitmap(PLUtils.getBitmap(context, R.raw.pano_u), false), PLCubeFaceOrientation.PLCubeFaceOrientationUp);
//			spher2Panorama.setImage(gl, PLImage.imageWithBitmap(PLUtils.getBitmap(context, R.raw.pano_d), false), PLCubeFaceOrientation.PLCubeFaceOrientationDown);

		}
		return new MSG(false,"鍏ㄦ櫙鍥剧墖涓嶅叏");
	}
	
	/**
	 * 璁剧疆鍏ㄦ櫙鍥剧墖
	 * @param imageUrl
	 * @param cacheFile
	 */
	private boolean setPanoImg(String imageUrl,File cacheFile){
		if (imageUrl.contains("/")) {
			imageUrl = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.length());
		}
		if(imageUrl.contains(".")){
			imageUrl = imageUrl.split("[.]")[0].toLowerCase();
		}

		PLCubeFaceOrientation plCubeFaceOrientation = null;
		if(imageUrl.endsWith("l")){	//宸?
			plCubeFaceOrientation =	PLCubeFaceOrientation.PLCubeFaceOrientationLeft; 
		}else if(imageUrl.endsWith("r")){	//鍙?
			plCubeFaceOrientation =	PLCubeFaceOrientation.PLCubeFaceOrientationRight;
		}else if(imageUrl.endsWith("f")){	//鍓?	
			plCubeFaceOrientation =	PLCubeFaceOrientation.PLCubeFaceOrientationFront;
		}else if(imageUrl.endsWith("b")){	//鍚?	
			plCubeFaceOrientation =	PLCubeFaceOrientation.PLCubeFaceOrientationBack;
		}else if(imageUrl.endsWith("u")){	//涓?	
			plCubeFaceOrientation =	PLCubeFaceOrientation.PLCubeFaceOrientationUp;
		}else if(imageUrl.endsWith("d")){	//涓?d
			plCubeFaceOrientation =	PLCubeFaceOrientation.PLCubeFaceOrientationDown;
		}
		//参考 报错 注释了
		/*if(plCubeFaceOrientation!=null){
			Bitmap bitmap = PLUtils.getBitmap(context, "file://"+cacheFile.getAbsolutePath());
			if(bitmap!=null){
				spher2Panorama.setImage(gl, PLImage.imageWithBitmap(bitmap, false), plCubeFaceOrientation);
				return true;
			}else{
			}
		}*/
		return false;
	}

}
