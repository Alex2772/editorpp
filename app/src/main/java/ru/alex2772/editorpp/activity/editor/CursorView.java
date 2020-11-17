package ru.alex2772.editorpp.activity.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import ru.alex2772.editorpp.R;

public class CursorView extends View {
    private Drawable mCursorDrawable;

    public CursorView(Context context) {
        super(context);
    }

    public CursorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CursorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mCursorDrawable = getContext().getResources().getDrawable(R.drawable.cursor);
    }

    @Override
    public void draw(Canvas canvas) {
        mCursorDrawable.draw(canvas);
    }
}
