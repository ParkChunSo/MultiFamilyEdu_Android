package kr.ac.skuniv.cosmoslab.multifamilyedu.controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import lombok.Getter;

@Getter
public class DrawWaveFormController extends View {

    private static final String TAG = "DrawWaveFormController";
    private final Handler handler;
    private ByteArrayOutputStream waveData = new ByteArrayOutputStream();
    private Paint waveBaseLine;
    private boolean fileTypeCheck;

    public DrawWaveFormController(Context context, boolean fileTypeCheck) {
        super(context);
        handler = new Handler();
        this.fileTypeCheck = fileTypeCheck;
    }

    private Paint setOriginalWaveBaseLine() {
        Paint waveOriginalLine = new Paint();
        waveOriginalLine.setAntiAlias(true);
        waveOriginalLine.setAlpha(40);
        waveOriginalLine.setColor(Color.LTGRAY);
        waveOriginalLine.setStyle(Paint.Style.FILL_AND_STROKE);
        waveOriginalLine.setStrokeWidth(50.0f);
        waveOriginalLine.setStrokeCap(Paint.Cap.ROUND);

        return waveOriginalLine;
    }

    private Paint setRecordWaveBaseLine() {
        Paint waveRecordLine = new Paint();
        waveRecordLine.setAlpha(120);
        waveRecordLine.setColor(Color.DKGRAY);
        waveRecordLine.setStyle(Paint.Style.FILL_AND_STROKE);
        waveRecordLine.setStrokeWidth(11.0f);
        waveRecordLine.setStrokeCap(Paint.Cap.ROUND);

        return waveRecordLine;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        byte[] bs = waveData.toByteArray();
        if (bs.length == 0) {
            return;
        }

        final int margin = 2;
        int width = this.getWidth() - margin * 2;
        int height = this.getHeight() - margin * 2;

        double[] ds = NormalizeWaveFileController.convertWaveData(bs);
        {
            double[][] plots = NormalizeWaveFileController.convertPlotData(ds, width);
            float lastY = height / 2.0f;
            boolean isLastPlus = true;
            for (int x = 0; x < width; x++) {
                if (plots != null && plots[x] != null) {
                    boolean wValue = plots[x][0] > 0.0 && plots[x][1] < 0.0;
                    if (wValue) {
                        double[] values = isLastPlus ? new double[]{plots[x][1], plots[x][0]} : new double[]{plots[x][0], plots[x][1]};
                        for (double d : values) {
                            lastY = drawWaveLine(canvas, d, x, lastY, height, margin);
                        }
                    } else {
                        double value = 0.0;
                        if (plots[x][1] < 0.0) {
                            value = plots[x][1];
                            isLastPlus = false;
                        } else {
                            value = plots[x][0];
                            isLastPlus = true;
                        }
                        lastY = drawWaveLine(canvas, value, x, lastY, height, margin);
                    }
                }
            }
        }
    }

    private float drawWaveLine(Canvas canvas, double value, float x, float y, int height, int margin) {
        float nextY = height * -1 * (float) (value - 1.0) / 2.0f;
        waveBaseLine = fileTypeCheck ? setOriginalWaveBaseLine() : setRecordWaveBaseLine();
        canvas.drawLine(x + margin, y + margin, x + 1 + margin, nextY + margin, waveBaseLine);
        return nextY;
    }

    public void setOriginalWaveDisplay(String originalFilePath) {
        try {
            FileInputStream fis = new FileInputStream(new File(originalFilePath));
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                waveData.write(buf, 0, readNum);
            }
            DrawWaveFormController.this.invalidate();
        } catch (Exception e) {
            Log.d("WAVEDISPLAY : ", e.toString());
        }
    }

    public byte[] getAllWaveData() {
        return waveData.toByteArray();
    }

    public void addWaveData(byte[] data) {
        addWaveData(data, 0, data.length);
    }

    public void addWaveData(byte[] data, int offset, int length) {
        waveData.write(data, offset, length);
        fireInvalidate();
    }

    public void closeWaveData() {
        byte[] bs = waveData.toByteArray();
        byte[] data = NormalizeWaveFileController.normalizeWaveData(bs);
        waveData.reset();
        addWaveData(data);
    }

    public void clearWaveData() {
        waveData.reset();
        fireInvalidate();
    }

    private void fireInvalidate() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                DrawWaveFormController.this.invalidate();
            }
        });
    }


}
