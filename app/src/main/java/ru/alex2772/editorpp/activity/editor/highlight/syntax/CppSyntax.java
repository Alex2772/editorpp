package ru.alex2772.editorpp.activity.editor.highlight.syntax;

public class CppSyntax extends CSyntax {
    public CppSyntax() {
        addKeywords(new String[]{
                "class",
                "virtual",
                "override",
                "namespace",
                "enum",
                "using",
                "public",
                "protected",
                "private",
                "this",
                "throw",
                "delete",
                "delete\\[\\]",
                "new",
                "static_cast",
                "dynamic_cast",
                "reinterpret_cast",
                "const_cast",
                "nullptr",
                "try",
                "catch",
                "finally"
        });
    }

    @Override
    public String getName() {
        return "C++";
    }
}
