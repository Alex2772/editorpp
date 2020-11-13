package ru.alex2772.editorpp.activity.editor.tools.findreplace;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;

public class CustomNestedScrollView extends androidx.core.widget.NestedScrollView {

    public CustomNestedScrollView(@NonNull Context context) {
        super(context);
    }

    public CustomNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private FindReplaceFragment mFragment;

    public void setFragment(FindReplaceFragment mFragment) {
        this.mFragment = mFragment;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (ev.getHistorySize() > 0) {
                float sX = ev.getHistoricalX(0, 0);
                float sY = ev.getHistoricalY(0, 0);

                float deltaX = ev.getX() - sX;
                float deltaY = ev.getY() - sY;

                if (deltaX * deltaX + deltaY * deltaY > 500) {
                    if (mFragment != null)
                        mFragment.showBackground();
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
