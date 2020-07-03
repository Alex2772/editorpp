package ru.alex2772.editorpp.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.alex2772.editorpp.R;

public class FileIconDrawable extends Drawable {
    private final Drawable mBackground;
    private final Paint mTextPaint;
    private final Paint mWhitePaint;
    private final float mTextHeight;
    private final Context mContext;
    private final float mDensity;
    private String mText;

    public FileIconDrawable(Context context, String text) {
        mContext = context;
        mText = text;
        mBackground = context.getResources().getDrawable(R.drawable.ic_file);

        mDensity = context.getResources().getDisplayMetrics().density;

        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18.f,context.getResources().getDisplayMetrics()));
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(context.getResources().getColor(R.color.colorPrimary));
        mTextPaint.setAntiAlias(true);

        mWhitePaint = new Paint();
        mWhitePaint.setColor(Color.WHITE);
    }

    @Override
    public void setBounds(@NonNull Rect bounds) {
        super.setBounds(bounds);
        mBackground.setBounds(bounds);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mBackground.setBounds(left, top, right, bottom);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRoundRect(new RectF(getBounds()), 16.f,  16.f, mWhitePaint);
        mBackground.draw(canvas);
        float x = getBounds().width() - mDensity * 2 - mTextPaint.measureText(mText);
        float y = getBounds().height() - mDensity;
        canvas.drawRect(x - 2 * mDensity, (y - mTextHeight), (float)getBounds().width(), (float)getBounds().height(), mWhitePaint);
        canvas.drawText(mText, x, y, mTextPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mBackground.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mBackground.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return mBackground.getOpacity();
    }
}
