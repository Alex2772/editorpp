package ru.alex2772.editorpp.activity.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import ru.alex2772.editorpp.R;

public class EditorFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.editor_preferences, rootKey);
    }
}
