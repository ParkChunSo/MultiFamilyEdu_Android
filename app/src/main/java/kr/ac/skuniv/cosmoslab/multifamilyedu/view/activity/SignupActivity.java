package kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.UserController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.dto.SignupDto;

public class SignupActivity extends AppCompatActivity {
    private EditText idEditText;
    private EditText pwEditText;
    private EditText nameEditText;
    private EditText mobileEditText;
    private Button signupBtn;
    private Button backBtn;

    private SignupDto signupDto;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        idEditText = (EditText) findViewById(R.id.idEditText);
        pwEditText = (EditText) findViewById(R.id.pwEditText);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        mobileEditText = (EditText) findViewById(R.id.mobileEditText);
        userController = new UserController(getApplicationContext());

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signupBtn = (Button) findViewById(R.id.singupBtn);
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idEditText.getText().length()>0 && pwEditText.getText().length()>0 && nameEditText.getText().length()>0 && mobileEditText.getText().length()>0) {
                    signupDto = new SignupDto(idEditText.getText().toString(), pwEditText.getText().toString(), nameEditText.getText().toString(), mobileEditText.getText().toString());
                    userController.signupUser(signupDto);
                } else {
                    Toast.makeText(getApplicationContext(), "항목을 작성해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
