package ru.alex2772.editorpp.util;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ViewHelper {
    public static void drawDrawable(View v, Canvas canvas, Drawable d) {
        d.setBounds(0, 0, v.getRight() - v.getLeft(), v.getBottom() - v.getTop());
        d.draw(canvas);
    }
}
