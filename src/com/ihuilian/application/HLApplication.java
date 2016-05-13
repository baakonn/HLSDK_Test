package com.ihuilian.application;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ihuilian.entity.MSG;
import com.ihuilian.task.IUIController;
import com.ihuilian.task.TaskManager;
import com.ihuilian.task.TaskRunnable;
import com.ihuilian.volley.VolleyManger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *HLApplication
 *author:bakon(762713299@qq.com)
 *date:2016/5/11-15:36
 */
public class HLApplication extends Application{

	private static HLApplication instance;
	private static ExecutorService pool;
	public static Context appcontext;
	private TaskManager taskManager;
	private VolleyManger volleyManger;
	/**
	 * 通知IUIController 刷新控件
	 */
	protected Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			if (bundle == null){
				return;
			}
			int id = bundle.getInt("taskId");
			String identification = bundle.getString("identification");
			MSG result = (MSG) bundle.get("result");
			IUIController controller = TaskManager.getInstance().getUIController(identification);
			if (controller == null)
				return;
			controller.refreshUI(id, result);
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.instance = this;
		this.appcontext = getApplicationContext();
		//初始化线程
		TaskRunnable.getInstance(mHandler);
		//初始化TaskManager
		taskManager = TaskManager.getInstance();
		//初始化VolleyManger
		volleyManger = VolleyManger.getInstance();
	}
	/**
	 * 初始化VolleryManager
	 */
	public void initVolleryManager(){
		VolleyManger.getInstance(this, mHandler);
	}

    /**
     * applition 单例
     * @return
     */
	public static HLApplication getInstance() {
		if (instance == null) {
			instance = new HLApplication();
		}
		return instance;
	}
	/**
	 * 单例  线程池
	 * TaskManager 使用
	 * @return
	 */
	public static ExecutorService getPoolInstance() {
		if(pool==null || (pool!=null && pool.isShutdown()))
			pool = Executors.newFixedThreadPool(5);
		return pool;
	}
	
	/**
	 * 清理
	 */
	public static void clear(){
		instance = null;
		if(pool!=null)
			pool.shutdownNow();
		pool = null;
	}

}
