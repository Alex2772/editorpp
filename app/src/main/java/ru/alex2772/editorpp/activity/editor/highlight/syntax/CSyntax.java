package ru.alex2772.editorpp.activity.editor.highlight.syntax;

import ru.alex2772.editorpp.activity.editor.theme.ThemeAttr;

public class CSyntax extends CBasedSyntax {
    public CSyntax() {
        addKeywords(new String[]{
                "struct",
                "bool",
                "typedef",
                "extern",
                "unsigned",
                "signed",
                "extern",
        });

        addRule("#.*", ThemeAttr.DEFINITION);
    }

    @Override
    public String getName() {
        return "C";
    }
}
