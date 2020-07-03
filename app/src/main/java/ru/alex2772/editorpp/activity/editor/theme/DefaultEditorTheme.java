package ru.alex2772.editorpp.activity.editor.theme;

import android.content.Context;
import android.graphics.Typeface;

import ru.alex2772.editorpp.R;

public class DefaultEditorTheme implements IEditorTheme {

    private Context mContext;

    public DefaultEditorTheme(Context context) {
        mContext = context;
    }

    @Override
    public int getColor(ThemeAttr color) {
        switch (color) {
            case KEYWORD:
                return mContext.getResources().getColor(R.color.colorPrimary);
            case OPERATOR:
                return mContext.getResources().getColor(R.color.colorPrimaryDark);

            case NUMBER:
            case STRING:
                return mContext.getResources().getColor(R.color.colorAccent);
            case COMMENT:
                return 0xff448844;
            case DEFINITION:
                return 0xff887711;
            case FUNCTION:
                return 0xff000000;
            case ANNOTATION:
                return 0xff777777;
        }
        return 0xffff0000;
    }

    @Override
    public int getStyle(ThemeAttr attr) {
        switch (attr) {
            case KEYWORD:
                return Typeface.BOLD;
            case COMMENT:
            case FUNCTION:
                return Typeface.ITALIC;
        }
        return Typeface.NORMAL;
    }

    @Override
    public int getName() {
        return R.string.tbeme_default_editor;
    }
}
