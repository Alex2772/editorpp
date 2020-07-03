package ru.alex2772.editorpp.activity.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.activity.editor.highlight.SyntaxHighlighter;
import ru.alex2772.editorpp.activity.editor.highlight.syntax.CppSyntax;
import ru.alex2772.editorpp.activity.editor.theme.ThemeManager;
import ru.alex2772.editorpp.util.PreferencesHelper;
import ru.alex2772.editorpp.view.HighlightEditText;

public class AppearanceFragment extends Fragment implements TextWatcher {
    public static final Integer[] FONT_SIZE = new Integer[]{
        4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 72
    };
    private Spinner mFontsSpinner;
    private Spinner mFontSizeSpinner;
    private HighlightEditText mEditor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.appearance_fragment, null);
        mFontsSpinner = v.findViewById(R.id.spinner2);
        mFontSizeSpinner = v.findViewById(R.id.spinner3);
        mEditor = v.findViewById(R.id.highlightEditText);
        mEditor.addTextChangedListener(this);
        mEditor.setText("// Russian Roulette program\n#include <random.h>\nint main() {\n\tint shotValue = rand() % 6;\n\tif (shotValue == 0)\n\t\tprintf(\"You died!\");\n\telse\n\t\tprintf(\"You won! %d\", shotValue);\n\treturn 0;\n}");
        mEditor.flushPreferences();
        // Retrieve fonts
        {
            mFontsSpinner.setAdapter(new FontAdapter(getContext()));
            mFontSizeSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, FONT_SIZE));
        }

        // Themes
        {
            ((RecyclerView)v.findViewById(R.id.themes)).setLayoutManager(new LinearLayoutManager(getContext()));
            ((RecyclerView)v.findViewById(R.id.themes)).setAdapter(new ThemesAdapter(getContext()));
        }

        PreferencesHelper x = new PreferencesHelper(getContext());
        x.bind("font_family", mFontsSpinner, 0);
        x.bind("font_size", mFontSizeSpinner, 5);
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(mEditor);
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(mEditor);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //mEdit.getText().setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), start, start + count, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (start < s.length()) {
            int lineBegin = start;
            for (; lineBegin > 0 && s.charAt(lineBegin - 1) != '\n'; --lineBegin) ;

            int lineEnd = start + count - 1;
            for (; lineEnd < s.length() && s.charAt(lineEnd) != '\n'; ++lineEnd) ;
            lineEnd -= 1;

            SyntaxHighlighter.highlight(getActivity(), new ThemeManager(getContext()).getTheme(), new CppSyntax(), mEditor.getText(), lineBegin, lineEnd);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
