package ru.alex2772.editorpp.activity.editor.theme;

import android.content.Context;

import ru.alex2772.editorpp.R;

public class EclipseEditorTheme extends DefaultEditorTheme {
    public EclipseEditorTheme(Context context) {
        super(context);
    }

    @Override
    public int getColor(ThemeAttr color) {
        switch (color) {
            case OPERATOR:
            case KEYWORD:
                return 0xff980067;
            case COMMENT:
                return 0xff308146;
            case STRING:
            case DEFINITION:
                return 0xff4f00ff;
            case NUMBER:
                return 0xff404040;
        }
        return super.getColor(color);
    }

    @Override
    public int getName() {
        return R.string.eclipse;
    }
}
