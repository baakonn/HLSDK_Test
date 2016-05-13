package com.ihuilian.volley;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.ihuilian.Constant;
import com.ihuilian.entity.ResponseObj;
import com.ihuilian.util.InputStreamUtil;
import com.ihuilian.util.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author linjinfa 331710168@qq.com
 * @version 创建时间：2014年8月7日 下午5:12:33
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GsonRequest<T> extends Request<T> {

	/**
	 * 超时时间 毫秒
	 */
	private final static int TIMEOUT = 10000;
	/**
	 * 最大自动重新请求次数
	 */
	private final static int MAX_RETRY_COUNT = 3;
	/**
	 * json前缀
	 */
	private String jsonDataTarget = "data";

	private Class<?> clazz;
	private Type type;
	private Map<String, String> headerMap;
	private Map<String, String> paramMap;
	/**
	 * 上传的文件
	 */
	private List<FormFile> uploadFileList;
	private Listener<T> listener;
	private Gson gson = new Gson();

	/**
	 * 
	 * @param method
	 * @param url
	 * @param headerMap
	 * @param clazz
	 * @param type
	 * @param paramMap
	 * @param listener
	 * @param errorListener
	 */
	public GsonRequest(int method, String url, Map<String, String> headerMap, Class<?> clazz, Type type, Map<String, String> paramMap,
			List<FormFile> uploadFileList, Listener listener, ErrorListener errorListener) {
		super(method, url, errorListener);
		this.clazz = clazz;
		this.type = type;
		this.headerMap = headerMap;
		this.paramMap = paramMap;
		this.uploadFileList = uploadFileList;
		this.listener = listener;
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		setShouldCache(true);
		setRetryPolicy(new DefaultRetryPolicy(TIMEOUT, MAX_RETRY_COUNT, 1));
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return headerMap==null?new HashMap<String,String>():headerMap;
	}

	@Override
	public Map<String, String> getParams() throws AuthFailureError {
		return paramMap==null?new HashMap<String,String>():paramMap;
	}

	/**
	 * 获取上传的文件
	 * 
	 * @return
	 */
	public List<FormFile> getUploadFileList() {
		return uploadFileList;
	}

	@Override
	protected void deliverResponse(T response) {
		listener.onResponse(response);
	}

	@Override
	protected Response parseNetworkResponse(NetworkResponse response) {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.data);
		InputStream inputStream = null;
		if (isGzip(response.data)) { // GZip
			try {
				inputStream = new GZIPInputStream(byteArrayInputStream);
			} catch (IOException e) {
				return Response.error(new VolleyError("数据解压异常"));
			}
		} else {
			inputStream = byteArrayInputStream;
		}
//		if(response.statusCode!=200){
//			return Response.error(new VolleyError("Http Code "+response.statusCode));
//		}
		String jsonStr = InputStreamUtil.InputStreamTOStringUTF8(inputStream);
		if(Constant.DEBUG)
			System.out.println("服务器返回======>  "+jsonStr);
		
		if(TextUtils.isEmpty(jsonStr)){
			return Response.error(new VolleyError("数据为空"));
		}
		ResponseObj responseObj = null;
		try {
			responseObj = gson.fromJson(jsonStr, ResponseObj.class);
		} catch (Exception e1) {}
		if(responseObj==null){
			return Response.error(new VolleyError("数据解析异常"));
		}
		if("0000".equals(responseObj.getStatus())){
			if (type != null) { // 解析成List
				JsonUtil jsonUtil = new JsonUtil(jsonDataTarget, type);
				List list = jsonUtil.readArrayByJsonStr(jsonStr);
				if (list == null) {
					return Response.error(new VolleyError("数据解析异常"));
				}
				return Response.success(list, HttpHeaderParser.parseCacheHeaders(response));
			}else{ // 解析成Object
				try {
					JsonUtil jsonUtil = new JsonUtil(jsonDataTarget, clazz);
					Object obj = jsonUtil.readObjectJsonStr(jsonStr);
					return Response.success(obj, HttpHeaderParser.parseCacheHeaders(response));
				} catch (Exception e) {
					return Response.error(new VolleyError("数据解析异常"));
				}
			}
		}else{
			return Response.error(new VolleyError(responseObj.getMessage()));
		}
	}

	/**
	 * 是否Gzip数据
	 * 
	 * @param data
	 * @return
	 */
	private boolean isGzip(byte[] data) {
		if (data == null || data.length < 2) {
			return false;
		}
		int head = ((int) ((data[0] << 8) | data[1] & 0xFF));
		return head == 0x1f8b;
	}

	/**
	 * 设置json前缀
	 * 
	 * @param jsonDataTarget
	 */
	public void setJsonDataTarget(String jsonDataTarget) {
		this.jsonDataTarget = jsonDataTarget;
	}

}
