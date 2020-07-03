package ru.alex2772.editorpp.activity.editor.highlight.syntax;

import java.util.List;

import ru.alex2772.editorpp.activity.editor.highlight.SyntaxHighlighter;

public interface IHighlighter {
    void highlight(List<SyntaxHighlighter.Span> result, CharSequence text);
    String getName();
}
