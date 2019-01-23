package kr.ac.skuniv.cosmoslab.multifamilyedu.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;
import kr.ac.skuniv.cosmoslab.multifamilyedu.adapter.PlayPageAdapter;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.FileController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.PlayController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.UserController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.dto.WordInfoDto;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity.DayStatusActivity;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity.HelpActivity;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity.RecordActivity;

import static android.media.AudioFormat.ENCODING_PCM_16BIT;

/**
 * Created by chunso on 2018-12-03.
 */

public class PlayActivity extends AppCompatActivity implements PlayFragment.FragmentListener{
    private String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MultiFamily";;
    private final int PASS_SCORE = 70;

    ViewPager viewPager;
    private Button playBtn, recordBtn, recordPlayBtn, submitBtn;
    private String mTag, mWord, mRecordPath, mOriginalPath, mPCMPath;
    private String mUserId, mDay;
    WordInfoDto mWordInfoDto;
    List<String> mPassWords = new ArrayList<>();
    List<String> mFailWords = new ArrayList<>();
    boolean isRecording = false;
    boolean isPlaying = false;
    SharedPreferences sharedPreferences;

    private int mHighestScore;

    public AudioRecord mAudioRecord = null;
    public AudioTrack mAudioTrack = null;
    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = ENCODING_PCM_16BIT;
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    FileController fileController;
    PlayPageAdapter playPageAdapter;
    UserController userController;

    int mPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        sharedPreferences = getSharedPreferences("wordScore", MODE_PRIVATE);
        fileController = new FileController(getApplicationContext());
        userController = new UserController(getApplicationContext());

        Intent intent = getIntent();
        mTag = intent.getStringExtra("tag");
        mUserId = intent.getStringExtra("user_id");
        mDay = intent.getStringExtra("day");
        mWordInfoDto = (WordInfoDto) intent.getSerializableExtra("word_info");

        mFailWords = findFailWords();

        if(mTag.equals("status")) {
            mWord = intent.getStringExtra("word");
            if(!mFailWords.contains(mWord))
                mFailWords.add(0, mWord);
            else {
                mFailWords.remove(mWord);
                mFailWords.add(0, mWord);
            }
        }
        else
            mWord = mFailWords.get(0);

        setEnvironment(mWord);

        playPageAdapter = new PlayPageAdapter(getSupportFragmentManager(), mFailWords, findHighScore(mFailWords));

        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(playPageAdapter);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mWord = mFailWords.get(position);
                setEnvironment(mWord);
                mPosition = position;
                mHighestScore = sharedPreferences.getInt(mWord, 0);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        playBtn = findViewById(R.id.playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(v, mOriginalPath);
            }
        });

        recordBtn = findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RecordActivity.class);
                intent.putExtra("word", mWord);
                startActivityForResult(intent, 1111);
            }
        });

        recordPlayBtn = findViewById(R.id.recordPlayBtn);
        recordPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(v, mRecordPath);
            }
        });

        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWordInfoDto.setWordpassinfo(syncData(mPassWords));

                if(mTag.equals("status")) {
                    Intent intent = new Intent();
                    intent.putExtra("word_info", mWordInfoDto);
                    setResult(RESULT_OK, intent);

                    finish();
                }else{
                    Intent intent = new Intent(getApplicationContext(), DayStatusActivity.class);
                    intent.putExtra("tag", "play");
                    intent.putExtra("user_id", mUserId);
                    intent.putExtra("day", mDay);
                    intent.putExtra("word_info", mWordInfoDto);
                    startActivityForResult(intent, 3000);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111 && resultCode == 2222) {
            playPageAdapter.update(mFailWords.get(mPosition), mPosition);
        }
        else if(requestCode == 3000){

        }
    }

    @Override
    public void onBackPressed() {
        mWordInfoDto.setWordpassinfo(syncData(mPassWords));

        Intent intent = new Intent();
        intent.putExtra("word_info", mWordInfoDto);
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public void onReceivedData(String word, String score) {
        mWord = word;
        saveScore(Integer.parseInt(score));
    }

    public void onPlay(View view, final String path) {
        if (isPlaying) {
            isPlaying = false;
            if(path.contains("RECORD"))
                recordPlayBtn.setText("녹음확인");
            else
                playBtn.setText("단어듣기");
        } else {
            isPlaying = true;
            if(path.contains("RECORD"))
                recordPlayBtn.setText("멈춤");
            else
                playBtn.setText("멈춤");

            if (mAudioTrack == null) {
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannelCount, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] writeData = new byte[mBufferSize];
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(path);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }

                    DataInputStream dis = new DataInputStream(fis);
                    mAudioTrack.play();  // write 하기 전에 play 를 먼저 수행해 주어야 함

                    while (isPlaying) {
                        try {
                            int ret = dis.read(writeData, 0, mBufferSize);
                            if (ret <= 0) {
                                (PlayActivity.this).runOnUiThread(new Runnable() { // UI 컨트롤을 위해
                                    @Override
                                    public void run() {
                                        isPlaying = false;
                                        if(path.contains("RECORD"))
                                            recordPlayBtn.setText("녹음확인");
                                        else
                                            playBtn.setText("단어듣기");
                                    }
                                });
                                break;
                            }
                            mAudioTrack.write(writeData, 0, ret); // AudioTrack 에 write 를 하면 스피커로 송출됨
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mAudioTrack.stop();
                    mAudioTrack.release();
                    mAudioTrack = null;

                    try {
                        dis.close();
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private List<String> findFailWords() {
        List<String> wordList = mWordInfoDto.getWordlist();
        Map<String, String> map = mWordInfoDto.getWordpassinfo();
        List<String> failWords = new ArrayList<>();

        for(int i = 0 ; i< wordList.size() ; i++){
            if(map.get(wordList.get(i)).equals("불합격")) {
                failWords.add(wordList.get(i));
            }
        }
        if( failWords.size() == 0)
            failWords = wordList;

        Random random = new Random();
        int leng = failWords.size();
        int[] indexArr = new int[leng];
        for(int i = 0 ; i<leng ; i++){
            indexArr[i] = random.nextInt(leng);
            for(int j = 0; j<i ; j++) {
                if(indexArr[i] == indexArr[j]) {
                    i--;
                }
            }
        }

        List<String> renewalFailWords = new ArrayList<>();
        for(int i = 0; i< leng ; i++)
            renewalFailWords.add(failWords.get(indexArr[i]));


        return renewalFailWords;
    }

    public List<Integer> findHighScore(List<String> wordList){
        List<Integer> wordScore = new ArrayList<>();
        for(int i = 0 ; i< wordList.size() ; i++){
            wordScore.add(sharedPreferences.getInt(wordList.get(i), 0));
        }
        return wordScore;
    }

    public void saveScore(int score) {
        if (mHighestScore != 0 && score > mHighestScore) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(mWord, score);
            editor.commit();

            mHighestScore = score;
            System.out.println("값이 높아서 저장됨.");
        } else if (mHighestScore == 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(mWord, score);
            editor.commit();

            mHighestScore = score;
            System.out.println("값이 없어서 저장됨");
        }

        if(mHighestScore > PASS_SCORE && mWordInfoDto.getWordpassinfo().get(mWord).equals("불합격")){
            PlayController playController = new PlayController(getApplicationContext());
            mPassWords.add(playController.setWordPassInfo(mUserId, mWord));
        }

        System.out.println("점수: " + score);
        System.out.println(sharedPreferences.getInt(mWord, 0));
    }

    public Map<String, String> syncData(List<String> passWord){
        Map<String, String> map = mWordInfoDto.getWordpassinfo();
        for(int i = 0 ; i< passWord.size() ; i++)
            map.put(passWord.get(i), "합격");
        return map;
    }
    public void setEnvironment(String word) {
        if (!fileController.confirmFile(word + ".wav")) {
            Toast.makeText(getApplicationContext(), "파일 없으므로 다운로드", Toast.LENGTH_SHORT).show();
            fileController.downloadFileByFileName(word + ".wav");
        }

        mOriginalPath = FILE_PATH + "/ORIGINAL/" + word + ".wav";
        mRecordPath = FILE_PATH + "/RECORD/" + word + ".wav";
        mPCMPath = FILE_PATH + "/RECORD/" + word + ".pcm";
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                Intent intent = new Intent(PlayActivity.this, HelpActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_signout:
                userController.signoutUser();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
