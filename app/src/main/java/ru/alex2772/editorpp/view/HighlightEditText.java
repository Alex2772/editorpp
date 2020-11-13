package ru.alex2772.editorpp.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;

import androidx.preference.PreferenceManager;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.activity.editor.highlight.syntax.IHighlighter;
import ru.alex2772.editorpp.activity.editor.highlight.syntax.PythonSyntax;
import ru.alex2772.editorpp.activity.settings.AppearanceFragment;
import ru.alex2772.editorpp.activity.settings.FontAdapter;

public class HighlightEditText extends EditText implements SharedPreferences.OnSharedPreferenceChangeListener, TextWatcher {
    private ISelectionChangedListener mSelectionChangedListener;
    private TextWatcher mWatcher;
    private SharedPreferences mPrefs = null;
    private boolean mShowLineNumber = true;
    private Paint mPaint = new Paint();
    private IHighlighter mIHighlighter;

    public HighlightEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/JetBrainsMono-Regular.ttf"));
        super.addTextChangedListener(this);

        updateHorizontalScroll();
        setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        mPaint.setTypeface(Typeface.MONOSPACE);
        mShowLineNumber = getPrefs().getBoolean("show_line_numbers", true);
        //setMovementMethod(new ScrollingMovementMethod());
        updatePadding();
    }

    private void updateHorizontalScroll() {
        setHorizontallyScrolling(getPrefs().getBoolean("horizontal_scroll", true));

    }

    private SharedPreferences getPrefs() {
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        }
        return mPrefs;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);

        if (mSelectionChangedListener != null)
            mSelectionChangedListener.onSelectionChanged(this, selStart, selEnd);

        // brackets highlight
        for (BracketSpan s : getText().getSpans(0, getText().length(), BracketSpan.class))
            getText().removeSpan(s);
        if (getPrefs().getBoolean("highlight_brackets", true)) {
            for (char[] b : new char[][]{
                    {'{', '}'},
                    {'(', ')'},
                    {'[', ']'},
            }) {
                for (int i = -1; i <= 0; ++i) {
                    try {
                        if (getText().charAt(selStart + i) == b[1]) { // find start }
                            int stack = 0;
                            for (int j = selStart + i - 2; j >= 0; --j) { // find end {
                                if (getText().charAt(j) == b[1]) {
                                    ++stack;
                                } else if (getText().charAt(j) == b[0]) {
                                    if (stack == 0) {
                                        // end found
                                        getText().setSpan(new BracketSpan(), selStart + i, selStart + i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        getText().setSpan(new BracketSpan(), j, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        return;
                                    } else {
                                        --stack;
                                    }
                                }
                            }
                        } else if (getText().charAt(selStart + i) == b[0]) {
                            int stack = 0;
                            for (int j = selStart + i + 1; j < getText().length(); ++j) { // find end }
                                if (getText().charAt(j) == b[0]) {
                                    ++stack;
                                } else if (getText().charAt(j) == b[1]) {
                                    if (stack == 0) {
                                        // end found
                                        getText().setSpan(new BracketSpan(), selStart + i, selStart + i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        getText().setSpan(new BracketSpan(), j, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        return;
                                    } else {
                                        --stack;
                                    }
                                }
                            }
                        } else {
                            continue;
                        }
                        // pair was not found
                        getText().setSpan(new BracketSpan(), selStart, selStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (Exception ignored) {

                    }
                }
            }
        }
    }

    public void setSelectionChangedListener(ISelectionChangedListener mSelectionChangedListener) {
        this.mSelectionChangedListener = mSelectionChangedListener;
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
        mWatcher = watcher;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "font_family":
                setTypeface(FontAdapter.getTypefaces(getContext()).get(sharedPreferences.getInt(key, 0)).mTypeface);
                break;
            case "font_size":
                setTextSize(TypedValue.COMPLEX_UNIT_PT, AppearanceFragment.FONT_SIZE[sharedPreferences.getInt(key, 5)]);
                mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, AppearanceFragment.FONT_SIZE[sharedPreferences.getInt(key, 5)] - 1, getResources().getDisplayMetrics()));
                updatePadding();
                break;
            case "highlight_syntax":
            case "theme_id":
                // invalidate highlight
                invalidateFullHighlight();
                break;
            case "horizontal_scroll":
                updateHorizontalScroll();
                break;
            case "show_line_numbers":
                mShowLineNumber = getPrefs().getBoolean(key, true);
                updatePadding();
                break;
        }
    }

    private void updatePadding() {
        int left = 8;
        if (mShowLineNumber) {
            left += (int)mPaint.measureText("0000");
        }
        setPadding(left, 12, 6, 0);
    }

    public void invalidateFullHighlight() {
        if (mWatcher != null) {
            mWatcher.onTextChanged(getText().toString(), 0, 0, getText().length());
        }
    }


    public void flushPreferences() {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        onSharedPreferenceChanged(sPref, "font_family");
        onSharedPreferenceChanged(sPref, "font_size");
    }


    private static final char[][] brackets = {
            {'{', '}'},
            {'(', ')'},
            {'[', ']'},
            {'\"', '\"'},
            {'\'', '\''},
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (after == 0 && count == 1 && mTextChangedFlag) { // erase
            try {
                mTextChangedFlag = false;
                for (char[] x : brackets) {
                    if (x[0] == s.charAt(start)) {
                        if (s.charAt(start + 1) == x[1]) {
                            getText().replace(start, start + 1, "");
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            mTextChangedFlag = true;

        }
    }

    private int mAppendPos = -1;
    private char mAppendChar;
    private boolean mTextChangedFlag = true;

    public String getTabs() {
        if (getPrefs().getBoolean("ac_tabulation", true)) {
            if (getPrefs().getString("ac_tabulation_symbol", "whitespaces").equals("whitespaces"))
                return "    ";
            else
                return "\t";
        }
        return "";
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mTextChangedFlag) {
            if (getPrefs().getBoolean("ac_tabulation", true) && count == 1) {
                try {
                    if (s.charAt(start) == '\n' && getSelectionStart() == start + 1) {
                        mTextChangedFlag = false;
                        // count indention
                        int newLineIndex = s.toString().lastIndexOf('\n', start - 1);
                        int indention = 0;
                        if (newLineIndex != -1) {
                            for (int i = newLineIndex + 1; i < s.length(); ++i, ++indention) {
                                if (s.charAt(i) != ' ' && s.charAt(i) != '\t')
                                    break;
                            }
                        }


                        StringBuilder x = new StringBuilder();
                        for (int i = 0; i < indention; i += getTabs().length()) {
                            x.append(getTabs());
                        }
                        if (start - 1 > 0 && start - 1 < s.length() && (s.charAt(start - 1) == '{'
                                || (s.charAt(start - 1) == ':' && mIHighlighter instanceof PythonSyntax) // Python
                        )) { // check for start bracket
                            StringBuilder sb = new StringBuilder(x + getTabs());
                            if (start + 1 < s.length() && s.charAt(start + 1) == '}') { // check for end bracket
                                sb.append("\n");
                                getText().insert(start + 1, sb.toString() + x);
                                setSelection(getSelectionStart() - 1 - x.length());
                            } else {
                                getText().insert(start + 1, sb);
                            }
                        } else {
                            getText().insert(start + 1, x);
                        }
                        mTextChangedFlag = true;
                        return;
                    }
                } catch (Exception ignored) {
                }
            }

            if (getPrefs().getBoolean("ac_bracket_pair", true)) {
                if (count > 0) {
                    try {
                        // append end bracket
                        char c = s.charAt(start + count - 1);
                        for (char[] x : brackets) {
                            if (c == x[0]) {
                                mAppendPos = start + count;
                                mAppendChar = x[1];
                                return;
                            }
                        }
                        // check for end bracket
                        char n = s.charAt(start + count);
                        for (char[] x : brackets) {
                            if (c == n && n == x[1]) {
                                mAppendPos = start + count;
                                mAppendChar = 0;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mAppendPos != -1 && mTextChangedFlag) {
            mTextChangedFlag = false;
            if (mAppendChar == 0) {// erase character
                s.replace(mAppendPos - 1, mAppendPos, "");
                try {
                    setSelection(getSelectionStart() + 1);
                } catch (Exception ignored) {
                }
            } else {
                int sel = getSelectionStart();
                s.insert(mAppendPos, Character.toString(mAppendChar));
                setSelection(sel);
            }
            mTextChangedFlag = true;
            mAppendPos = -1;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Render error", e);
        }
        float barWidth = mPaint.measureText("0000");

        final float dpToPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.f, getResources().getDisplayMetrics());

        if (mShowLineNumber) {
            int baseline = getBaseline();

            // Line number bar
            mPaint.setColor(0xffeeeeee);
            canvas.drawRect(getScrollX(), 0, barWidth + getScrollX(), getBottom(), mPaint);
            mPaint.setColor(0xbbbbbbbb);
            canvas.drawRect(barWidth + getScrollX(), 0, barWidth + getScrollX() + dpToPx, getBottom(), mPaint);

            mPaint.setColor(0xff888888);
            for (int i = 0; i < getLineCount(); i++) {
                canvas.drawText(String.format("%4d", (i + 1)), getScrollX(), baseline, mPaint);
                baseline += getLineHeight();
            }
        }
    }

    public void setHighlighter(IHighlighter IHighlighter) {
        mIHighlighter = IHighlighter;
    }

    public void setTextChangedFlag(boolean textChangedFlag) {
        mTextChangedFlag = textChangedFlag;
    }

    public boolean getTextChangedFlag() {
        return mShowLineNumber;
    }

    public interface ISelectionChangedListener {
        void onSelectionChanged(EditText editText, int selStart, int selEnd);
    }

    class BracketSpan extends BackgroundColorSpan {

        public BracketSpan() {
            super(getContext().getResources().getColor(R.color.colorBracketHighlightBg));
        }
    }
}
