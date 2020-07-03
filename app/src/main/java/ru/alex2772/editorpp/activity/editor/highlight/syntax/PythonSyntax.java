package ru.alex2772.editorpp.activity.editor.highlight.syntax;

import ru.alex2772.editorpp.activity.editor.theme.ThemeAttr;

public class PythonSyntax extends RegexHighlighter {
    public PythonSyntax() {
        addRule("[a-zA-Z0-9_]*\\s*(?=\\()", ThemeAttr.FUNCTION);

        addKeywords(new String[]{
                "and",
                "as",
                "assert",
                "break",
                "class",
                "continue",
                "def",
                "del",
                "elif",
                "else",
                "except",
                "False",
                "finally",
                "for",
                "from",
                "global",
                "if",
                "import",
                "in",
                "is",
                "lambda",
                "None",
                "nonlocal",
                "not",
                "or",
                "pass",
                "raise",
                "return",
                "True",
                "try",
                "while",
                "with",
                "yield",
        });

        addRule("(@\\w*)", ThemeAttr.ANNOTATION);

        addRule(";", ThemeAttr.KEYWORD);
        addRule("-?\\b\\d+(\\.\\d+(d|f|l|D|F|L)?)?\\b", ThemeAttr.NUMBER); // dec
        addRule("-?\\b0b[0-1]+\\b", ThemeAttr.NUMBER); // bin
        addRule("-?\\b0x[0-f]+\\b", ThemeAttr.NUMBER); // hex

        addRule("\"[^\\\"]*\"", ThemeAttr.STRING); // string
        addRule("'[^']*'", ThemeAttr.STRING); // string
        addRule("'''.*'''", ThemeAttr.STRING);
        addRule("\"\"\".*\"\"\"", ThemeAttr.STRING);

        addRule("(#.*)", ThemeAttr.COMMENT);
    }

    @Override
    public String getName() {
        return "Python";
    }
}
