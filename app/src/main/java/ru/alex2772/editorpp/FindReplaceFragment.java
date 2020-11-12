package ru.alex2772.editorpp;

import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import ru.alex2772.editorpp.activity.editor.EditorActivity;
import ru.alex2772.editorpp.util.Util;

public class FindReplaceFragment extends Fragment implements ValueAnimator.AnimatorUpdateListener {

    private EditorActivity mEditor;
    private View mView;
    private boolean mHidden = false;
    private View mAdvancedOptionsWrap;
    private View mFindCardWrap;
    private View mFindCardInner;
    private float mFragmentHeight;
    private View mTextToFindLabel;

    public FindReplaceFragment() {
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

        EditText findQueryEdit = mView.findViewById(R.id.find_query);
        findQueryEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                    hideBackground();
                else
                    showBackground();
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
        return mView;
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