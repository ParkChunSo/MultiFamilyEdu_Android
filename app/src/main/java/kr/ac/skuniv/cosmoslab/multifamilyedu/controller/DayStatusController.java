package kr.ac.skuniv.cosmoslab.multifamilyedu.controller;

import android.content.Context;

import java.util.Map;

import kr.ac.skuniv.cosmoslab.multifamilyedu.model.dto.WordInfoDto;
import kr.ac.skuniv.cosmoslab.multifamilyedu.network.NetRetrofit;
import lombok.Getter;
import retrofit2.Call;
import retrofit2.Response;

/**
 * MultiFamilyEdu_Android
 * Class: DayStatusController
 * Created by hsl95 on 2019-01-01.
 * <p>
 * Description:
 */
@Getter
public class DayStatusController {
    private WordInfoDto wordInfoDto;
    private Context context;
    private Map<String, String> setPassInfo;

    public DayStatusController(Context context) {
        this.context = context;
    }


    public void getWordListByUserid(final String level, final String userid) {
        final Call<WordInfoDto> res = NetRetrofit.getInstance().getNetRetrofitInterface().getWordListByLevelAndUserid(level, userid);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<WordInfoDto> respon = res.execute();
                    if(respon.raw().code() == 200) {
                        wordInfoDto = respon.body();
                    } else {
                        wordInfoDto = null;
                    }
                } catch (Exception e) {
                    wordInfoDto = null;
                }
            }
        }).start();

        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String,String> setWordPassInfo(final String userid, final String wordname) {
        final Call<Map<String,String>> res = NetRetrofit.getInstance().getNetRetrofitInterface().setWordPassInfo(userid, wordname);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<Map<String,String>> respon = res.execute();
                    if(respon.raw().code() == 200) {
                        setPassInfo = respon.body();
                    } else {
                        setPassInfo = null;
                    }
                } catch (Exception e) {
                    setPassInfo = null;
                }
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return setPassInfo;
    }
}
