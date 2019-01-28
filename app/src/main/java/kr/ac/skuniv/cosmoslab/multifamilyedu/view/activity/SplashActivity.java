package kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import kr.ac.skuniv.cosmoslab.multifamilyedu.view.SigninActivity;

/**
 * Created by chunso on 2019-01-09.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, SigninActivity.class);
        startActivity(intent);

        finish();
    }
}
