package kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;
import kr.ac.skuniv.cosmoslab.multifamilyedu.adapter.WordPageAdapter;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.UserController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.dto.WordInfoDto;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity.WordModel;

public class DayStatusActivity extends AppCompatActivity {
    private UserController userController;
    String mUserId, mTag;
    String mDay;
    WordInfoDto mWordInfoDto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordlist);

        Intent intent = getIntent();
        mTag = intent.getStringExtra("tag");
        mUserId = intent.getStringExtra("user_id");
        mDay = intent.getStringExtra("day");
        mWordInfoDto = (WordInfoDto) intent.getSerializableExtra("word_info");

        userController = new UserController(getApplicationContext());

        WordPageAdapter adapter = new WordPageAdapter(getApplicationContext(), mappingWordInfoDtoToWordModel(mWordInfoDto));
        ListView listview = findViewById(R.id.listView);
        listview.setAdapter(adapter);

        //해당 단어(Play Activity)로 이동
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), PlayActivity.class); // 다음넘어갈 화면
                String word = mWordInfoDto.getWordlist().get(position);
                intent.putExtra("tag", "status");
                intent.putExtra("user_id", mUserId);
                intent.putExtra("day", mDay);
                intent.putExtra("word_info", mWordInfoDto);
                intent.putExtra("word", word);
                startActivityForResult(intent,3000);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            mWordInfoDto = (WordInfoDto) data.getSerializableExtra("word_info");

            WordPageAdapter adapter = new WordPageAdapter(getApplicationContext(), mappingWordInfoDtoToWordModel(mWordInfoDto));
            ListView listview = findViewById(R.id.listView);
            listview.setAdapter(adapter);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("word_info", mWordInfoDto);
        setResult(RESULT_OK, intent);

        finish();
    }

    /*    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        mUserId = intent.getStringExtra("user_id");
        mDay = intent.getStringExtra("mDay");
        mWordInfoDto = (WordInfoDto) intent.getSerializableExtra("word_info");

        WordPageAdapter adapter = new WordPageAdapter(getApplicationContext(), mappingWordInfoDtoToWordModel(mWordInfoDto));
        ListView listview = findViewById(R.id.listView);
        listview.setAdapter(adapter);
    }*/

    private List<WordModel> mappingWordInfoDtoToWordModel(WordInfoDto wordInfoDto) {
        SharedPreferences sharedPreferences = getSharedPreferences("wordScore", MODE_PRIVATE);
        List<String> wordList = wordInfoDto.getWordlist();
        Map<String, String> pass = wordInfoDto.getWordpassinfo();
        List<WordModel> wordModels = new ArrayList<>();

        for(int i = 0 ; i<wordList.size() ; i++){
            WordModel wordModel = WordModel.builder()
                    .word(wordList.get(i))
                    .pass(pass.get(wordList.get(i)))
                    .highestScore(sharedPreferences.getInt(wordList.get(i), 0))
                    .build();
            wordModels.add(wordModel);
        }

        return wordModels;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signout:
                userController.signoutUser();
                finish();
                return true;
            case R.id.action_help:
                Intent intent = new Intent(DayStatusActivity.this, HelpActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}