package kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.DecodeWaveFileController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.DrawWaveFormController;

import static android.media.AudioFormat.ENCODING_PCM_16BIT;

public class RecordActivity extends Activity {

    private final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MultiFamily";
    private boolean isRecording = false;
    public AudioRecord mAudioRecord = null;
    private String mRecordPath, mPCMPath;
    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_STEREO;
    private int mAudioFormat = ENCODING_PCM_16BIT;
    private int mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);
    private String mWord;
    private long mTimer;
    private DrawWaveFormController recordDisplayView;
    private DecodeWaveFileController decodeWaveFileController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_record);

        Intent intent = getIntent();
        mWord = intent.getStringExtra("word");
        mRecordPath = FILE_PATH + "/RECORD/" + mWord + ".wav";
        mPCMPath = FILE_PATH + "/RECORD/" + mWord + ".pcm";

        LinearLayout recordDisplayLayout = (LinearLayout) findViewById(R.id.recordDisplayView);
        recordDisplayView = new DrawWaveFormController(getApplicationContext(), false);
        recordDisplayLayout.addView(recordDisplayView);
        decodeWaveFileController = new DecodeWaveFileController();

        onRecord();
    }

    /*//확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    public void onRecord() {
        isRecording = true;
        Toast.makeText(this, "녹음이 시작됩니다...", Toast.LENGTH_SHORT).show();
        recordDisplayView.clearWaveData();
        mTimer = System.currentTimeMillis();

        if (mAudioRecord == null) {
            mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
            mAudioRecord.startRecording();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[mBufferSize];
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mPCMPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                while (isRecording) {
                    int ret = mAudioRecord.read(readData, 0, mBufferSize);//AudioRecord의 read 함수를 통해 pcm data 를 읽어옴
                    recordDisplayView.addWaveData(readData, 0, ret);

                    try {
                        fos.write(readData, 0, mBufferSize);//읽어온 readData 를 파일에 write 함
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (System.currentTimeMillis() - mTimer > 2500) {
                        isRecording = false;
                        break;
                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;

                try {
                    fos.close();
                    decodeWaveFileController.pcmToWav(mPCMPath, mRecordPath);
                    Intent intent = new Intent();
                    intent.putExtra("result", "2222");
                    setResult(2222, intent);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }








    /*@Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }*/

}
