package com.ihuilian.example;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.android.volley.Request;
import com.google.gson.reflect.TypeToken;
import com.ihuilian.Constant;
import com.ihuilian.entity.BaseBean;
import com.ihuilian.entity.MSG;
import com.ihuilian.entity.ScenicPanoramaBean;
import com.ihuilian.entity.ScenicPanoramaBean.PanoramaBean;
import com.ihuilian.hlandroid_sdk.R;
import com.ihuilian.task.ITask;
import com.ihuilian.task.IUIController;
import com.ihuilian.task.TaskManager;
import com.ihuilian.util.ToastUtil;
import com.ihuilian.view.ICommonUtil;
import com.ihuilian.volley.LoadPanoImgTask;
import com.ihuilian.volley.VolleyManger;
import com.ihuilian.volley.VolleyRequestTask;
import com.panoramagl.PLImage;
import com.panoramagl.PLSpherical2Panorama;
import com.panoramagl.PLView;
import com.panoramagl.PLViewListener;
import com.panoramagl.hotspots.PLHotspot;
import com.panoramagl.utils.PLUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

/**
 * @author bakon(762713299@qq.com)
 * @version
 */
public class BasePLView extends PLView implements IUIController {

	private final int SCIENCE_DETAIL_DATA = 1001;
	private final int LOAD_PAN_IMG = 1002;
	//	protected CustomProgressDialog progressDialog = null;
	protected View progressBarView;
	//请求参数
	protected Map<String, String> parms = new HashMap<String, String>();
	private BaseBean<ScenicPanoramaBean> dataInfo;
	private List<PanoramaBean> panoramaBeanList;
	//Intent 数据（外面传进来）
	private String id;
	private PLSpherical2Panorama panorama = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        TaskManager.getInstance().registerUIController(this);

		initUI(savedInstanceState);
        initData(savedInstanceState);
        initListener(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		TaskManager.getInstance().unRegisterUIController(this);
//		dismissProgressDialog();
	}

	@Override
	public void initUI(Bundle savedInstanceState) {
		//intent 数据
		id = getIntent().getStringExtra("id");

		panorama = new PLSpherical2Panorama();
//		panorama.setImage(new PLImage(PLUtils.getBitmap(this, R.drawable.p2), false));
		setPanorama(panorama);
	}
	@Override
	protected void onGLContextCreated(GL10 gl) {
		super.onGLContextCreated(gl);
		loadPnorama();
//		showProgressDialog();
	}
	@Override
	public void initListener(Bundle savedInstanceState) {
		setListener(new PLViewListener() {});
	}

	@Override
	public void initData(Bundle savedInstanceState) {
		int netType = ICommonUtil.getNetworkType();
		//请求数据
		parms.clear();
		parms.put("id", id);
		execuVolleyTask(new VolleyRequestTask<ScenicPanoramaBean>(
				SCIENCE_DETAIL_DATA, Constant.URL_BY_ID, Request.Method.GET,parms, new TypeToken<BaseBean<ScenicPanoramaBean>>() {}.getType()));
	}
	@Override
	public void refreshUI(int id, MSG msg) {
		/** 网络请求失败操作 */
		if (msg.getIsNetworkError() != null && msg.getIsNetworkError() == true) { // 网络异常
			// showWifiDisConnView(diaryDetailRelay);
			return;
		}
		/** 网络请求成功操作 */
		if (msg.getIsSuccess()) {
			switch (id) {
				case SCIENCE_DETAIL_DATA:
					dataInfo = (BaseBean<ScenicPanoramaBean>) msg.getObj();
					if (dataInfo.data != null && dataInfo.data.list != null) {
						panoramaBeanList = dataInfo.data.list;
						Log.i("", ""+dataInfo.toString());
						//下载全景图片，设置全景
						loadPnorama();
					}
					break;
				case LOAD_PAN_IMG:
					if (msg.getIsSuccess()) {
						// TODO: 2016/5/12 做一些最后的设置
						LinkedList<Object> dataList = (LinkedList<Object>) msg.getObj();
						panorama = (PLSpherical2Panorama) dataList.get(0);
						PLHotspot hotspot1 = new PLHotspot(1, new PLImage(PLUtils.getBitmap(this, R.drawable.hotspot), false), -30.0f, 0.0f, 0.05f, 0.05f);
						panorama.addHotspot(hotspot1);

						this.reset();
						setPanorama(panorama);
						// 俯视角度的范围
						getCamera().setPitchRange(-45, 45);
						// 水平角度的范围
						getCamera().setYawRange(-180, 180);
						getCamera().setInitialLookAt(0, 0);
						isLoadPano = false;


						/*Bitmap bitmap = PLViewModelUtils.getViewBitmap(R.layout.pl_hotspot_layout);

						hotspot1 = new PLHotspot(1, PLImage.imageWithBitmap(bitmap, false), 0.0f, 0.0f);
						plCubicPanorama.addHotspot(hotspot1);
						pLViewModelUtils.addAllHotsport(plCubicPanorama,panoramasList);
						initPLCubicPanorama(false);
						dismissDelayProgressDialog();
						dismissDelayProgressBarView();
						if(null != progressBarLayout && progressBarLayout.isShown()){
							PLViewModelUtils.endAnimationDetails(progressBarLayout);
							progressBarLayout.setVisibility(View.GONE);
						}else{
							initLayout();
							initDetaulsLayout();
						}*/

						// startAutoCamera();
					}
					isLoadPano = false;

					break;
				default:
					break;
			}
		}

	}
	 //测试的获取全景图片id  好像没什么用
	private String panoId;
	//是否正在加载全景
	private boolean isLoadPano = false;
	/**
	 * 处理数据,加载全景
	 */
	private void loadPnorama(){
		panoId = panoramaBeanList.get(0).id;
		if (TextUtils.isEmpty(panoId)) {
			ToastUtil.showShort(this, "加载失败,全景ID为空!");
			return;
		}
		isLoadPano = true;
		/*getGLSurfaceView().clearPanorama(new ICallBck() {
			@Override
			public void callBack() {
				execuTask(new LoadPanoImgTask(LOAD_PAN_IMG, panoramasInfo,
						getCurrentGL(), plCubicPanorama));
			}
		});*/
		execuTask(new LoadPanoImgTask(LOAD_PAN_IMG, panoramaBeanList.get(0), panorama));
	}




	/**
	 * 显示等待进度框
	 *
	 * @param message
	 * @param cancelable
	 */
//	protected void showProgressDialog(String message, boolean cancelable) {
//		progressDialog = CustomProgressDialog.show(this, message, false, R.layout.progress_pano_dialog, R.style.FullScreenDialogStyle);
//	}

	/**
	 * 默认显示 正在加载全景 圆形进度
	 */
//	protected void showProgressDialog() {
//		dismissProgressDialog();
//		progressDialog = CustomProgressDialog.show(this, "正在加载全景...", false, R.layout.progress_pano_dialog, R.style.FullScreenDialogStyle);
//		progressDialog.setOnKeyListener(new OnKeyListener() {
//			@Override
//			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//				if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//					finish();
//				}
//				return false;
//			}
//		});
//	}

	/**
	 * 关闭ProgressDialog
	 */
//	protected void dismissProgressDialog() {
//		if (progressDialog != null) {
//			progressDialog.disappear();
//			progressDialog = null;
//		}
//	}

	/**
	 * 延迟 关闭ProgressDialog
	 */
//	protected void dismissDelayProgressDialog() {
//		getGLSurfaceView().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				dismissProgressDialog();
//			}
//		}, 100);
//	}

	/**
	 * 显示progressbarview
	 */
	protected void showProgressBarView(){
		progressBarView.setVisibility(View.VISIBLE);
		ObjectAnimator.ofFloat(progressBarView, "alpha", 0f,1f).setDuration(200).start();
	}

	/**
	 * 延迟 隐藏progressbarview
	 */
	protected void dismissDelayProgressBarView(){
		getGLSurfaceView().postDelayed(new Runnable() {
			@Override
			public void run() {
				dismissProgressBarView();
			}
		}, 100);
	}

	/**
	 * 隐藏progressbarview
	 */
	protected void dismissProgressBarView(){
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(progressBarView, "alpha", 1f,0f).setDuration(400);
		objectAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {}
			@Override
			public void onAnimationRepeat(Animator animation) {}
			@Override
			public void onAnimationEnd(Animator animation) {
				progressBarView.setVisibility(View.INVISIBLE);
			}
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
		objectAnimator.start();
	}

	/**
	 * 初始化progressbar view
	 */
//	private void initProgressBarView(){
//		if(progressBarView!=null)
//			return ;
//		progressBarView = getLayoutInflater().inflate(R.layout.progress_pano_dialog, null);
//		progressBarView.setVisibility(View.INVISIBLE);
//		FrameLayout.LayoutParams lpLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
//		lpLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
//		addContentView(progressBarView, lpLayoutParams);
//	}

	/**
	 * 执行Task任务
	 *
	 * @param task
	 */
	protected void execuTask(ITask task) {
		if (task == null)
			return;
		task.setContext(this);
		task.setmIdentification(getIdentification());
		TaskManager.getInstance().addTask(task);
	}

	/**
	 * 执行Volley Task任务
	 *
	 * @param task
	 */
	protected void execuVolleyTask(ITask task) {
		if (task == null)
			return;
		task.setContext(this);
		task.setmIdentification(getIdentification());
		VolleyManger.getInstance().addTask(task);
	}

	/**
	 * 返回键
	 */
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public String getIdentification() {
		return getClass().toString() + this;
	}

}
