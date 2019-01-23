package kr.ac.skuniv.cosmoslab.multifamilyedu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.AnalysisWaveFormController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.FileController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.PretreatmentController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity.WaveFormModel;

/**
 * Created by chunso on 2019-01-12.
 */

public class PlayFragment extends Fragment implements PlayListener {

    private String TAG = "PlayFragment";

    public interface FragmentListener{
        void onReceivedData(String word, String score);
    }

    private final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MultiFamily";
    private final int PASS_SCORE = 70;
    private FragmentListener mFragmentListener;

    Context mContext;

    TextView textView, highestScoreTV, preScoreTV, scoreTV, passTv;

    ImageView imageView;

    String mTag, mWord, mRecordPath, mOriginalPath, mPCMPath;
    int mHighestScore, mFinalScore = 0;

    FileController fileController = new FileController(mContext);

    WaveFormModel mOriginalModel, mRecordModel;

    public PlayFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(getActivity() != null && getActivity() instanceof FragmentListener)
            mFragmentListener = (FragmentListener)getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        mContext = view.getContext();

        highestScoreTV = view.findViewById(R.id.highestScoreTV);
        preScoreTV = view.findViewById(R.id.preScore);
        textView = view.findViewById(R.id.wordTV);
        imageView = view.findViewById(R.id.displayView);
        scoreTV = view.findViewById(R.id.result_textview);
        passTv = view.findViewById(R.id.pass_textView);

        Bundle bundle = getArguments();
        mTag = bundle.getString("tag");
        mWord = bundle.getString("word");
        mHighestScore = Integer.parseInt(bundle.getString("score"));
        setEnvironment(mWord);

        if(mTag.equals("both")) {
            imageView.setImageBitmap(onDraw(mContext, mOriginalPath, mRecordPath));
            blink(mFinalScore);
        }
        else
            imageView.setImageBitmap(onDrawOriginalWaveForm());

        return view;
    }

    @Override
    public void onRecord(String word) {
        if(word.equals(mWord)) {
            Bitmap bitmap = onDraw(mContext, mOriginalPath, mRecordPath);
            if(bitmap != null)
                imageView.setImageBitmap(bitmap);

            if(mFinalScore > mHighestScore) {
                mFragmentListener.onReceivedData(mWord, String.valueOf(mFinalScore));
                highestScoreTV.setText(String.valueOf(mFinalScore));
                mHighestScore = mFinalScore;
            }

            blink(mFinalScore);
        }
    }

    public Bitmap onDrawOriginalWaveForm() {
        PretreatmentController pretreatmentController = new PretreatmentController(mContext);
        pretreatmentController.setWaveFile(mOriginalPath);
        int[] originalArray = pretreatmentController.getMOriginalDrawModel();

        int bitmapX = originalArray.length;
        int bitmapY = 5000;

        Bitmap waveForm = Bitmap.createBitmap(bitmapX, bitmapY, Bitmap.Config.ARGB_8888);
        Canvas originalCanvas = new Canvas(waveForm);

        Paint originalWaveform = new Paint();
        originalWaveform.setColor(Color.BLUE);
        originalWaveform.setAlpha(40);

        for (int i = 0; i < originalArray.length; i++) {
            originalCanvas.drawLine(i, bitmapY - originalArray[i], i, bitmapY, originalWaveform);
        }

        return waveForm;
    }

    public Bitmap onDraw(Context context, String originalPath, String recordPath) {
        PretreatmentController pretreatmentController = new PretreatmentController(context);
        try {
            if (!pretreatmentController.run(originalPath, recordPath)) {
                Toast.makeText(context, "녹음이 잘못되었습니다. 녹음을 다시 해주십시오...", Toast.LENGTH_LONG).show();
                return null;
            }
        }catch (NullPointerException | ArrayIndexOutOfBoundsException | ArithmeticException e){
            Toast.makeText(context, "녹음이 잘못되었습니다. 녹음을 다시 해주십시오...", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onDraw: " + e.getMessage());
        }
        int[] originalArray = pretreatmentController.getMOriginalDrawModel();
        int[] recordArray = pretreatmentController.getMRecordDrawModel();
        int maximumValue = pretreatmentController.getMaximumValue();

        AnalysisWaveFormController analysisWaveform = new AnalysisWaveFormController(context, pretreatmentController.getMOriginalModel(), pretreatmentController.getMRecordModel());
        mFinalScore = analysisWaveform.getFinalScore();

        if (mFinalScore == 0) {
            Toast.makeText(context, "점수를 계산하는데 문제가 발생되었습니다. 녹음을 다시 해주십시오...", Toast.LENGTH_SHORT).show();
            return null;
        }

        int bitmapX = originalArray.length > recordArray.length ? originalArray.length : recordArray.length;
        int bitmapY = 5000;

        Bitmap waveForm = Bitmap.createBitmap(bitmapX, bitmapY, Bitmap.Config.ARGB_8888);
        Canvas originalCanvas = new Canvas(waveForm);
        Canvas recodeCanvas = new Canvas(waveForm);

        Paint originalWaveform = new Paint();
        originalWaveform.setColor(Color.BLUE);
        originalWaveform.setAlpha(40);

        Paint recodeWaveform = new Paint();
        recodeWaveform.setColor(Color.RED);
        recodeWaveform.setAlpha(60);

        for (int i = 0; i < originalArray.length; i++) {
            originalCanvas.drawLine(i, bitmapY - originalArray[i], i, bitmapY, originalWaveform);
        }
        for (int i = 0; i < recordArray.length; i++) {
            recodeCanvas.drawLine(i, bitmapY - recordArray[i], i, bitmapY, recodeWaveform);
        }

        return waveForm;
    }

    public void setEnvironment(String word) {
        if (!fileController.confirmFile(word + ".wav")) {
            Toast.makeText(mContext, "파일 없으므로 다운로드", Toast.LENGTH_SHORT).show();
            fileController.downloadFileByFileName(word + ".wav");
        }

        mOriginalPath = FILE_PATH + "/ORIGINAL/" + word + ".wav";
        mRecordPath = FILE_PATH + "/RECORD/" + word + ".wav";
        mPCMPath = FILE_PATH + "/RECORD/" + word + ".pcm";
        textView.setText(word);
//        preScoreTV.setText("0");
        highestScoreTV.setText(String.valueOf(mHighestScore));
    }

    public void blink(int score){
        scoreTV.setText("점수: "+score);
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.blink_animation);
        if(score > PASS_SCORE) {
            passTv.setText("통과");
        }
        else {
            passTv.setText("불통과");
        }
        passTv.startAnimation(animation);
    }

    public Bitmap onDrawTest(Context context, String originalPath, String recordPath) {
        PretreatmentController pretreatmentController = new PretreatmentController(context);
        try {
            if (!pretreatmentController.run(originalPath, recordPath)) {
                Toast.makeText(context, "녹음이 잘못되었습니다. 녹음을 다시 해주십시오...", Toast.LENGTH_LONG).show();
                return null;
            }
        }catch (NullPointerException | ArrayIndexOutOfBoundsException | ArithmeticException e){
            Toast.makeText(context, "녹음이 잘못되었습니다. 녹음을 다시 해주십시오...", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onDraw: " + e.getMessage());
        }
        int[] originalArray = pretreatmentController.getMOriginalDrawModel();
        int[] recordArray = pretreatmentController.getMRecordDrawModel();
        int maximumValue = pretreatmentController.getMaximumValue();

        AnalysisWaveFormController analysisWaveform = new AnalysisWaveFormController(context, pretreatmentController.getMOriginalModel(), pretreatmentController.getMRecordModel());
        mOriginalModel = analysisWaveform.getMOriginalModel();
        mRecordModel = analysisWaveform.getMRecodeModel();
        mFinalScore = analysisWaveform.getFinalScore();

        if (mFinalScore == 0) {
            Toast.makeText(context, "점수를 계산하는데 문제가 발생되었습니다. 녹음을 다시 해주십시오...", Toast.LENGTH_SHORT).show();
            return null;
        }

        int bitmapX = mOriginalModel.getWaveData().length > mRecordModel.getWaveData().length ? mOriginalModel.getWaveData().length : mRecordModel.getWaveData().length;
        int bitmapY = 5000;

        Bitmap waveForm = Bitmap.createBitmap(bitmapX, bitmapY, Bitmap.Config.ARGB_8888);
        Canvas originalCanvas = new Canvas(waveForm);
        Canvas recodeCanvas = new Canvas(waveForm);

        Paint originalWaveform = new Paint();
        originalWaveform.setColor(Color.BLUE);
        originalWaveform.setAlpha(40);

        Paint recodeWaveform = new Paint();
        recodeWaveform.setColor(Color.RED);
        recodeWaveform.setAlpha(60);

        for (int i = 0; i < mOriginalModel.getWaveData().length; i++)
            originalCanvas.drawLine(i, bitmapY - mOriginalModel.getWaveData()[i], i, bitmapY, originalWaveform);
        for (int i = 0; i < mRecordModel.getWaveData().length; i++)
            recodeCanvas.drawLine(i, bitmapY - mRecordModel.getWaveData()[i], i, bitmapY, recodeWaveform);

        originalWaveform.setAlpha(80);
        recodeWaveform.setAlpha(100);
        List<Integer> originCheckPoint = mOriginalModel.getCheckPoints();
        List<Integer> recordCheckPoint = mRecordModel.getCheckPoints();

        for(int i = 0; i< originCheckPoint.size(); i++ ){
            originalCanvas.drawLine(originCheckPoint.get(i), bitmapY -mOriginalModel.getWaveData()[originCheckPoint.get(i)], originCheckPoint.get(i), bitmapY, originalWaveform);
        }

        for(int i = 0; i< recordCheckPoint.size(); i++ ){
            recodeCanvas.drawLine(recordCheckPoint.get(i), bitmapY - mRecordModel.getWaveData()[recordCheckPoint.get(i)], recordCheckPoint.get(i), bitmapY, recodeWaveform);
        }

        originalWaveform.setColor(Color.BLACK);
        recodeWaveform.setColor(Color.BLACK);

        List<Integer> originHidenCheckPoint = mOriginalModel.getHidenCheckPoints();
        List<Integer> recordHidenCheckPoint = mRecordModel.getHidenCheckPoints();

        for(int i = 0; i< originHidenCheckPoint.size(); i++ ){
            originalCanvas.drawLine(originHidenCheckPoint.get(i), bitmapY -mOriginalModel.getWaveData()[originHidenCheckPoint.get(i)], originHidenCheckPoint.get(i), bitmapY, originalWaveform);
        }

        for(int i = 0; i< recordHidenCheckPoint.size(); i++ ){
            recodeCanvas.drawLine(recordHidenCheckPoint.get(i), bitmapY - mRecordModel.getWaveData()[recordHidenCheckPoint.get(i)], recordHidenCheckPoint.get(i), bitmapY, recodeWaveform);
        }
        return waveForm;
    }
}
