package com.ihuilian.network;

import com.ihuilian.Constant;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络请求Client
 */
public class ServiceClient {
    private static ServiceInterface seviceClient;


    public static ServiceInterface getSeviceClient() {
        if (seviceClient == null) {
            //拦截器，可以统一设置heard
            OkHttpClient okClient = new OkHttpClient();
            okClient.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response response = chain.proceed(chain.request());
                    return response;
                }
            });

            Retrofit client = new Retrofit.Builder()
                    .baseUrl(Constant.BASE_URL)
//                    .addConverter(String.class, new ToStringConverter())
                    .client(okClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            seviceClient = client.create(ServiceInterface.class);
        }
        return seviceClient;
    }
}
