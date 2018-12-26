package com.nex3z.tflitemnist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class FingerPaintView extends View {

    private static final float TOUCH_TOLERANCE = 4f;
    private static final float PEN_SIZE = 48f;

    private Bitmap drawBitmap;
    private Canvas drawCanvas;
    private Paint drawPaint = new Paint(Paint.DITHER_FLAG);
    private Paint pencil = buildPencil();
    private Path drawPath = new Path();
    private float drawX = 0f;
    private float drawY = 0f;
    private boolean isEmpty = true;

    // List of callbacks that invokes upon draw
    private List<Pair<Runnable, Executor>> onDrawCallBack = new ArrayList<>();

    public FingerPaintView(Context context) {
        super(context);
    }

    public FingerPaintView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(drawBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) {
            return;
        }
        canvas.drawBitmap(drawBitmap, 0, 0, drawPaint);
        canvas.drawPath(drawPath, pencil);
        for (Pair<Runnable, Executor> callback : onDrawCallBack) {
            callback.second.execute(callback.first);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        isEmpty = false;
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp(x, y);
                performClick();
                invalidate();
                break;
        }
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void clear() {
        drawPath.reset();
        drawBitmap = Bitmap.createBitmap(drawBitmap.getWidth(), drawBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(drawBitmap);
        isEmpty = true;
        invalidate();
    }

    public Bitmap exportToBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        draw(canvas);
        return bitmap;
    }

    public Bitmap exportToBitmap(int width, int height) {
        Bitmap rawBitmap = exportToBitmap();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap, width, height, false);
        rawBitmap.recycle();
        return scaledBitmap;
    }

    public void addOnDrawCallBack(Runnable runnable, Executor executor) {
        onDrawCallBack.add(new Pair<>(runnable, executor));
    }

    private void onTouchDown(float x, float y) {
        drawPath.reset();
        drawPath.moveTo(x, y);
        drawX = x;
        drawY = y;
    }

    private void onTouchMove(float x, float y) {
        float dx = Math.abs(x - drawX);
        float dy = Math.abs(y - drawY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(drawX, drawY, (x + drawX) / 2, (y + drawY) / 2);
            drawX = x;
            drawY = y;
        }
    }

    private void onTouchUp(float x, float y) {
        drawPath.lineTo(drawX, drawY);
        drawCanvas.drawPath(drawPath, pencil);
        drawPath.reset();
    }

    static Paint buildPencil() {
        Paint pencil = new Paint();
        pencil.setDither(true);
        pencil.setAntiAlias(true);
        pencil.setColor(Color.BLACK);
        pencil.setStyle(Paint.Style.STROKE);
        pencil.setStrokeJoin(Paint.Join.ROUND);
        pencil.setStrokeCap(Paint.Cap.ROUND);
        pencil.setStrokeWidth(PEN_SIZE);
        return pencil;
    }
}
