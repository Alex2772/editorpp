package ru.alex2772.editorpp.activity.main;

import androidx.annotation.StringRes;

import ru.alex2772.editorpp.model.IItemType;

class TitledDividerItem implements IItemType {
    @StringRes
    private int mString;

    public TitledDividerItem(@StringRes int string) {
        mString = string;
    }

    public @StringRes int getString() {
        return mString;
    }

    @Override
    public int getItemType() {
        return 1;
    }
}
