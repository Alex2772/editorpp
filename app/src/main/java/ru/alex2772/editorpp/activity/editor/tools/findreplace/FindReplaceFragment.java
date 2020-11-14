package ru.alex2772.editorpp.activity.editor.tools.findreplace;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.assist.AssistStructure;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.JsonReader;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.activity.editor.EditorActivity;
import ru.alex2772.editorpp.activity.editor.IEditorFragment;
import ru.alex2772.editorpp.util.MTP;
import ru.alex2772.editorpp.util.Util;

public class FindReplaceFragment extends Fragment implements ValueAnimator.AnimatorUpdateListener, TextWatcher, IEditorFragment {


    private EditorActivity mEditor;
    private CustomNestedScrollView mView;
    private boolean mHidden = false;
    private View mAdvancedOptionsWrap;
    private View mFindCardWrap;
    private View mFindCardInner;
    private float mFragmentHeight;
    private View mTextToFindLabel;
    private EditText mFindQueryEdit;
    private CheckBox mWrapCheckbox;
    private CheckBox mMatchCaseCheckbox;
    private RadioButton mSpecialSymbolsRadio;
    private RadioButton mRagioRadio;
    private View mButtonFindUp;
    private View mButtonFindDown;
    private final ArrayList<Integer> mFindEntries = new ArrayList<>();
    private TextView mErrorDisplay;

    public FindReplaceFragment() {
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        clearAllSpans();
        updateSearchOccurrences(false);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onDrawerOpened() {

    }

    @Override
    public void onDrawerClosed() {
        clearAllSpans();
    }

    private enum Direction {
        UP,
        DOWN
    }

    private void hideBackground() {
        if (mHidden)
            return;
        mHidden = true;
        mEditor.hideTopPanel();
        mAdvancedOptionsWrap.measure(0, 0);
        mFragmentHeight = mAdvancedOptionsWrap.getMeasuredHeightAndState();

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.addUpdateListener(this);
        anim.start();
    }


    void showBackground() {
        if (!mHidden)
            return;

        mHidden = false;
        mAdvancedOptionsWrap.measure(0, 0);
        mFragmentHeight = mAdvancedOptionsWrap.getMeasuredHeightAndState();

        ValueAnimator anim = ValueAnimator.ofFloat(1, 0);
        anim.addUpdateListener(this);
        anim.start();
    }


    interface FindSpan {}

    private class FindHighlightSpan extends BackgroundColorSpan implements FindSpan {

        public FindHighlightSpan() {
            super(getResources().getColor(R.color.colorPrimaryDarkFind));
        }
    }

    private class FindBorderHighlightSpan extends UnderlineSpan implements FindSpan {

        public FindBorderHighlightSpan() {
        }
    }

    private void clearAllSpans() {
        for (FindSpan s : mEditor.getText().getSpans(0, mEditor.getEditor().getEditableText().length(), FindSpan.class)) {
            mEditor.getText().removeSpan(s);
        }
    }

    private boolean nextOccurrence(Direction direction) {
        for (FindSpan s : mEditor.getText().getSpans(0, mEditor.getEditor().getEditableText().length(), FindBorderHighlightSpan.class)) {
            mEditor.getText().removeSpan(s);
        }

        int nearestPos = findNearestOccurrence(direction);

        if (nearestPos == -1)
            return false;

        mEditor.getEditor().setSelection(nearestPos, nearestPos + mFindQueryEdit.getText().length());
        mEditor.getText().setSpan(new FindBorderHighlightSpan(), nearestPos, nearestPos + mFindQueryEdit.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mEditor.scrollToCursor();
        return true;
    }
    private int findNearestOccurrence(Direction direction) {
        int start = mEditor.getEditor().getSelectionStart();
        synchronized (mFindEntries) {
            if (mFindEntries.isEmpty())
                return -1;

            int nearestPos = -1;

            switch (direction) {
                case DOWN:
                    for (int i : mFindEntries) {
                        int delta = i - start;
                        if (delta > 0) {
                            if (nearestPos == -1) {
                                nearestPos = i;
                            } else if ((nearestPos - start) > delta) {
                                nearestPos = i;
                            }
                        }
                    }
                    break;

                case UP:
                    for (int i : mFindEntries) {
                        int delta = start - i;
                        if (delta > 0) {
                            if (nearestPos == -1) {
                                nearestPos = i;
                            } else if ((start - nearestPos) > delta) {
                                nearestPos = i;
                            }
                        }
                    }
                    break;
            }

            return nearestPos;
        }
    }
    private boolean goToOccurrence(Direction direction) {
        for (FindSpan s : mEditor.getText().getSpans(0, mEditor.getEditor().getEditableText().length(), FindBorderHighlightSpan.class)) {
            mEditor.getText().removeSpan(s);
        }
        int start = mEditor.getEditor().getSelectionStart();


        synchronized (mFindEntries) {
            if (mFindEntries.isEmpty())
                return false;

            int nearestPos = -1;

            switch (direction) {
                case DOWN:
                    for (int i : mFindEntries) {
                        int delta = i - start;
                        if (delta >= 0) {
                            if (nearestPos == -1) {
                                nearestPos = i;
                            } else if ((nearestPos - start) > delta) {
                                nearestPos = i;
                            }
                        }
                    }
                    break;

                case UP:
                    for (int i : mFindEntries) {
                        int delta = start - i;
                        if (delta >= 0) {
                            if (nearestPos == -1) {
                                nearestPos = i;
                            } else if ((start - nearestPos) > delta) {
                                nearestPos = i;
                            }
                        }
                    }
                    break;
            }
            if (nearestPos == -1) {
                return false;
            }

            mEditor.getEditor().setSelection(nearestPos, nearestPos + mFindQueryEdit.getText().length());
            mEditor.getText().setSpan(new FindBorderHighlightSpan(), nearestPos, nearestPos + mFindQueryEdit.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            mEditor.scrollToCursor();

            return true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mEditor = (EditorActivity)getActivity();
        mView = (CustomNestedScrollView) inflater.inflate(R.layout.fragment_find_replace, container, false);
        mView.setFragment(this);

        mTextToFindLabel = mView.findViewById(R.id.text_to_find_label);
        mFindCardWrap = mView.findViewById(R.id.find_card_wrap);
        mFindCardInner = mView.findViewById(R.id.find_card_inner);
        mAdvancedOptionsWrap = mView.findViewById(R.id.advanced_options_wrap);

        mFindQueryEdit = mView.findViewById(R.id.find_query);
        mFindQueryEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearAllSpans();
                if (charSequence.length() > 0)
                    hideBackground();
                else {
                    showBackground();
                    return;
                }
                updateSearchOccurrences(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mFindCardWrap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showBackground();
                return false;
            }
        });

        mButtonFindDown = mView.findViewById(R.id.button_down);
        mButtonFindDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideBackground();
                if (mFindEntries.isEmpty()) {
                    Toast.makeText(getContext(),
                                   getResources().getQuantityString(R.plurals.occurrence_count, 0, 0),
                                   Toast.LENGTH_SHORT).show();
                    updateSearchButtonsDisability();
                    clearAllSpans();
                    mEditor.getEditor().setSelection(mEditor.getEditor().getSelectionStart());
                    return;
                }
                if (nextOccurrence(Direction.DOWN)) {
                    mEditor.getEditor().requestFocus();
                    updateSearchButtonsDisability();
                } else if (mWrapCheckbox.isChecked()) {
                    mEditor.getEditor().setSelection(0);
                    nextOccurrence(Direction.DOWN);

                    Toast.makeText(getContext(), R.string.search_wrapped, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonFindUp = mView.findViewById(R.id.button_up);
        mButtonFindUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideBackground();
                if (mFindEntries.isEmpty()) {
                    Toast.makeText(getContext(),
                            getResources().getQuantityString(R.plurals.occurrence_count, 0, 0),
                            Toast.LENGTH_SHORT).show();
                    updateSearchButtonsDisability();
                    clearAllSpans();
                    mEditor.getEditor().setSelection(mEditor.getEditor().getSelectionStart());
                    return;
                }
                if (nextOccurrence(Direction.UP)) {
                    mEditor.getEditor().requestFocus();
                    updateSearchButtonsDisability();
                } else if (mWrapCheckbox.isChecked()) {
                    mEditor.getEditor().setSelection(mEditor.getText().length());
                    nextOccurrence(Direction.UP);

                    Toast.makeText(getContext(), R.string.search_wrapped, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mWrapCheckbox = mView.findViewById(R.id.checkbox_wrap);
        mWrapCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateSearchButtonsDisability();
            }
        });

        mMatchCaseCheckbox = mView.findViewById(R.id.checkbox_match_case);
        mMatchCaseCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateSearchOccurrences(true);
            }
        });

        RadioGroup searchModeRagioGroup = mView.findViewById(R.id.search_mode_radio_group);
        searchModeRagioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateSearchOccurrences(true);
            }
        });
        mSpecialSymbolsRadio = mView.findViewById(R.id.special_symbols);
        mRagioRadio = mView.findViewById(R.id.regex);


        mView.findViewById(R.id.button_count).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOccurrencesCount();
            }
        });
        mErrorDisplay = mView.findViewById(R.id.error_display);

        return mView;
    }

    private void updateSearchButtonsDisability() {
        if (mWrapCheckbox.isChecked()) {
            mButtonFindUp.setEnabled(true);
            mButtonFindDown.setEnabled(true);
        } else {
            mButtonFindDown.setEnabled(findNearestOccurrence(Direction.DOWN) != -1);
            mButtonFindUp.setEnabled(findNearestOccurrence(Direction.UP) != -1);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEditor.getEditor().addTextChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearAllSpans();
        mEditor.getEditor().removeTextChangedListener(this);
    }


    private void updateSearchOccurrences(final boolean select) {
        final String text = mEditor.getText().toString();
        final String query = mFindQueryEdit.getText().toString();
        if (query.isEmpty())
            return;

        final boolean matchCase = mMatchCaseCheckbox.isChecked();
        final boolean isSpecialSymbols = mSpecialSymbolsRadio.isChecked();
        final boolean isRegex = mRagioRadio.isChecked();

        MTP.schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (mFindEntries) {
                    String t = text;
                    String q = query;
                    mFindEntries.clear();
                    if (!matchCase) {
                        t = t.toLowerCase();
                        q = q.toLowerCase();
                    }

                    if (isSpecialSymbols) {
                        boolean escapeNext = false;
                        StringBuilder newString = new StringBuilder();

                        for (int i = 0; i < q.length(); ++i) {
                            final char currentChar = q.charAt(i);
                            if (escapeNext) {
                                escapeNext = false;
                                switch (q.charAt(i)) {
                                    case '\\':  newString.append('\\');
                                        break; /* switch */

                                    case 'r':  newString.append('\r');
                                        break; /* switch */

                                    case 'n':  newString.append('\n');
                                        break; /* switch */

                                    case 'f':  newString.append('\f');
                                        break; /* switch */

                                    /* PASS a \b THROUGH!! */
                                    case 'b':  newString.append("\\b");
                                        break; /* switch */

                                    case 't':  newString.append('\t');
                                        break; /* switch */

                                    case 'a':  newString.append('\007');
                                        break; /* switch */

                                    case 'e':  newString.append('\033');
                                        break; /* switch */
                                    default:
                                        newString.append("\\").append(currentChar);

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setErrorString(getResources().getString(R.string.invalid_escaped_string_invalid_char, currentChar));
                                            }
                                        });
                                        return;
                                }
                            } else if (currentChar == '\\') {
                                escapeNext = true;
                            } else {
                                newString.append(currentChar);
                            }
                        }
                        if (escapeNext) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setErrorString(R.string.invalid_escaped_string_last_slash);
                                }
                            });
                            return;
                        }
                        q = newString.toString();
                    }

                    int index = 0;

                    for (; ; ) {
                        index = t.indexOf(q, index + 1);
                        if (index >= 0) {
                            mFindEntries.add(index);
                        } else {
                            break;
                        }
                    }

                    if (select) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (mFindEntries) {
                                    for (int indexCopy : mFindEntries) {
                                        mEditor.getText().setSpan(new FindHighlightSpan(),
                                                indexCopy,
                                                indexCopy + query.length(),
                                                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                    if (mFindEntries.isEmpty()) {
                                        setErrorString(getResources().getQuantityString(R.plurals.occurrence_count, 0, 0));
                                        return;
                                    } else {
                                        setErrorString(null
                                        );
                                    }
                                }
                                goToOccurrence(Direction.DOWN);
                            }
                        });
                    }
                }
            }

        });
    }

    private void setErrorString(@StringRes int errorString) {
        setErrorString(getContext().getString(errorString));
    }

    private void setErrorString(@Nullable String errorString) {
        if (errorString == null) {
            hideBackground();
            mErrorDisplay.setVisibility(View.GONE);
            return;
        }
        clearAllSpans();
        showBackground();
        mErrorDisplay.setText(errorString);
        mErrorDisplay.setVisibility(View.VISIBLE);
    }

    private void showOccurrencesCount() {
        String s = getResources().getQuantityString(R.plurals.occurrence_count, mFindEntries.size(), mFindEntries.size());
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float val = (float) valueAnimator.getAnimatedValue();
        float invVal = 1.f - val;

        // scrim transparency
        mEditor.setToolDrawerScrimTransparency((int) (invVal * 255));

        // text to find label height
        mTextToFindLabel.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Util.setHeight(mTextToFindLabel, (int) (invVal * mTextToFindLabel.getMeasuredHeight()));

        // advanced options height
        Util.setHeight(mAdvancedOptionsWrap, (int)(invVal * mFragmentHeight));
        mFindCardWrap.requestLayout();

        // shadow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final CardView findQueryWrap = mView.findViewById(R.id.find_card_wrap);
            findQueryWrap.setElevation(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    val * 6,
                    getResources().getDisplayMetrics()));
            findQueryWrap.invalidate();
        }

        // margin for shadow
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFindCardWrap.getLayoutParams();
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                    8 * val,
                                                     getResources().getDisplayMetrics());
        int top    = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                    4 * val,
                                                     getResources().getDisplayMetrics());
        lp.setMargins(margin, 0, margin, margin);

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                8 * invVal,
                getResources().getDisplayMetrics());
        mFindCardInner.setPadding(padding, padding, padding, padding);
    }
}