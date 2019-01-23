package kr.ac.skuniv.cosmoslab.multifamilyedu.controller;

import android.content.Context;
import android.widget.Toast;

import java.util.Map;

import kr.ac.skuniv.cosmoslab.multifamilyedu.network.NetRetrofit;
import retrofit2.Call;

/**
 * Created by chunso on 2019-01-06.
 */

public class PlayController {
    private Map<String, String> map;
    private Context context;

    public PlayController(Context context) {
        this.context = context;
    }


    public String setWordPassInfo(final String userId, final String wordName) {
        final Call<Map<String, String>> res = NetRetrofit.getInstance().getNetRetrofitInterface().setWordPassInfo(userId, wordName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    map = res.execute().body();
                } catch (Exception e) {
                    Toast.makeText(context.getApplicationContext(), "리스트 정보 받기 실패", Toast.LENGTH_LONG).show();
                }
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wordName;
    }
}
