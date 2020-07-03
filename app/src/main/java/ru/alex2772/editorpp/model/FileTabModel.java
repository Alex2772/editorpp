package ru.alex2772.editorpp.model;

import android.text.Editable;

import java.io.File;

import ru.alex2772.editorpp.activity.editor.SaveState;
import ru.alex2772.editorpp.activity.editor.highlight.SyntaxManager;

public class FileTabModel {
    private String mFilePath;
    private String mTitle;
    private SaveState mSaveState = SaveState.SAVED;
    private Editable mText;
    private boolean mCurrent;
    private int mSelectionStart;
    private int mSelectionEnd;
    private int mScrollX;
    private int mScrollY;
    private int mSyntax;


    public FileTabModel(String filePath, String title) {
        mFilePath = filePath;
        mTitle = title;
        mText = Editable.Factory.getInstance().newEditable("");
        mSyntax = SyntaxManager.guessSyntax(filePath);
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
        setTitle(new File(filePath).getName());
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public SaveState getSaveState() {
        return mSaveState;
    }

    public void setSaveState(SaveState saveState) {
        mSaveState = saveState;
    }

    public Editable getText() {
        return mText;
    }

    public void setText(Editable text) {
        mText = text;
    }

    public boolean isCurrent() {
        return mCurrent;
    }

    public void setCurrent(boolean current) {
        mCurrent = current;
    }

    public int getSelectionStart() {
        return mSelectionStart;
    }

    public void setSelectionStart(int selectionStart) {
        mSelectionStart = selectionStart;
    }

    public int getSelectionEnd() {
        return mSelectionEnd;
    }

    public void setSelectionEnd(int selectionEnd) {
        mSelectionEnd = selectionEnd;
    }

    public int getScrollX() {
        return mScrollX;
    }

    public void setScrollX(int scrollX) {
        mScrollX = scrollX;
    }

    public int getScrollY() {
        return mScrollY;
    }

    public void setScrollY(int scrollY) {
        mScrollY = scrollY;
    }

    public int getSyntax() {
        return mSyntax;
    }

    public void setSyntax(int syntax) {
        mSyntax = syntax;

    }
}
