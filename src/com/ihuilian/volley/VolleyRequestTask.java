package com.ihuilian.volley;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.ihuilian.Constant;
import com.ihuilian.entity.MSG;
import com.ihuilian.task.ITask;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linjinfa 331710168@qq.com
 * @version 创建时间：2014年8月8日 下午5:51:35
 */
public class VolleyRequestTask<T> extends ITask {

	private Map<String, String> headerMap;
	private Class<?> cls;
	private Type type;
	private int method;
	private String url;
	/**
	 * 上传的文件
	 */
	private List<FormFile> uploadFormFileList;
	private GsonRequest<?> gsonRequest;

	/**
	 * @param taskId
	 * @param url
	 * @param method
	 *            Request.Method.POST
	 * @param headerMap
	 * @param params
	 * @param cls
	 * @param type
	 *            new TypeToken<List<T>>(){}.getType()
	 */
	private VolleyRequestTask(int taskId, String url, int method, Map<String, String> headerMap, Map<String, String> params,
			List<FormFile> uploadFormFileList, Class<?> cls, Type type) {
		super(taskId, params);
		this.headerMap = headerMap;
		this.uploadFormFileList = uploadFormFileList;
		this.cls = cls;
		this.type = type;
		this.method = method;
		this.url = url;
		appendHead();
		appendParam();
	}

	public VolleyRequestTask(int taskId, String url, int method, Map<String, String> headerMap, Map<String, String> params, Type type) {
		this(taskId, url, method, headerMap, params, null, null, type);
	}

	public VolleyRequestTask(int taskId, String url, int method, Map<String, String> headerMap, Map<String, String> params,
			List<FormFile> uploadFormFileList, Type type) {
		this(taskId, url, method, headerMap, params, uploadFormFileList, null, type);
	}

	public VolleyRequestTask(int taskId, String url, int method, Map<String, String> headerMap, Map<String, String> params, Class<?> cls) {
		this(taskId, url, method, headerMap, params, null, cls, null);
	}

	public VolleyRequestTask(int taskId, String url, int method, Map<String, String> headerMap, Map<String, String> params, String fileParaName,
			List<FormFile> uploadFormFileList, Class<?> cls) {
		this(taskId, url, method, headerMap, params, uploadFormFileList, cls, null);
	}

	public VolleyRequestTask(int taskId, String url, int method, Map<String, String> params, Type type) {
		this(taskId, url, method, null, params, null, null, type);
	}

	public VolleyRequestTask(int taskId, String url, int method, Map<String, String> params, List<FormFile> uploadFormFileList, Type type) {
		this(taskId, url, method, null, params, uploadFormFileList, null, type);
	}

	public VolleyRequestTask(int taskId, String url, int method, Map<String, String> params, Class<?> cls) {
		this(taskId, url, method, null, params, null, cls, null);
	}

	public VolleyRequestTask(int taskId, String url, int method, Map<String, String> params, List<FormFile> uploadFormFileList, Class<?> cls) {
		this(taskId, url, method, null, params, uploadFormFileList, cls, null);
	}

	public VolleyRequestTask(int taskId, String url, int method, Type type) {
		this(taskId, url, method, null, null, null, null, type);
	}

	public VolleyRequestTask(int taskId, String url, int method, List<FormFile> uploadFormFileList, Type type) {
		this(taskId, url, method, null, null, uploadFormFileList, null, type);
	}

	public VolleyRequestTask(int taskId, String url, int method, Class<?> cls) {
		this(taskId, url, method, null, null, null, cls, null);
	}

	public VolleyRequestTask(int taskId, String url, int method, List<FormFile> uploadFormFileList, Class<?> cls) {
		this(taskId, url, method, null, null, uploadFormFileList, cls, null);
	}

	@Override
	public MSG doTask() {
		return null;
	}

	/**
	 * 处理网络任务
	 * 
	 * @param handler
	 */
	@Override
	public MSG doTask(final Handler handler) {
		url = getUrl(url,method,params);
		if (type != null) {
			gsonRequest = new GsonRequest<List<T>>(method, url, headerMap, null, type, params, uploadFormFileList,
					new Listener<List<T>>() {
						@Override
						public void onResponse(List<T> response) {
							performSuccess(handler, response);
						}
					},
					new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							performError(handler, error);
						}
					});
		} else {
			gsonRequest = new GsonRequest<T>(method, url, headerMap, cls, null, params, uploadFormFileList,
					new Listener<T>() {
						@Override
						public void onResponse(T response) {
							performSuccess(handler, response);
						}
					},
					new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							performError(handler, error);
						}
					});
		}
		gsonRequest.setTag(String.valueOf(taskId));
		return new MSG(true, gsonRequest);
	}

	/**
	 * 发送返回的数据
	 * 
	 * @param handler
	 * @param data
	 */
	private void performSuccess(Handler handler, Object data) {
		Bundle bundle = getBaseBundle();
		bundle.putSerializable("result", new MSG(true, data));

		Message msg = handler.obtainMessage();
		msg.setData(bundle);
		msg.sendToTarget();
	}

	/**
	 * 发送异常信息
	 * 
	 * @param handler
	 * @param error
	 */
	private void performError(Handler handler, VolleyError error) {
		Bundle bundle = getBaseBundle();
		MSG msg = new MSG(false,getErrorMsg(error));
		if(error instanceof NetworkError){
			msg.setIsNetworkError(true);
		}
		bundle.putSerializable("result", msg);

		Message message = handler.obtainMessage();
		message.setData(bundle);
		message.sendToTarget();
	}
	
	/**
	 * 返回异常信息
	 * @param error
	 * @return
	 */
	public static String getErrorMsg(VolleyError error){
		String msg;
		if (error instanceof NetworkError) {
			msg = "网络错误";
		} else if (error instanceof NoConnectionError) {
			msg = "URL链接失败";
		} else if (error instanceof TimeoutError) {
			msg = "请求超时";
		} else if (error instanceof AuthFailureError) {
			msg = "auth 错误,未经授权";
		} else if (error instanceof ServerError) {
			msg = "服务器错误,StatusCode: " + error.networkResponse.statusCode;
		} else if (error.getMessage() != null && !"".equals(error.getMessage())) {
			msg = error.getMessage();
		} else {
			msg = "未知异常";
		}
		return msg;
	}

	/**
	 * base Bundle
	 * 
	 * @return
	 */
	private Bundle getBaseBundle() {
		Bundle bundle = new Bundle();
		bundle.putInt("taskId", taskId);
		bundle.putString("identification", getmIdentification());
		return bundle;
	}

	/**
	 * 追加头信息
	 */
	private void appendHead() {
		if (headerMap == null) {
			headerMap = new HashMap<String, String>();
		}
		// headerMap.put("Content-Type", "application/json");
		// headerMap.put("Charset", "UTF-8");
	}

	/**
	 * 追加通用参数
	 */
	private void appendParam() {
		if (params == null) {
			params = new HashMap<String, String>();
		}
//		params.put("width", String.valueOf(HLConstants.SCREEN_WIDTH));
//		params.put("height", String.valueOf(HLConstants.SCREEN_HEIGHT));
//		params.put("device_type", "android");
//		params.put("format", "json");
	}

	/**
	 * 获取URL地址
	 * 
	 * @param urlStr
	 * @return
	 */
	public static String getUrl(String urlStr,int method,Map<String,String> params) {
		String urlRe = "";
		if (urlStr.toLowerCase().startsWith("http:") || urlStr.toLowerCase().startsWith("https:")) {
			urlRe = urlStr;
		}
		urlRe = Constant.BASE_URL + urlStr;
		
		if (method == Request.Method.GET) {
			StringBuilder sb = new StringBuilder(urlRe);
			if (params != null && params.size() != 0) {
				if (!urlRe.contains("?")) {
					sb.append("?");
				}
				int index = 0;
				for (Map.Entry<String, String> entry : params.entrySet()) {
					sb.append(entry.getKey() + "=" + entry.getValue());
					if (index != params.size() - 1) {
						sb.append("&");
					}
					index++;
				}
			}
			urlRe = sb.toString();
		}
		return urlRe;
	}

	/**
	 * 取消Request
	 */
	public void cancelRequest() {
		if (gsonRequest != null) {
			gsonRequest.cancel();
		}
	}

}
