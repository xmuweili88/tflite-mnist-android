package com.nex3z.tflitemnist;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Executors;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private FingerPaintView mFpvPaint;
    @BindView(R.id.tv_prediction) TextView mTvPrediction;
    @BindView(R.id.tv_probability) TextView mTvProbability;
    @BindView(R.id.tv_timecost) TextView mTvTimeCost;

    private Classifier mClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mFpvPaint = findViewById(R.id.fpv_paint);
        mFpvPaint.addOnDrawCallBack(
                ()->{
                    Result result = predictResult();
                    renderResult(result);
                    },
                Executors.newSingleThreadExecutor()
        );
        init();
    }

    @OnClick(R.id.btn_clear)
    void onClearClick() {
        mFpvPaint.clear();
    }

    private void init() {
        try {
            mClassifier = new Classifier(this);
        } catch (IOException e) {
            Toast.makeText(this, R.string.failed_to_create_classifier, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "init(): Failed to create tflite model", e);
        }
    }

    @Nullable
    private Result predictResult() {
        if (mClassifier == null) {
            Log.e(LOG_TAG, "onDetectClick(): Classifier is not initialized");
            return null;
        } else if (mFpvPaint.isEmpty()) {
            return null;
        }

        Bitmap image = mFpvPaint.exportToBitmap(
                Classifier.DIM_IMG_SIZE_WIDTH, Classifier.DIM_IMG_SIZE_HEIGHT);
        // The model is trained on images with black background and white font
        Bitmap inverted = ImageUtil.invert(image);
        return mClassifier.classify(inverted);
    }

    private void renderResult(@Nullable Result result) {
        if (result != null) {
            mTvPrediction.setText(String.valueOf(result.getNumber()));
            mTvProbability.setText(String.valueOf(result.getProbability()));
            mTvTimeCost.setText(String.format(getString(R.string.timecost_value),
                    result.getTimeCost()));
        } else {
            mTvPrediction.setText(R.string.empty);
            mTvProbability.setText(R.string.empty);
            mTvTimeCost.setText(R.string.empty);
        }
    }
}
