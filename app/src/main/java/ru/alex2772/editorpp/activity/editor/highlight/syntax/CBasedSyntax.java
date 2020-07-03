package ru.alex2772.editorpp.activity.editor.highlight.syntax;

import java.util.List;

import ru.alex2772.editorpp.activity.editor.highlight.SyntaxHighlighter;
import ru.alex2772.editorpp.activity.editor.theme.ThemeAttr;

public abstract class CBasedSyntax extends RegexHighlighter {
    public CBasedSyntax() {
        addRule("[a-zA-Z0-9_]*\\s*(?=\\()", ThemeAttr.FUNCTION);

        addKeywords(new String[]{
                "int",
                "long",
                "short",
                "float",
                "double",
                "float",
                "if",
                "else",
                "do",
                "while",
                "for",
                "switch",
                "case",
                "default",
                "void",
                "char",
                "static",
                "const",
                "volatile",
                "return",
                "false",
                "goto",
                "true",
                "continue",
        });

        addRule(";", ThemeAttr.KEYWORD);
        addRule("-?\\b\\d+(\\.\\d+(d|f|l|D|F|L)?)?\\b", ThemeAttr.NUMBER); // dec
        addRule("-?\\b0b[0-1]+\\b", ThemeAttr.NUMBER); // bin
        addRule("-?\\b0x[0-f]+\\b", ThemeAttr.NUMBER); // hex

        addRule("\"[^\\\"]*\"", ThemeAttr.STRING); // string
        addRule("\\'[^']*\\'", ThemeAttr.STRING); // char

        addRule("\\/\\/.*", ThemeAttr.COMMENT);
        addRule("\\/\\*.*\\*\\/", ThemeAttr.COMMENT);
    }

    @Override
    public void highlight(List<SyntaxHighlighter.Span> result, CharSequence text) {
        super.highlight(result, text.toString().replaceAll("\\\\\"", "dd"));
    }
}
