package kr.ac.skuniv.cosmoslab.multifamilyedu.controller;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity.PretreatmentModel;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity.WaveFormModel;
import lombok.Getter;

import static java.lang.System.arraycopy;

/**
 * Created by chunso on 2018-12-30.
 */

@Getter
public class PretreatmentController {
    static final int NOISE_BOUND = 500;

    private Context context;
    private WaveFormModel mOriginalModel = new WaveFormModel();
    private WaveFormModel mRecordModel = new WaveFormModel();

    private int[] mOriginalDrawModel;
    private int[] mRecordDrawModel;
    private int maximumValue;
    public PretreatmentController(Context context){
        this.context = context;
    }

    public boolean run(String originalFilePath, String recordFilePath) {
        DecodeWaveFileController decoderOriginalWAV = new DecodeWaveFileController();
        DecodeWaveFileController decoderRecodeWAV = new DecodeWaveFileController();

        try{
            File originalFile = new File(originalFilePath);
            decoderOriginalWAV.ReadFile(originalFile);
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            File recodeFile = new File(recordFilePath);
            decoderRecodeWAV.ReadFile(recodeFile);
        }catch (IOException e){
            e.printStackTrace();
        }

        PretreatmentModel originalModel = new PretreatmentModel();
        PretreatmentModel recordModel = new PretreatmentModel();

        originalModel.setWaveData(decoderOriginalWAV.getFrameGains());
        recordModel.setWaveData(decoderRecodeWAV.getFrameGains());

        if(originalModel.getWaveData() == null || recordModel.getWaveData() == null || recordModel.getWaveData().length < 50)
            return false;

        int count = 0;
        try {
            while(count < 10){
                originalModel.setWaveData(smoothingForDrawWaveform(originalModel.getWaveData(), 4));
                recordModel.setWaveData(smoothingForDrawWaveform(recordModel.getWaveData(), 4));
                count++;
            }
        }catch (IndexOutOfBoundsException e){
            messageBox("smoothingForDrawWaveform", e.getMessage());
            return false;
        }

        try {
            recordModel.setWaveData(
                    normalizeSoundSize(originalModel.getWaveData(), recordModel.getWaveData())
            );
        }catch (ArithmeticException e){
            messageBox("normalizeSoundSize", e.getMessage());
            return false;
        }

        try {
            originalModel = findStartIndexAndEndIndex(originalModel);
            recordModel = findStartIndexAndEndIndex(recordModel);
        }catch (ArrayIndexOutOfBoundsException e){
            messageBox("findStartIndexAndEndIndex", e.getMessage());
            return false;
        }


        try {
            mOriginalDrawModel = setDrawableData(originalModel);
            mRecordDrawModel = setDrawableData(recordModel);
        }catch (ArrayIndexOutOfBoundsException e){
            messageBox("setDrawableData", e.getMessage());
            return false;
        }

        maximumValue = findMaximumValueIndex(mOriginalDrawModel) > findMaximumValueIndex(mRecordDrawModel) ? mOriginalDrawModel[findMaximumValueIndex(mOriginalDrawModel)] : mRecordDrawModel[findMaximumValueIndex(mRecordDrawModel)];

        try {
            syncSpeechTime(originalModel, recordModel);
        }catch (ArithmeticException | NullPointerException e) {
            messageBox("syncSpeechTime", e.getMessage());
            return false;
        }

        count = 0;
        try {
            while(count < 5){
                mOriginalModel.setWaveData(smoothingForDrawWaveform(mOriginalModel.getWaveData(),4));
                mRecordModel.setWaveData(smoothingForDrawWaveform(mRecordModel.getWaveData(),4));
                count++;
            }
        }catch (IndexOutOfBoundsException e){
            messageBox("smoothingForDrawWaveform", e.getMessage());
            return false;
        }

        int[] originalSlope;
        int[] recodeSlope;
        try {
            originalSlope = findSlopeValue(mOriginalModel.getWaveData(), 3);
            recodeSlope = findSlopeValue(mRecordModel.getWaveData(),3);
        }catch (IndexOutOfBoundsException e){
            messageBox("findSlopeValue", e.getMessage());
            return false;
        }

        count = 0;
        try {
            while(count < 5){
                originalSlope = smoothingForDrawWaveform(originalSlope, 4);
                recodeSlope = smoothingForDrawWaveform(recodeSlope, 4);
                count++;
            }
        }catch (IndexOutOfBoundsException e){
            messageBox("smoothingForDrawWaveform", e.getMessage());
            return false;
        }

        mOriginalModel.setFirstSlopeData(originalSlope);
        mRecordModel.setFirstSlopeData(recodeSlope);

        int[] originalSlope1;
        int[] recodeSlope1;
        try{
            originalSlope1 = findSlopeValue(mOriginalModel.getFirstSlopeData(), 3);
            recodeSlope1 = findSlopeValue(mRecordModel.getFirstSlopeData(), 3);
        }catch (IndexOutOfBoundsException e){
            messageBox("findSlopeValue", e.getMessage());
            return false;
        }

        count = 0;
        try {
            while(count < 5){
                originalSlope1 = smoothingForDrawWaveform(originalSlope1, 4);
                recodeSlope1 = smoothingForDrawWaveform(recodeSlope1, 4);
                count++;
            }
        }catch (IndexOutOfBoundsException e){
            messageBox("smoothingForDrawWaveform", e.getMessage());
            return false;
        }

        mOriginalModel.setSecondSlopeData(originalSlope1);
        mRecordModel.setSecondSlopeData(recodeSlope1);

        return true;
    }

    private int[] smoothingForDrawWaveform(int[] waveData, int windowSize) throws ArrayIndexOutOfBoundsException{
        int leng = waveData.length;
        int[] resultData = new int[leng];
        float drawableData;

        for(int i=0 ; i < leng ; i++) {
            drawableData = 0;

            if (i - windowSize >= 0 && i + windowSize < leng) {
                for (int j = i - windowSize; j <= i + windowSize; j++)
                    drawableData += waveData[j];
                drawableData = drawableData / ((windowSize*2)+1);
                resultData[i] = (int) drawableData;
            }else
                resultData[i] = waveData[i];

        }
        return resultData;
    }

    private PretreatmentModel findStartIndexAndEndIndex(PretreatmentModel pretreatmentModel) throws ArrayIndexOutOfBoundsException{
        int[] waveData = pretreatmentModel.getWaveData();

        for(int i = 0 ; i < waveData.length ; i++){
            if(waveData[i]>NOISE_BOUND && waveData[i+5]>NOISE_BOUND && waveData[i+10]>NOISE_BOUND) {
                pretreatmentModel.setStartIndex(i);
                break;
            }
        }

        for(int i = waveData.length - 1; i > 5 ; i--)
        {
            if(waveData[i] < NOISE_BOUND && waveData[i-5] > NOISE_BOUND){
                pretreatmentModel.setEndIndex(i);
                break;
            }
        }

        return pretreatmentModel;
    }

    private int[] normalizeSoundSize(int[] originalWaveData, int[] recordWaveData) throws ArithmeticException{
        double graphRatio = 0.0;
        try{
            graphRatio = getAverageFromHistogram(originalWaveData, originalWaveData.length) / (double) (getAverageFromHistogram(recordWaveData, recordWaveData.length));//최댓값으로 비율 구함
        }catch (ArrayIndexOutOfBoundsException e){
            messageBox("getAverageFromHistogram",e.getMessage());
            return null;
        }

        //비율 구하기
        graphRatio = Math.round(graphRatio * 100) / 100.0;//소수점 둘째자리까지 반올림
        graphRatio = Math.abs(graphRatio);//절대값

        for(int i = 0; i< recordWaveData.length; i++)
            recordWaveData[i] = (int)(recordWaveData[i] * graphRatio);

        return recordWaveData;
    }

    private int getAverageFromHistogram(int[] waveData, int getnumFrames) throws ArrayIndexOutOfBoundsException{
        int num = (int) (Math.round(getnumFrames * 0.1));//10% 개수
        int [] HistogramGain = new int[waveData.length];
        arraycopy(waveData,0,HistogramGain,0,waveData.length);

        for(int i = 0;i<getnumFrames; i++) {
            for(int j = i; j < getnumFrames; j++)       // 첫번째 배열의 값이 두번째 배열보다
            {                                            // 작으면 자리를 바꿈
                if(HistogramGain[i] < HistogramGain[j])  // 순서대로 다음의 배열과 비교하여 작은경우에
                {                                        // 자리를 바꿈
                    int temp = HistogramGain[i];
                    HistogramGain[i] = HistogramGain[j];
                    HistogramGain[j] = temp;
                }
            }
        }

        int sum = 0;
        for(int i = 0;i<num; i++) {
            sum = sum + HistogramGain[i];
        }

        int average = (int) (sum / num);

        return average;
    }

    private void syncSpeechTime(PretreatmentModel originalModel, PretreatmentModel recordModel) throws ArithmeticException, NullPointerException{
        boolean whoIsBigger = true; //original이 더 크다고 가정
        int originalDataLengDiff = originalModel.getEndIndex() - originalModel.getStartIndex()+1;
        int recordDataLengDiff = recordModel.getEndIndex() - recordModel.getStartIndex()+1;

        if(recordDataLengDiff > originalDataLengDiff)
            whoIsBigger = false;

        if(whoIsBigger){
            int lengDiff = originalDataLengDiff - recordDataLengDiff;
            double lengGratio = (double)recordDataLengDiff / (double)originalDataLengDiff;
            double syncGratio = Math.round(((2 * lengGratio - 1)*100)) / 100.0;
            int syncLeng = (int)Math.round(lengDiff * syncGratio);
            int index = Math.round(recordDataLengDiff / syncLeng);

            try {
                originalModel.setWaveData(setArrFormatIfLarge(originalModel,originalDataLengDiff,0));
                recordModel.setWaveData(setArrFormatIfLarge(recordModel, originalDataLengDiff, index));
            }catch (ArrayIndexOutOfBoundsException e){
                messageBox("setArrFormatIfLarge",e.getMessage());
            }
        }else{
            int lengDiff = recordDataLengDiff - originalDataLengDiff;
            double lengGratio = (double)recordDataLengDiff / (double)originalDataLengDiff;
            double syncGratio = Math.round(((-2 * lengGratio + 3)*100)) / 100.0;
            int syncLeng = (int)Math.round(lengDiff * syncGratio);
            int index = Math.round(recordDataLengDiff / syncLeng);

            try {
                originalModel.setWaveData(setArrFormatIfSmall(originalModel, originalDataLengDiff, 0));
                recordModel.setWaveData(setArrFormatIfSmall(recordModel, recordDataLengDiff, index));
            }catch (ArrayIndexOutOfBoundsException e) {
                messageBox("setArrFormatIfSmall", e.getMessage());
            }
        }

        mOriginalModel.setWaveData(originalModel.getWaveData());
        mRecordModel.setWaveData(recordModel.getWaveData());
    }

    private int[] setArrFormatIfLarge(PretreatmentModel pretreatmentModel, int arrLeng, int copyIndex) throws ArrayIndexOutOfBoundsException{
        int[] newArr = new int[arrLeng];
        int[] waveArr = pretreatmentModel.getWaveData();
        int waveDataStartIndex = pretreatmentModel.getStartIndex();
        int waveDataEndIndex = pretreatmentModel.getEndIndex();

        int count = 1; int index = 0;
        while(waveDataStartIndex <= waveDataEndIndex){
            newArr[index] = waveArr[waveDataStartIndex];
            if(count == copyIndex){
                newArr[index+1] = newArr[index];
                index+=2; count = 1;waveDataStartIndex++;
                continue;
            }
            index++; count++; waveDataStartIndex++;
        }

        int[] resultArr = new int[index];;
        if(index < arrLeng)
            arraycopy(newArr, 0, resultArr, 0, index);
        else
            resultArr = newArr;

        return resultArr;
    }

    private int[] setArrFormatIfSmall(PretreatmentModel pretreatmentModel, int arrLeng, int removeIndex) throws ArrayIndexOutOfBoundsException{
        int[] newArr = new int[arrLeng];
        int[] waveArr = pretreatmentModel.getWaveData();
        int waveDataStartIndex = pretreatmentModel.getStartIndex();
        int waveDataEndIndex = pretreatmentModel.getEndIndex();

        int count = 1; int index = 0;
        while(waveDataStartIndex <= waveDataEndIndex){
            if(count == removeIndex){
                count = 1;waveDataStartIndex++;
                continue;
            }else if(index < arrLeng) {
                newArr[index] = waveArr[waveDataStartIndex];
            }
            index++; count++; waveDataStartIndex++;
        }

        int[] resultArr = new int[index];;
        if(index < arrLeng)
            arraycopy(newArr, 0, resultArr, 0, index);
        else
            resultArr = newArr;

        return resultArr;
    }

    private int[] findSlopeValue(int[] waveData, int windowSize) throws ArrayIndexOutOfBoundsException{
        int[] resultData = new int[waveData.length];
        int i = 0;
        for(i = windowSize ; i < waveData.length ; i++)
            resultData[i-windowSize] = (waveData[i] - waveData[i-windowSize])/windowSize * 10;

        for(i = i-windowSize ; i<waveData.length; i++){
            resultData[i] = resultData[i-1];
            windowSize--;
        }

        return resultData;
    }

    private int findMaximumValueIndex(int[] waveData){
        int maximumValueIndex = 0;
        int max = 0;
        for(int i = 0; i < waveData.length ; i++){
            if(waveData[i] > max) {
                max = waveData[i];
                maximumValueIndex = i;
            }
        }
        return maximumValueIndex;
    }

    private int[] setDrawableData(PretreatmentModel waveData) throws ArrayIndexOutOfBoundsException{
        int startIndex = waveData.getStartIndex();
        int endIndex = waveData.getEndIndex();
        int[] inputData = waveData.getWaveData();

        int arrStart, arrEnd;
        if(startIndex > 50){
            arrStart = startIndex - 50;
        }else
            arrStart = 0;

        //복사할 배열의 끝점
        if(endIndex + 50 <= inputData.length) {
            arrEnd = endIndex - startIndex + (startIndex - arrStart) + 50;//실제 그래프 길이 + 100
        }else
            arrEnd = (endIndex - startIndex) + (startIndex - arrStart) + (inputData.length - endIndex);

        int[] analysisData = new int[arrEnd];
        arraycopy(inputData, arrStart, analysisData, 0, arrEnd);

        return  analysisData;
    }

    private void messageBox(String method, String message) {
        Log.d("EXCEPTION: " + method,  message);

        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setTitle(method);
        messageBox.setMessage(message);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }



















}
