package kr.ac.skuniv.cosmoslab.multifamilyedu.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * MultiFamilyEdu_Android
 * Class: NetRetrofit
 * Created by youngjun on 2018-11-27.
 * <p>
 * Description: SingleTone
 */

public class NetRetrofit {
    private static final NetRetrofit ourInstance = new NetRetrofit();

    public static NetRetrofit getInstance() {
        return ourInstance;
    }

    private NetRetrofit() {
    }

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://172.30.1.27:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    NetRetrofitInterface netRetrofitInterface;
    public NetRetrofitInterface getNetRetrofitInterface(){
        if(netRetrofitInterface == null){
            netRetrofitInterface = retrofit.create(NetRetrofitInterface.class);
        }
        return netRetrofitInterface;
    }
}

