package kr.ac.skuniv.cosmoslab.multifamilyedu.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.FileController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.UserController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity.UserModel;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity.DayActivity;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity.HelpActivity;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity.SignupActivity;

public class SigninActivity extends AppCompatActivity {
    private static final String TAG = "SigninActivity";
    private EditText idEditText;
    private EditText pwEditText;
    private Button signinBtn;
    private Button signupBtn;
    private CheckBox autoSigninCheckBox;

    private UserController userController;
    private FileController fileController;
    SharedPreferences auto;
    SharedPreferences.Editor editor;
    String autoId;
    String autoPw;
    Boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        int permissionReadStorage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteStorage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionAudio = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionReadStorage == PackageManager.PERMISSION_DENIED || permissionWriteStorage == PackageManager.PERMISSION_DENIED || permissionAudio == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 2);
        }

        idEditText = (EditText) findViewById(R.id.idEditText);
        pwEditText = (EditText) findViewById(R.id.pwEditText);
        signinBtn = (Button) findViewById(R.id.loginBtn);
        signupBtn = (Button) findViewById(R.id.signupBtn);
        autoSigninCheckBox = (CheckBox) findViewById(R.id.autoSigninCheckBox);

        auto = getSharedPreferences("autoSignin", Activity.MODE_PRIVATE);
        editor = auto.edit();
        autoId = auto.getString("autoId", null);
        autoPw = auto.getString("autoPw", null);
        isFirstRun = auto.getBoolean("isFirstRun", true);

        userController = new UserController(getApplicationContext());
        fileController = new FileController(getApplicationContext());

        fileController.createFilePath();

        if (isFirstRun) {
            editor.putBoolean("isFirstRun", false);
            editor.apply();
            initSetting();
        }

        if (autoId != null) {
            userController.signinUser(autoId, autoPw);
            if (userController.getUserModel() != null) {
                Intent intent = new Intent(getApplicationContext(), DayActivity.class);
                intent.putExtra("login_model", userController.getUserModel());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "자동로그인이 실패했습니다. 다시 로그인해 주세요..", Toast.LENGTH_LONG).show();
                idEditText.setText(autoId);
                pwEditText.setText("");
            }
        }

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userController.signinUser(idEditText.getText().toString(), pwEditText.getText().toString());
                if (userController.getUserModel() == null) {
                    Toast.makeText(getApplicationContext(), "로그인이 실패했습니다. 다시 로그인해 주세요..", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), DayActivity.class);
                intent.putExtra("login_model", userController.getUserModel());
                startActivity(intent);

                try {
                    if (autoSigninCheckBox.isChecked()) {
                        editor.putString("autoId", idEditText.getText().toString());
                        editor.putString("autoPw", pwEditText.getText().toString());
                        editor.putBoolean("autoLogin", true);
                        editor.commit();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoSigninCheckBox.isChecked()) {
            finish();
        } else {
            idEditText.setText("");
            pwEditText.setText("");
        }
    }

    private void initSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("도움말");
        builder.setMessage("도움말을 보시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "도움말을 시작합니다.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SigninActivity.this, HelpActivity.class);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "도움말을 보지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }

}
