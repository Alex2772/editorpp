package ru.alex2772.editorpp.activity.editor.theme;

public interface IEditorTheme {
    int getColor(ThemeAttr color);
    int getStyle(ThemeAttr attr);

    int getName();
}
