package ru.alex2772.editorpp.activity.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.activity.editor.theme.ThemeManager;

class ThemesAdapter extends RecyclerView.Adapter<ThemesAdapter.Holder> {
    private final ThemeManager mThemeManager;
    private final LayoutInflater mInflater;

    private RadioButton mLastRB = null;

    public ThemesAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mThemeManager  = new ThemeManager(context);
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(mInflater.inflate(R.layout.item_theme, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int position) {
        final RadioButton rb = holder.itemView.findViewById(R.id.radioButton);
        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastRB != null) {
                    mLastRB.setChecked(false);
                }
                mLastRB = rb;
                mLastRB.setChecked(true);

                mThemeManager.setTheme(position);
            }
        });
        if (position == mThemeManager.getThemeId()) {
            rb.setChecked(true);
            mLastRB = rb;
        }
        rb.setText(mThemeManager.getThemes().get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mThemeManager.getThemes().size();
    }

    class Holder extends RecyclerView.ViewHolder {

        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
