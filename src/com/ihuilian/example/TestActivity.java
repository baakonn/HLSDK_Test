package com.ihuilian.example;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ihuilian.hlandroid_sdk.R;

public class TestActivity extends BasePLView {

	private Button startNew;
	private boolean isAuto = false;

	/** init methods */
	private static String TAG = "Tag";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initClick();
	}

	@Override
	protected View onContentViewCreated(View contentView) {
		// Load layout
		ViewGroup mainView = (ViewGroup) this.getLayoutInflater().inflate(R.layout.panorma_view_test, null);
		// Add 360 view
		mainView.addView(contentView, 0);
		// Return root content view
		return super.onContentViewCreated(mainView);
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

	//初始化点击事件
	private void initClick() {
		startNew = (Button)findViewById(R.id.startNew1);
		startNew.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isAuto) {
					endAutoCamera();
					startNew.setText("start");
				} else {
					startAutoCamera();
					startNew.setText("stop");
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
