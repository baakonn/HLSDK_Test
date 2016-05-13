package com.ihuilian.example;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ZoomControls;

import com.ihuilian.hlandroid_sdk.R;
import com.ihuilian.util.ToastUtil;
import com.panoramagl.PLIView;
import com.panoramagl.PLImage;
import com.panoramagl.PLSpherical2Panorama;
import com.panoramagl.PLView;
import com.panoramagl.PLViewListener;
import com.panoramagl.hotspots.PLHotspot;
import com.panoramagl.hotspots.PLIHotspot;
import com.panoramagl.ios.structs.CGPoint;
import com.panoramagl.structs.PLPosition;
import com.panoramagl.transitions.PLTransitionBlend;
import com.panoramagl.utils.PLUtils;

public class MainActivity extends PLView{

	//	@InjectView(R.id.zoomControl)
	private ZoomControls zoomControl;
	private GLSurfaceView glSurfaceView;
	private Button startButton,stopButton,startNew;
	//传感器manager
	private SensorManager sensorManager;
	//磁力传感器
	private Sensor magneticSensor;
	//速度传感器
	private Sensor accelerometerSensor;
	//陀螺仪传感器
	private Sensor gyroscopeSensor;


	private boolean isAuto = true;

	/** init methods */
	private static String TAG = "Tag";
	private Context context;
	private PLSpherical2Panorama panorama;
	private PLSpherical2Panorama panorama2;
	int maxZoomLevle;

	/**
	 * This event is fired when root content view is created
	 *
	 * @param contentView
	 *            current root content view
	 * @return root content view that Activity will use
	 */
	@Override
	protected View onContentViewCreated(View contentView) {
		showProgressBar();
		// Load layout
		ViewGroup mainView = (ViewGroup) this.getLayoutInflater().inflate(R.layout.panorma_view, null);
		// Add 360 view
		mainView.addView(contentView, 0);
		// Return root content view
		return super.onContentViewCreated(mainView);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this;
		initSensor();//传感器

		// Load panorama
		panorama = new PLSpherical2Panorama();
//		PLSphericalPanorama panorama = new PLSphericalPanorama();

		panorama.getCamera().lookAt(0.0f, 0.0f);
		maxZoomLevle = panorama.getCamera().getZoomLevels();
		Log.d(TAG, maxZoomLevle + "");
//		PLRotation rotation = panorama.getCamera().getLookAtRotation();
//		Log.d(TAG, rotation.pitch + "/"+rotation.roll+"/"+rotation.yaw);

		PLHotspot hotspot1 = new PLHotspot(1, new PLImage(PLUtils.getBitmap(context, R.drawable.hotspot), false), -30.0f, 0.0f, 0.05f, 0.05f);
		final PLHotspot hotspot2 = new PLHotspot(2, new PLImage(PLUtils.getBitmap(context, R.drawable.hotspot), false), 0.0f, 40.0f, 0.05f, 0.05f);
		panorama.addHotspot(hotspot1);
//		lookAt(vLookAt, hLookAt, <animated>)
//		hotspot1.setOnClick("lookAt(0.0, 170.0, true)");
		panorama.addHotspot(hotspot2);
//		lookAtAndZoom(vLookAt, hLookAt, zoomFactor, <animated>)
//		zoom(zoomFactor, <animated>)
//		fov(fov, <animated>)
//		hotspot2.setOnClick("fov(30, true)");//json file
//		hotspot2.setOnClick("load('res://raw/p2', true, BLEND(2.0, 1.0), 0.0, 190.0)");
//		hotspot2.addOnClickListener(new OnHopspotClickListener() {
//			@Override
//			public void onHopspotClick() {
//				setLocked(true);
//
//				panorama.setImage(new PLImage(PLUtils.getBitmap(context, R.drawable.p2), false));
//				reset();
//				startTransition(new PLTransitionBlend(2.0f), panorama);
//
//				setLocked(false);
//			}
//		});


//		ImageLoad.getInstance().getBitmaps("");

		panorama.setImage(new PLImage(PLUtils.getBitmap(this, R.drawable.spherical_pano), false));
		//设置全景图片的几种方式
		this.setPanorama(panorama);


		hideProgressBar();
//		this.startTransition(new PLTransitionBlend(2.0f), panorama);

		initClick();//放大缩小 getCamera 修改setZoomLevel

		this.setListener(new PLViewListener() {
			@Override
			public void onDidClickHotspot(PLIView view, PLIHotspot hotspot, CGPoint screenPoint, PLPosition scene3dPoint) {
//				super.onDidClickHotspot(view, hotspot, screenPoint, scene3dPoint);
				ToastUtil.showShort(context, hotspot.getIdentifier()+"");
				switch ((int)hotspot.getIdentifier()) {
					case 1:
						getCamera().lookAt(0.0f, 170.0f,true);
						break;
					case 2:
//					setLocked(true);
//					reset();
//					panorama.reset();

						panorama2 = new PLSpherical2Panorama();
						panorama2.getCamera().lookAt(0.0f, 30.0f);
						panorama2.addHotspot(new PLHotspot(3, new PLImage(PLUtils.getBitmap(context, R.drawable.hotspot), false), -30.0f, 0.0f, 0.05f, 0.05f));
						panorama2.setImage(new PLImage(PLUtils.getBitmap(context, R.drawable.p2), false));
						startTransition(new PLTransitionBlend(2.0f), panorama2);

//					setLocked(false);
						break;
					case 3:
//					panorama.render(GL10., renderer);
						startTransition(new PLTransitionBlend(2.0f), panorama);
						break;

					default:
						break;
				}
			}
		});
	}


	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (isAuto) {
				getCamera().lookAt(getCamera().getPitch(), getCamera().getYaw()+0.3f);
				handler.sendEmptyMessageDelayed(1, 25);
			}
		};
	};

	Runnable autoCamera = new Runnable() {
		@Override
		public void run() {
			handler.sendEmptyMessage(1);
		}
	};

	/**
	 * 自动播放全景
	 */
	private void startAutoCamera(){
		if (!isAuto) {
			isAuto = !isAuto;
			handler.post(autoCamera);
		}
	}

	/**
	 * 停止自动播放全景
	 */
	private void endAutoCamera(){
		if (isAuto) {
			isAuto = false;
			handler.removeCallbacks(autoCamera);
		}
	}
	//注册传感器
	private boolean mRegisteredSensor;
	private void initSensor() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


		// 注册陀螺仪传感器，并设定传感器向应用中输出的时间间隔类型是SensorManager.SENSOR_DELAY_GAME(20000微秒)
		// SensorManager.SENSOR_DELAY_FASTEST(0微秒)：最快。最低延迟，一般不是特别敏感的处理不推荐使用，该模式可能在成手机电力大量消耗，由于传递的为原始数据，诉法不处理好会影响游戏逻辑和UI的性能
		// SensorManager.SENSOR_DELAY_GAME(20000微秒)：游戏。游戏延迟，一般绝大多数的实时性较高的游戏都是用该级别
		// SensorManager.SENSOR_DELAY_NORMAL(200000微秒):普通。标准延时，对于一般的益智类或EASY级别的游戏可以使用，但过低的采样率可能对一些赛车类游戏有跳帧现象
		// SensorManager.SENSOR_DELAY_UI(60000微秒):用户界面。一般对于屏幕方向自动旋转使用，相对节省电能和逻辑处理，一般游戏开发中不使用
		sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
		mRegisteredSensor = true;
	}
	//初始化点击事件
	private void initClick() {

//		deactiveAccelerometer();
//		setLocked(true);
		glSurfaceView = getGLSurfaceView();
		zoomControl = (ZoomControls) findViewById(R.id.zoomControl);
		startButton = (Button) findViewById(R.id.startButton);
		stopButton = (Button) findViewById(R.id.stopButton);
		startNew = (Button) findViewById(R.id.startNew);
		//放大
		zoomControl.setOnZoomInClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int cuttLevenl = panorama.getCamera().getZoomLevel();
				if (cuttLevenl != maxZoomLevle) {
					panorama.getCamera().setZoomLevel(cuttLevenl + 1);
				} else {
					ToastUtil.showShort(context, "不能放大了");
				}
			}
		});
		//缩小
		zoomControl.setOnZoomOutClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int cuttLevenl = panorama.getCamera().getZoomLevel();
				if(cuttLevenl > 0){
					panorama.getCamera().setZoomLevel(cuttLevenl - 1);
				} else {
					ToastUtil.showShort(context, "不能缩小了");
				}

			}
		});
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startAutoCamera();
			}
		});
		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				endAutoCamera();
			}
		});
		startNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, TestActivity.class);
				intent.putExtra("id", "100185");
				startActivity(intent);

				/*if (mRegisteredSensor) {
					onUnregister();
				} else {
					initSensor();
				}*/
			}
		});

	}
	private void onUnregister(){
		sensorManager.unregisterListener(this);
		mRegisteredSensor = false;
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
//		sensorManager.unregisterListener(this);
	}

}
