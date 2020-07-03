package ru.alex2772.editorpp.activity.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import ru.alex2772.editorpp.R;

public class ToolsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.tools_preferences, rootKey);
    }
}
