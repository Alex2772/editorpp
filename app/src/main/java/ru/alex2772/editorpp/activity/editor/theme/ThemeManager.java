package ru.alex2772.editorpp.activity.editor.theme;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    private final SharedPreferences mPrefs;

    private List<IEditorTheme> mThemes = new ArrayList<>();

    public ThemeManager(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        addTheme(new DefaultEditorTheme(context));
        addTheme(new EclipseEditorTheme(context));
    }

    private void addTheme(IEditorTheme theme) {
        mThemes.add(theme);
    }

    public List<IEditorTheme> getThemes() {
        return mThemes;
    }

    public IEditorTheme getTheme() {
        return mThemes.get(getThemeId());
    }

    public int getThemeId() {
        return mPrefs.getInt("theme_id", 0);
    }

    public void setTheme(int themeId) {
        mPrefs.edit().putInt("theme_id", themeId).apply();
    }
}
