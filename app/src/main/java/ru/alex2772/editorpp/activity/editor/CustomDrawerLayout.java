package ru.alex2772.editorpp.activity.editor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class CustomDrawerLayout extends DrawerLayout {
    public CustomDrawerLayout(@NonNull Context context) {
        super(context);
    }

    public CustomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final View drawerView =  getChildAt(1);

        if (isDrawerOpen(GravityCompat.END) && !(ev.getX() >= drawerView.getX() && ev.getY() <= drawerView.getY() + drawerView.getHeight())) {
            ((EditorActivity)getContext()).ensureEditorFocus();
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
