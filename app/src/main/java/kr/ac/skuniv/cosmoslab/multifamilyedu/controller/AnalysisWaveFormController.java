package kr.ac.skuniv.cosmoslab.multifamilyedu.controller;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity.WaveFormModel;
import lombok.Getter;

/**
 * Created by chunso on 2018-11-19.
 */

@Getter
public class AnalysisWaveFormController {
    private static final String TAG = "ANALISIS_WAVEFORM_CONTROLLER";
    private static final int HIDEN_EXTREME_BOUND = 2;
    private static final int EXTREME_POINT_BOUND = 5;

    private WaveFormModel mOriginalModel;
    private WaveFormModel mRecodeModel;
    private Context context;

    public AnalysisWaveFormController(Context context,WaveFormModel _originalData, WaveFormModel _recodeData){
        this.context = context;
        this.mOriginalModel = _originalData;
        this.mRecodeModel = _recodeData;
    }

    public int getFinalScore(){
        int finalScore = 0;
        int areaScore = 0;
        try {
            areaScore = calculateAreaScore(mOriginalModel.getWaveData(), mRecodeModel.getWaveData());
        }catch (NullPointerException | ArrayIndexOutOfBoundsException e){
            Log.d(TAG, "getFinalScore: 면적점수를 계산하는데 문제가 생김");
            return 0;
        }
        int shapeScore=0;
        try {
            shapeScore = calculateShapeScore(mOriginalModel, mRecodeModel);
        }catch (Throwable e){
            Log.d(TAG, "getFinalScore: 모양점수를 계산하는데 문제가 생김");
            return 0;
        }

        if(mOriginalModel == null || mRecodeModel == null)
            return 0;

        System.out.println("//////////////////////////////////////////////////////////////////////");
        System.out.println("//////////////////////  ORIGINAL DATA  //////////////////////////////");
        System.out.println("//////////////////////////////////////////////////////////////////////");
        System.out.println("데이터 길이:  "+mOriginalModel.getWaveData().length);
        System.out.println("첫번째 데이터:  "+mOriginalModel.getWaveData()[mOriginalModel.getMaximumValueIndex()]);
        System.out.println("극점 개수:  "+mOriginalModel.getFinalExtremePoints().size());
        System.out.println("숨겨진 극점 개수:  "+mOriginalModel.getHidenCheckPoints().size());
        System.out.println(" ");
        System.out.println("//////////////////////////////////////////////////////////////////////");
        System.out.println("////////////////////////  RECORD DATA  //////////////////////////////");
        System.out.println("//////////////////////////////////////////////////////////////////////");
        System.out.println("데이터 길이:  "+mRecodeModel.getWaveData().length);
        System.out.println("첫번째 데이터:  "+mRecodeModel.getWaveData()[mRecodeModel.getMaximumValueIndex()]);
        System.out.println("극점 개수:  "+mRecodeModel.getFinalExtremePoints().size());
        System.out.println("숨겨진 극점 개수:  "+mRecodeModel.getHidenCheckPoints().size());
        System.out.println("//////////////////////////////////////////////////////////////////////");
        Log.d(TAG, "면적점수: "+areaScore+" 모양점수: "+shapeScore);

        /*
            가중치 알고리즘 필요..............
         */
        finalScore = (int)((areaScore*0.5) + (shapeScore*0.5));

        return finalScore;
    }

    /**
     * 두 파형의 합집합 면적과 교집합 면적을 나누어 얼마나 비슷한지 비교한다.
     * 모양 점수 = (두 파형의 교집합) / (두 파형의 합집합) * 100
     */
    private int calculateAreaScore(int[] originalData, int[] recodeData) throws NullPointerException, ArrayIndexOutOfBoundsException{
        int bigValue = 0, smallValue = 0;
        int sumBigValue = 0, sumSmallValue = 0;
        double score = 0;
        int smallLength = originalData.length > recodeData.length ? recodeData.length : originalData.length;
        int bigLength = originalData.length > recodeData.length ? originalData.length : recodeData.length;

        //둘중 큰 그래프 길이만큼 비교, 시작점 맞춤, 끝점 다름
        for (int i = 0; i < smallLength; i++) {
            if (originalData[i] >= recodeData[i]) {
                bigValue = originalData[i];
                smallValue = recodeData[i];
            } else if (originalData[i] < recodeData[i]) {
                bigValue = recodeData[i];
                smallValue = originalData[i];
            }
            sumBigValue = sumBigValue + bigValue;
            sumSmallValue = sumSmallValue + smallValue;
        }

        if(bigLength == originalData.length) {
            for (int i = smallLength; i < bigLength; i++)
                sumBigValue += originalData[i];
        }else{
            for (int i = smallLength; i < bigLength; i++)
                sumBigValue += recodeData[i];
        }

        score = Math.round((sumSmallValue / (double) sumBigValue) * 100);//소수점 반올림
        return (int)score;
    }

    /**
     * 두개의 wave파일의 데이터를 미분한다.
     * 1차 미분값중 부호가 변한 값의 인덱스를 체크포인트에 넣는다. v
     * 1차 미분값들을 한번더 미분하여 변화를 감지하고 숨겨진 체크포인트를 찾아 넣는다.
     *
     * 체크포인트의 값들을 하나씩 나머지 Wave파일의 파형과 비교한다.
     * 첫번째로 제대로된 극점인지를 인지한다.(100값이 넘은...)
     */
    private int calculateShapeScore(WaveFormModel originalData, WaveFormModel recodeData) {
        int finalScore =0;

        try {
            originalData = findExtremePoints(originalData);
            recodeData = findExtremePoints(recodeData);
        }catch (ArrayIndexOutOfBoundsException e){
            Log.d(TAG, "calculateShapeScore: 극점을 찾는데 문제가 생김");
            return 0;
        }

        if(originalData == null && recodeData == null)
            return 0;

        try {
            finalScore = synchronousTwoCheckPoints(originalData, recodeData);
        }catch (NullPointerException | ArrayIndexOutOfBoundsException | ArithmeticException e){
            Log.d(TAG, "calculateShapeScore: 점수를 계산하는데 문제가 생김");
            return 0;
        }

        return finalScore;
    }

    /**
     * 1차적으로 기울기가 양수 -> 음수 또는 음수 -> 양수로 변하는 부분을 극점이라 한다.
     * 2차적으로 각 극점과 극점사이의 기울기의 변화를 살펴 기울기가 변화하는 구간을 찾는다.
     * 보편적으로 극점과 극점사이의 값을 미분하였을때 1차 미분의 값의 극점이 하나가 있지만 그렇지 않은 구간이 존재한다.
     * 우리는 이 구간을 숨겨진 구간이라 하고 극점 후보로 한다.
     * 이 구간에서 기울기의 변화를 살피고 가장 큰 기울기에서 가장 작은 기울기를 뺀 값을 A라 했을때 A의 의미는 원그래프에서 얼마나 구부러진 것을 의미한다.
     * 가장 큰 기울기의 10% 이상일 경우 무시한다.
     *
     *
     */
    private WaveFormModel findExtremePoints(WaveFormModel data) throws ArrayIndexOutOfBoundsException{
        int[] firstSlopeValue = data.getFirstSlopeData();
        int[] secondSlopeValue = data.getSecondSlopeData();

        List<Integer> checkPoints = new ArrayList<>();
        List<Integer> hidenCheckPoints = new ArrayList<>();
        List<Integer> temp = new ArrayList<>();
        int sign = 1,index=-1;

        //1차적으로 1차 미분값이 부호가 변하는 체크포인트
        for (int i = 5; i < firstSlopeValue.length-5; i++) {
            if (firstSlopeValue[i] >= 0 && sign < 0) {
                checkPoints.add(i);
                sign = 1; index++;
            } else if (firstSlopeValue[i] <= 0 && sign > 0) {
                checkPoints.add(i);
                sign = -1; index++;
            }

            if(checkPoints.size() > 1 && checkPoints.get(index) < (checkPoints.get(index-1) + EXTREME_POINT_BOUND)) {
                checkPoints.remove(index);
                index--;
            }
        }

        sign = 1;
        int k =0;
        for (int i = 5; i < secondSlopeValue.length-5; i++) {
            if (secondSlopeValue[i] > 0 && sign < 0) {
                temp.add(i);
                sign = 1;
            } else if (secondSlopeValue[i] < 0 && sign > 0) {
                temp.add(i);
                sign = -1;
            }
            if (i == checkPoints.get(k) || i == secondSlopeValue.length-1) {
                if (temp.size() > 1) {
                    for (int j = 0; j < temp.size(); j++) {
                        if (j % 2 == 1)
                            hidenCheckPoints.add(temp.get(j));
                    }
                }

                if(k<checkPoints.size() && k+1 != checkPoints.size())
                    k++;
                temp.clear();
            }
        }

        if(checkPoints.isEmpty())
            return null;

        data.setCheckPoints(checkPoints);
        data.setHidenCheckPoints(hidenCheckPoints);
        return data;
    }

    /***
     *
     * 극점의 개수가 같은지 확인한다.
     * 1. 극점의 개수가 같을 때,
     *
     * 2. 극점의 개수가 같지 않을 때,
     *     체크포인트를 극점으로 변환하여 극점의 개수를 맞춘다.
     *     그래도 극점의 개수가 다를 경우 n-1까지 이전의 방식과 동일하게 진행되지만 마지막 극점은 경유하는 방식으로 진행.
     *
     *
     * 각 구간별 점수 = 100 / (원본의 극점의 개수 +1) 로 하고 Original 파일을 기준으로 계산한다.
     *
     */
    private int synchronousTwoCheckPoints(WaveFormModel _originalData, WaveFormModel _recodeData)throws NullPointerException, ArrayIndexOutOfBoundsException, ArithmeticException{
        List<Integer> originalCheckPoints = _originalData.getCheckPoints();
        List<Integer> originaalHidenCheckPoints = _originalData.getHidenCheckPoints();
        List<Integer> recodeCheckPoints = _recodeData.getCheckPoints();
        List<Integer> recodeHidenCheckPoints = _recodeData.getHidenCheckPoints();

        if(originalCheckPoints.size() != recodeCheckPoints.size()) {
            int cnt = (originalCheckPoints.size() - recodeCheckPoints.size()) / 2;
            int i = 0;

            if (cnt > 0) {
                while (i < Math.abs(cnt) && i < recodeHidenCheckPoints.size()) {
                    recodeCheckPoints.addAll(convertHidenToExtreme(_recodeData, i));
                    i++;
                }
            } else {
                while (i < Math.abs(cnt) && i < originaalHidenCheckPoints.size()) {
                    originalCheckPoints.addAll(convertHidenToExtreme(_originalData, i));
                    i++;
                }
            }
        }
        Collections.sort(originalCheckPoints); Collections.sort(recodeCheckPoints);
        _originalData.setFinalExtremePoints(originalCheckPoints); _recodeData.setFinalExtremePoints(recodeCheckPoints);

        return getShapeScore(_originalData, _recodeData);
    }

    private List<Integer> convertHidenToExtreme(WaveFormModel data, int index)throws ArrayIndexOutOfBoundsException {
        int hidenSlopeValue = 0, hidenCheckPointIndex = 0, checkPointIndex1 = 0, checkPointIndex2 = 0;
        List<Integer> hidenCheckPoints = data.getHidenCheckPoints();
        List<Integer> checkPoints = data.getCheckPoints();
        List<Integer> newCheckPoints = new ArrayList<>();
        int[] firstSlopeData = data.getFirstSlopeData();

        if(!hidenCheckPoints.isEmpty()) {
            //찾은 극점값 입력.
            int extremeValue1Index = 0;
            int extremeValue2Index = 0;

            hidenCheckPointIndex = hidenCheckPoints.get(index);
            hidenSlopeValue = firstSlopeData[hidenCheckPointIndex];

            //숨겨진 극점의 탐색 구간을 정해줌.
            for (int j = 0; j < checkPoints.size(); j++) {
                if (hidenCheckPointIndex < checkPoints.get(j)) {
                    checkPointIndex2 = checkPoints.get(j);
                    if (j != 0)
                        checkPointIndex1 = checkPoints.get(j-1);
                    else
                        checkPointIndex1 = 0;
                    break;
                }

                if (j+1 == checkPoints.size() && checkPointIndex2 == 0) {
                    checkPointIndex1 = checkPoints.get(j);
                    checkPointIndex2 = data.getWaveData().length - 1;
                }
            }

            //숨겨진 극점 중 오목한 극점을 변곡점으로 했을 때
            for (int j = hidenCheckPointIndex; j > checkPointIndex1; j--) {
                if (firstSlopeData[j] - (hidenSlopeValue * 2) > 0 && hidenSlopeValue > 0) {
                    extremeValue1Index = j;
                    newCheckPoints.add(extremeValue1Index);
                    break;
                }else if(firstSlopeData[j] - (hidenSlopeValue * 2) < 0 && hidenSlopeValue < 0) {
                    extremeValue1Index = j;
                    newCheckPoints.add(extremeValue1Index);
                    break;
                }
            }
            if(newCheckPoints.isEmpty())
                newCheckPoints.add(hidenCheckPointIndex-checkPointIndex1);

            for (int j = hidenCheckPointIndex; j < checkPointIndex2; j++) {
                if (firstSlopeData[j] - (hidenSlopeValue * 2) > 0 && hidenSlopeValue > 0) {
                    extremeValue2Index = j;
                    newCheckPoints.add(extremeValue2Index);
                    break;
                }else if(firstSlopeData[j] - (hidenSlopeValue * 2) < 0 && hidenSlopeValue < 0) {
                    extremeValue2Index = j;
                    newCheckPoints.add(extremeValue2Index);
                    break;
                }
            }
            if(newCheckPoints.size() == 1)
                newCheckPoints.add(checkPointIndex2-hidenCheckPointIndex);
        }

        return newCheckPoints;
    }

    private int getShapeScore(WaveFormModel originalModel, WaveFormModel recodeModel) throws ArrayIndexOutOfBoundsException, ArithmeticException, NullPointerException{
        List<Integer> originalExtreme = originalModel.getFinalExtremePoints();
        List<Integer> recodeExtreme = recodeModel.getFinalExtremePoints();
        int score = 0;
        double matchRate;
        List<Integer> O_compareValue = new ArrayList<>();
        List<Integer> R_compareValue = new ArrayList<>();
        List<Double> O_rateOfLengths = new ArrayList<>(), R_rateOfLengths = new ArrayList<>();

        O_compareValue.add(0);
        O_compareValue.addAll(originalExtreme);
        O_compareValue.add(originalModel.getWaveData().length - 1);
        R_compareValue.add(0);
        R_compareValue.addAll(recodeExtreme);
        R_compareValue.add(recodeModel.getWaveData().length - 1);

        int largeSize = O_compareValue.size() > R_compareValue.size() ? O_compareValue.size() : R_compareValue.size();//compareValue.size() = 시작점,끝점 포함 극점 개수
        int smallSize = O_compareValue.size() < R_compareValue.size() ? O_compareValue.size() : R_compareValue.size();//결국은 큰 구간으로 비교하는 것
        int sectionCnt = largeSize - 1; //점수낼 구간 갯수

        O_rateOfLengths = calcurateRateOfLength(O_compareValue, originalModel.getWaveData(), smallSize);
        R_rateOfLengths = calcurateRateOfLength(R_compareValue, recodeModel.getWaveData(), smallSize);


        for (int i = 0; i < O_rateOfLengths.size(); i++) {
            if (O_rateOfLengths.get(i) >= R_rateOfLengths.get(i))
                matchRate = R_rateOfLengths.get(i) / O_rateOfLengths.get(i);
            else
                matchRate = O_rateOfLengths.get(i) / R_rateOfLengths.get(i);

            score += (100 / sectionCnt) * matchRate;
        }
        return score;
    }

    private List<Double> calcurateRateOfLength(List<Integer> extremePoints, int[] waveData, int size) throws ArrayIndexOutOfBoundsException, ArithmeticException{
        List<Double> triangleHypotenuses = new ArrayList<>();
        List<Double> rateOfLength = new ArrayList<>();
        int x, y;
        double temp = 0;

        for(int i = 1; i<extremePoints.size() ; i++){
            x = extremePoints.get(i) - extremePoints.get(i-1);
            if(i%2 == 1)
                y = waveData[extremePoints.get(i)];
            else
                y = waveData[extremePoints.get(i-1)];

            if(size != extremePoints.size() && i < size - 1){
                triangleHypotenuses.add(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
                rateOfLength.add(triangleHypotenuses.get(i-1) / x);
            }else if(size == extremePoints.size() && i < size){
                triangleHypotenuses.add(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
                rateOfLength.add(triangleHypotenuses.get(i-1) / x);
            }else
                temp += Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        }
        if(size != extremePoints.size()) {
            x = extremePoints.get(extremePoints.size() - 1) - extremePoints.get(size - 1);
            rateOfLength.add(temp / x);
        }

        return rateOfLength;
    }
}
