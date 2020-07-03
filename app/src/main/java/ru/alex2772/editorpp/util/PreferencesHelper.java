package ru.alex2772.editorpp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.preference.PreferenceManager;

public class PreferencesHelper {

    private final SharedPreferences mPrefs;

    public PreferencesHelper(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void bind(final String key, final Spinner spinner, final int defaultId) {
        spinner.setSelection(mPrefs.getInt(key, defaultId),false);
        spinner.setSelection(mPrefs.getInt(key, defaultId),true);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPrefs.edit().putInt(key, position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
