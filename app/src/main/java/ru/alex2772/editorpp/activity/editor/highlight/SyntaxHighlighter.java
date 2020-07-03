package ru.alex2772.editorpp.activity.editor.highlight;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.LinkedList;

import ru.alex2772.editorpp.activity.editor.highlight.syntax.IHighlighter;
import ru.alex2772.editorpp.activity.editor.theme.IEditorTheme;
import ru.alex2772.editorpp.activity.editor.theme.ThemeAttr;
import ru.alex2772.editorpp.util.MTP;

public class SyntaxHighlighter {
    private static String TAG = SyntaxHighlighter.class.getSimpleName();

    public static void highlight(final Activity c, final IEditorTheme theme, final IHighlighter highlighter, final Editable text, final int lineBegin, final int lineEnd) {

        if (PreferenceManager.getDefaultSharedPreferences(c).getBoolean("highlight_syntax", true))
            MTP.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        CharSequence sub = text.subSequence(lineBegin, lineEnd + 1);
                        final LinkedList<SyntaxHighlighter.Span> spans = new LinkedList<>();
                        {
                            long s = System.currentTimeMillis();
                            highlighter.highlight(spans, sub);
                            Log.i(TAG, "Highlighted " + (lineEnd - lineBegin) + " symbols in " + (System.currentTimeMillis() - s) + "ms");
                        }

                        final LinkedList<Object> aboutToRemove = new LinkedList<Object>();

                        c.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                aboutToRemove.addAll(Arrays.asList(text.getSpans(lineBegin, lineEnd, ForegroundColorSpan.class)));
                                aboutToRemove.addAll(Arrays.asList(text.getSpans(lineBegin, lineEnd, StyleSpan.class)));
                            }
                        });

                        for (final Span s : spans) {
                            c.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int offset = 0;
                                    while (lineBegin < (lineEnd + offset)) {
                                        try {
                                            text.setSpan(new ForegroundColorSpan(theme.getColor(s.mAttr)), s.mBegin + lineBegin, s.mEnd + lineBegin + offset, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            break;
                                        } catch (IndexOutOfBoundsException ignored) {
                                            offset -= 1;
                                        }
                                    }
                                    int style = theme.getStyle(s.mAttr);
                                    for (Object x : text.getSpans(s.mBegin + lineBegin, s.mEnd + lineBegin, StyleSpan.class)) {
                                        text.removeSpan(x);
                                    }
                                    if (style != Typeface.NORMAL)
                                        text.setSpan(new StyleSpan(style), s.mBegin + lineBegin, s.mEnd + lineBegin + offset, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

                                }
                            });
                            throttle(10);
                        }
                        c.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (Object o : aboutToRemove) {
                                    text.removeSpan(o);
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to highlight", e);
                    }
                }
            });
        else {
            for (Object x : text.getSpans(lineBegin, lineEnd, ForegroundColorSpan.class)) {
                text.removeSpan(x);
            }
            for (Object x : text.getSpans(lineBegin, lineEnd, StyleSpan.class)) {
                text.removeSpan(x);
            }
        }
    }

    private static void throttle(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class Span {
        public ThemeAttr mAttr;
        public int mBegin;
        public int mEnd;

        public Span(ThemeAttr attr, int begin, int end) {
            mAttr = attr;
            mBegin = begin;
            mEnd = end;
        }

    }
}
