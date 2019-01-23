package kr.ac.skuniv.cosmoslab.multifamilyedu.view.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.AnalysisWaveFormController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.DayStatusController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.PretreatmentController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity.WaveFormModel;

/**
 * Created by chunso on 2018-12-04.
 */

public class DialogResult extends DialogFragment {
    private final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MultiFamily";
    String mOriginalPath;
    String mRecordPath;
    String mWord;
    int mFinalScore;
    private Map<String,String> setPassInfo;
    private DayStatusController dayStatusController;

    public interface OnCompleteListener {
        void onReplay(int score);

        void onNext(String complete, int score);

        void startStatusActivity(int score);
    }

    private OnCompleteListener mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnCompleteListener) activity;
        } catch (ClassCastException e) {
            Log.d("DialogFragmentExample", "Activity doesn'timer implement the OnCompleteListener interface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_result, null);
        builder.setView(view);

        Bundle args = getArguments();
        setPath(args.getString("word"));
        mFinalScore = args.getInt("finalScore");

        final Button replayBtn = view.findViewById(R.id.replayBtn);
        final Button selectWordBtn = view.findViewById(R.id.select_word);
        final Button statusBtn = view.findViewById(R.id.start_status_activity_button);
        final TextView scoreTV = view.findViewById(R.id.scoreTV);
        final TextView passTV = view.findViewById(R.id.passTV);

        setPassInfo = new HashMap<>();
        dayStatusController = new DayStatusController(getContext());

//        onDraw();

        scoreTV.setText(mFinalScore + "점");
        if(mFinalScore > 80) {
            passTV.setText("합격");
            passTV.setTextColor(Color.BLUE);
        }
        else {
            passTV.setText("불합격");
            passTV.setTextColor(Color.RED);
        }

        System.out.println("점수: " + mFinalScore);

        replayBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                mCallback.onReplay(mFinalScore);
            }
        });

        selectWordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mCallback.onNext("next", mFinalScore);
            }
        });

        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mCallback.startStatusActivity(mFinalScore);
            }
        });

        return builder.create();
    }



    public Bitmap onDraw() {
        PretreatmentController pretreatmentController = new PretreatmentController(getContext());
        if(!pretreatmentController.run(mOriginalPath, mRecordPath)){
            Toast.makeText(getContext(), "녹음이 잘못되었습니다. 녹음을 다시 해주십시오...", Toast.LENGTH_LONG).show();
            return null;
        }
        int[] originalArray = pretreatmentController.getMOriginalDrawModel();
        int[] recordArray = pretreatmentController.getMRecordDrawModel();
        int maximumValue = pretreatmentController.getMaximumValue();

        AnalysisWaveFormController analysisWaveform = new AnalysisWaveFormController(getContext(),pretreatmentController.getMOriginalModel(), pretreatmentController.getMRecordModel());
        mFinalScore = analysisWaveform.getFinalScore();

        if(mFinalScore == 0) {
            Toast.makeText(getContext(), "점수를 계산하는데 문제가 발생되었습니다. 녹음을 다시 해주십시오...", Toast.LENGTH_LONG).show();
            return null;
        }

        WaveFormModel originalModel = analysisWaveform.getMOriginalModel();
        WaveFormModel recordModel = analysisWaveform.getMRecodeModel();

        int bitmapX = originalArray.length > recordArray.length ? originalArray.length : recordArray.length;
        int bitmapY = maximumValue;

        Bitmap waveForm = Bitmap.createBitmap(bitmapX, bitmapY, Bitmap.Config.ARGB_8888);
        Canvas originalCanvas = new Canvas(waveForm);
        Canvas recodeCanvas = new Canvas(waveForm);

        Paint originalWaveform = new Paint();
        originalWaveform.setColor(Color.BLUE);
        originalWaveform.setAlpha(40);

        Paint recodeWaveform = new Paint();
        recodeWaveform.setColor(Color.RED);
        recodeWaveform.setAlpha(60);

        for (int i = 0; i < originalArray.length; i++)
            originalCanvas.drawLine(i, bitmapY - originalArray[i], i, bitmapY, originalWaveform);
        for (int i = 0; i < recordArray.length; i++)
            recodeCanvas.drawLine(i, bitmapY - recordArray[i], i, bitmapY, recodeWaveform);
        return waveForm;
    }

    public void setPath(String word) {
        mWord = word;
        mOriginalPath = FILE_PATH + "/ORIGINAL/" + mWord + ".wav";
        mRecordPath = FILE_PATH + "/RECORD/" + mWord + ".wav";
    }
}
