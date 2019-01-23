package kr.ac.skuniv.cosmoslab.multifamilyedu.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import kr.ac.skuniv.cosmoslab.multifamilyedu.view.SigninActivity;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.dto.SignupDto;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity.UserModel;
import kr.ac.skuniv.cosmoslab.multifamilyedu.network.NetRetrofit;
import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * MultiFamilyEdu_Android
 * Class: UserController
 * Created by youngjun on 2018-11-27.
 * <p>
 * Description:
 */
@Getter
@Setter
public class UserController {
    private static final String TAG = "FileController";
    private UserModel userModel;
    Context context;
    SharedPreferences auto;
    SharedPreferences.Editor editor;
    private boolean response;

    public UserController(Context context) {
        this.context = context;
    }

    public UserController(UserModel userModel, Context context) {
        this.userModel = userModel;
        this.context = context;
    }

    public UserModel getUserModel() {
        return this.userModel;
    }

    public boolean getResponse() {
        return response;
    }


    //로그인 메소드
    public void signinUser(final String userid, final String pw) {
        response = false;
        final Call<UserModel> res = NetRetrofit.getInstance().getNetRetrofitInterface().signin(userid, pw);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<UserModel> respon = res.execute();
                    if (respon.raw().code() == 200) {
                        userModel = respon.body();
                    } else {
                        userModel = null;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "실패");
                }
            }
        }).start();

        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //회원가입 메소드
    public void signupUser(SignupDto signupDto) {
        Call<Void> res = NetRetrofit.getInstance().getNetRetrofitInterface().singup(signupDto);
        res.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context.getApplicationContext(), "회원가입 성공", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, SigninActivity.class);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context.getApplicationContext(), "아이디 중복", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.i(TAG + "실패", t.getMessage());
                Toast.makeText(context.getApplicationContext(), "인터넷 연결 실패", Toast.LENGTH_LONG).show();
            }
        });
    }

    //로그아웃 메소드
    public void signoutUser() {
        auto = context.getSharedPreferences("autoSignin", context.MODE_PRIVATE);
        editor = auto.edit();
        editor.putBoolean("autoLogin", false);
        editor.clear();
        editor.putBoolean("isFirstRun", false);
        editor.commit();

        Intent intent = new Intent(context, SigninActivity.class);
        context.startActivity(intent);
        Toast.makeText(context.getApplicationContext(), "로그아웃", Toast.LENGTH_LONG).show();
    }

    //유저정보를 다음 LEVEL로 변경하는 메소드
    public void convertToNextDayByUser(String userid, String level) {
        response = false;
        if (Integer.parseInt(level) >= 20) {
            Toast.makeText(context.getApplicationContext(), "모든 DAY에 합격하였습니다", Toast.LENGTH_LONG).show();
            response = false;
        } else {
            int nextLevel = Integer.parseInt(level) + 1;
            level = String.valueOf(nextLevel);
        }

        final Call<Void> res = NetRetrofit.getInstance().getNetRetrofitInterface().convertToNextDay(userid, String.valueOf(level));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response<Void> respon = res.execute();
                    if (respon.raw().code() == 200) {
                        response = true;
                    } else {
                        response = false;
                    }
                } catch (Exception e) {
                    response = false;
                }
            }
        }).start();

        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
