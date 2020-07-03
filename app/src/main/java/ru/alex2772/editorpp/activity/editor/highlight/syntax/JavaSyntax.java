package ru.alex2772.editorpp.activity.editor.highlight.syntax;

import ru.alex2772.editorpp.activity.editor.theme.ThemeAttr;

public class JavaSyntax extends CBasedSyntax {
    public JavaSyntax() {
        addKeywords(new String[] {
                "abstract",
                "assert",
                "boolean",
                "break",
                "byte",
                "catch",
                "class",
                "enum",
                "exports",
                "extends",
                "final",
                "finally",
                "implements",
                "import",
                "instanceof",
                "interface",
                "module",
                "native",
                "new",
                "package",
                "private",
                "protected",
                "public",
                "record",
                "requires",
                "strictfp",
                "switch",
                "synchronized",
                "this",
                "throw",
                "throws",
                "transient",
                "yield",
                "null",
        });

        addRule("(@\\w*)", ThemeAttr.ANNOTATION);
    }

    @Override
    public String getName() {
        return "Java";
    }
}
