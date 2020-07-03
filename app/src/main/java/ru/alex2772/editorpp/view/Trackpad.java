package ru.alex2772.editorpp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.util.ViewHelper;

public class Trackpad extends View {
    private final float DOT_DST = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics());
    private final float DOT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getContext().getResources().getDisplayMetrics());
    private static final float STEP_SIZE = 30.f;

    private float mPrevX = 0;
    private float mPrevY = 0;
    private float mPrev2X = 0;
    private float mPrev2Y = 0;

    private float mOffsetX = 0;
    private float mOffsetY = 0;

    private float mStepAccX = 0;
    private float mStepAccY = 0;

    private float mAccForceX = 0;
    private float mAccForceY = 0;

    private EditText mEditText;

    public Trackpad(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    public void setEditText(EditText mEditText) {
        this.mEditText = mEditText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ViewHelper.drawDrawable(this, canvas, getResources().getDrawable(R.drawable.trackpad_bg));

        mOffsetX %= DOT_DST;
        mOffsetY %= DOT_DST;
        Paint p = new Paint();
        p.setColor(getResources().getColor(R.color.colorJoystickDots));

        for (int y = 0; y < getHeight(); y += DOT_DST) {
            for (int x = 0; x < getWidth(); x += DOT_DST) {
                canvas.drawRect(x + mOffsetX, y + mOffsetY, x + mOffsetX + DOT_SIZE, y + mOffsetY + DOT_SIZE, p);
            }
        }

        ViewHelper.drawDrawable(this, canvas, getResources().getDrawable(R.drawable.trackpad_layer));
        ViewHelper.drawDrawable(this, canvas, getResources().getDrawable(R.drawable.trackpad_frame));

        if (Math.abs(mAccForceX) > 0.1f || Math.abs(mAccForceY) > 0.1f) {
            mAccForceX *= 0.8f;
            mAccForceY *= 0.8f;
            onDrag(mAccForceX, mAccForceY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                onDrag(event.getX() - mPrevX, event.getY() - mPrevY);
            case MotionEvent.ACTION_DOWN:
                mPrev2X = mPrevX;
                mPrev2Y = mPrevY;
                mPrevX = event.getX();
                mPrevY = event.getY();
                mAccForceX = 0;
                mAccForceY = 0;
                return true;
            case MotionEvent.ACTION_UP:
                mAccForceX = mPrevX - mPrev2X;
                mAccForceY = mPrevY - mPrev2Y;
                onDrag(mAccForceX, mAccForceY);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void passAxisEvent(float axisValue, int codeLower, int codeUpper) {
        int code = axisValue < 0 ? codeLower : codeUpper;
        mEditText.onKeyDown(code, new KeyEvent(KeyEvent.ACTION_UP, code));
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        assert v != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(5);
        }
    }

    private void onDrag(float x, float y) {
        mOffsetX += x;
        mOffsetY += y;

        if (Math.signum(mStepAccX) != Math.signum(x))
            mStepAccX = x;
        else
            mStepAccX += x;
        if (Math.signum(mStepAccY) != Math.signum(y))
            mStepAccY = y * 0.4f;
        else
            mStepAccY += y * 0.4f;
        for (; Math.abs(mStepAccY) >= STEP_SIZE; mStepAccY -= STEP_SIZE * Math.signum(mStepAccY)) {
            passAxisEvent(mStepAccY, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN);
        }
        for (; Math.abs(mStepAccX) >= STEP_SIZE; mStepAccX -= STEP_SIZE * Math.signum(mStepAccX)) {
            passAxisEvent(mStepAccX, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT);
        }

        invalidate();
    }

}
