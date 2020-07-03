package ru.alex2772.editorpp.activity.editor.highlight.syntax;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.alex2772.editorpp.activity.editor.highlight.SyntaxHighlighter;
import ru.alex2772.editorpp.activity.editor.theme.ThemeAttr;

public abstract class RegexHighlighter implements IHighlighter {
    private LinkedList<Rule> mRules = new LinkedList<>();

    protected void addRule(String rule, ThemeAttr color) {
        mRules.add(new Rule(Pattern.compile(rule), color));
    }

    @Override
    public void highlight(List<SyntaxHighlighter.Span> result, CharSequence text) {
        for (Rule r : mRules) {
            final Matcher m = r.mRegex.matcher(text);
            while (m.find()) {
                if (m.start() != m.end())
                    result.add(new SyntaxHighlighter.Span(r.mColor, m.start(), m.end()));
            }
        }
    }

    protected void addKeyword(String keyword) {
        addRule("\\b()\\b", ThemeAttr.KEYWORD);
    }

    protected void addKeywords(String[] keywords) {
        StringBuilder sb = new StringBuilder();
        for (String s : keywords) {
            if (!sb.toString().isEmpty()) {
                sb.append('|');
            }
            sb.append(s);
        }

        addRule("\\b(" + sb + ")\\b", ThemeAttr.KEYWORD);
    }

    private class Rule {
        Pattern mRegex;
        ThemeAttr mColor;

        Rule(Pattern regex, ThemeAttr color) {
            mRegex = regex;
            mColor = color;
        }
    }
}
