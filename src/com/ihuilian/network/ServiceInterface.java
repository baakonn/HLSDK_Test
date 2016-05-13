package com.ihuilian.network;

import java.io.InputStream;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Part;

/**
 * @ClassName: NetService 
 * @Description: 所有的请求接口
 * @author Bakon 762713299@qq.com
 * @date May 9, 2016
 *
 */
public interface ServiceInterface {
	
	@GET("getImage")
	Call<ResponseBody> getImageByUrl(@Part("url") String url);
	Call<InputStream> getImage(@Body RequestBody url);

}
