package ru.alex2772.editorpp.activity.filebrowser;

import android.graphics.drawable.Drawable;

public interface IFileItem {
    String getDisplayName();
    Drawable getIcon();
    void onClick();
}
