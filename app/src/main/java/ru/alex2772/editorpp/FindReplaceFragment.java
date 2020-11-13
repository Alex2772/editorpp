package ru.alex2772.editorpp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.alex2772.editorpp.activity.editor.EditorActivity;
import ru.alex2772.editorpp.util.MTP;
import ru.alex2772.editorpp.util.Util;

public class FindReplaceFragment extends Fragment implements ValueAnimator.AnimatorUpdateListener, TextWatcher {

    private EditorActivity mEditor;
    private View mView;
    private boolean mHidden = false;
    private View mAdvancedOptionsWrap;
    private View mFindCardWrap;
    private View mFindCardInner;
    private float mFragmentHeight;
    private View mTextToFindLabel;
    private EditText mFindQueryEdit;
    private final ArrayList<Integer> mFindEntries = new ArrayList<>();

    public FindReplaceFragment() {
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        clearAllSpans();
        updateSearchOccurrences(false);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private enum Direction {
        UP,
        DOWN
    }

    private void hideBackground() {
        if (mHidden)
            return;
        mHidden = true;
        mFragmentHeight = mEditor.getNavigationView().getMeasuredHeightAndState();

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.addUpdateListener(this);
        anim.start();
    }


    private void showBackground() {
        if (!mHidden)
            return;
        mHidden = false;
        mFragmentHeight = mEditor.getNavigationView().getMeasuredHeightAndState();

        ValueAnimator anim = ValueAnimator.ofFloat(1, 0);
        anim.addUpdateListener(this);
        anim.start();
    }


    interface FindSpan {}

    private class FindHighlightSpan extends BackgroundColorSpan implements FindSpan {

        public FindHighlightSpan() {
            super(getResources().getColor(R.color.colorPrimaryDarkFind));
        }
    }

    private class FindBorderHighlightSpan extends UnderlineSpan implements FindSpan {

        public FindBorderHighlightSpan() {
        }
    }

    private void clearAllSpans() {
        for (FindSpan s : mEditor.getText().getSpans(0, mEditor.getEditor().getEditableText().length(), FindSpan.class)) {
            mEditor.getText().removeSpan(s);
        }
    }

    private boolean nextOccurrence(Direction direction) {
        for (FindSpan s : mEditor.getText().getSpans(0, mEditor.getEditor().getEditableText().length(), FindBorderHighlightSpan.class)) {
            mEditor.getText().removeSpan(s);
        }
        int start = mEditor.getEditor().getSelectionStart();


        synchronized (mFindEntries) {
            if (mFindEntries.isEmpty())
                return false;

            int nearestPos = -1;

            switch (direction) {
                case DOWN:
                    for (int i : mFindEntries) {
                        int delta = i - start;
                        if (delta > 0) {
                            if (nearestPos == -1) {
                                nearestPos = i;
                            } else if ((nearestPos - start) > delta) {
                                nearestPos = i;
                            }
                        }
                    }
                    break;

                case UP:
                    for (int i : mFindEntries) {
                        int delta = start - i;
                        if (delta > 0) {
                            if (nearestPos == -1) {
                                nearestPos = i;
                            } else if ((start - nearestPos) > delta) {
                                nearestPos = i;
                            }
                        }
                    }
                    break;
            }
            if (nearestPos == -1) {
                return false;
            }
            mEditor.getEditor().setSelection(nearestPos, nearestPos + mFindQueryEdit.getText().length());
            mEditor.getText().setSpan(new FindBorderHighlightSpan(), nearestPos, nearestPos + mFindQueryEdit.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mEditor = (EditorActivity)getActivity();
        mView = inflater.inflate(R.layout.fragment_find_replace, container, false);

        mTextToFindLabel = mView.findViewById(R.id.text_to_find_label);
        mFindCardWrap = mView.findViewById(R.id.find_card_wrap);
        mFindCardInner = mView.findViewById(R.id.find_card_inner);
        mAdvancedOptionsWrap = mView.findViewById(R.id.advanced_options_wrap);

        mFindQueryEdit = mView.findViewById(R.id.find_query);
        mFindQueryEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearAllSpans();
                if (charSequence.length() > 0)
                    hideBackground();
                else {
                    showBackground();
                    return;
                }
                updateSearchOccurrences(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mFindCardWrap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showBackground();
                return false;
            }
        });

        mView.findViewById(R.id.button_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextOccurrence(Direction.DOWN);
                mEditor.getEditor().requestFocus();
            }
        });
        mView.findViewById(R.id.button_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextOccurrence(Direction.UP);
                mEditor.getEditor().requestFocus();
            }
        });

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEditor.getEditor().addTextChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearAllSpans();
        mEditor.getEditor().removeTextChangedListener(this);
    }

    private void updateSearchOccurrences(final boolean select) {
        final String text = mEditor.getText().toString();
        final String query = mFindQueryEdit.getText().toString();
        if (query.isEmpty())
            return;
        MTP.schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (mFindEntries) {
                    mFindEntries.clear();
                    int index = 0;

                    for (; ; ) {
                        index = text.indexOf(query, index + 1);
                        if (index >= 0) {
                            mFindEntries.add(index);
                        } else {
                            break;
                        }
                    }

                    if (select) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int indexCopy : mFindEntries) {
                                    mEditor.getText().setSpan(new FindHighlightSpan(),
                                                              indexCopy,
                                                              indexCopy + query.length(),
                                                              SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                nextOccurrence(Direction.DOWN);
                            }
                        });
                    }
                }
            }
        });
    }


    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float val = (float) valueAnimator.getAnimatedValue();
        float invVal = 1.f - val;

        // scrim transparency
        mEditor.setToolDrawerScrimTransparency((int) (invVal * 255));

        // text to find label height
        mTextToFindLabel.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Util.setHeight(mTextToFindLabel, (int) (invVal * mTextToFindLabel.getMeasuredHeight()));

        // advanced options height
        Util.setHeight(mAdvancedOptionsWrap, (int)(invVal * mFragmentHeight));
        mFindCardWrap.requestLayout();

        // shadow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final CardView findQueryWrap = mView.findViewById(R.id.find_card_wrap);
            findQueryWrap.setElevation(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    val * 6,
                    getResources().getDisplayMetrics()));
            findQueryWrap.invalidate();
        }

        // margin for shadow
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFindCardWrap.getLayoutParams();
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                    8 * val,
                                                     getResources().getDisplayMetrics());
        int top    = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                    4 * val,
                                                     getResources().getDisplayMetrics());
        lp.setMargins(margin, 0, margin, margin);

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                8 * invVal,
                getResources().getDisplayMetrics());
        mFindCardInner.setPadding(padding, padding, padding, padding);
    }
}