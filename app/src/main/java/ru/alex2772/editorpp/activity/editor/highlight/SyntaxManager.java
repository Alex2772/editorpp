package ru.alex2772.editorpp.activity.editor.highlight;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.activity.editor.highlight.syntax.CSyntax;
import ru.alex2772.editorpp.activity.editor.highlight.syntax.CppSyntax;
import ru.alex2772.editorpp.activity.editor.highlight.syntax.IHighlighter;
import ru.alex2772.editorpp.activity.editor.highlight.syntax.JavaSyntax;
import ru.alex2772.editorpp.activity.editor.highlight.syntax.PythonSyntax;

public class SyntaxManager {
    private List<IHighlighter> mSyntaxes = new ArrayList<>();
    public SyntaxManager(final Context context) {
        mSyntaxes.add(new IHighlighter() {
            @Override
            public void highlight(List<SyntaxHighlighter.Span> result, CharSequence text) {

            }

            @Override
            public String getName() {
                return context.getResources().getString(R.string.none);
            }
        });
        mSyntaxes.add(new CSyntax());
        mSyntaxes.add(new CppSyntax());
        mSyntaxes.add(new JavaSyntax());
        mSyntaxes.add(new PythonSyntax());
    }

    public List<IHighlighter> getSyntaxes() {
        return mSyntaxes;
    }

    public static int guessSyntax(String filePath) {
        try {
            String ext = filePath.substring(filePath.lastIndexOf('.') + 1);
            switch (ext) {
                case "c":
                    return 1;
                case "h":
                case "cxx":
                case "cc":
                case "cpp":
                    return 2;
                case "java":
                    return 3;
                case "py":
                    return 4;
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
