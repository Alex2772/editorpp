package ru.alex2772.editorpp.activity.editor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.model.FileTabModel;

public class FileTabAdapter extends RecyclerView.Adapter<FileTabAdapter.FileTabViewHolder> {

    private final LayoutInflater mInflater;
    private final EditorActivity mEditor;
    private List<FileTabModel> mTabs;

    public FileTabAdapter(EditorActivity c, List<FileTabModel> tabs) {
        mTabs = tabs;
        mEditor = c;
        mInflater = LayoutInflater.from(c);
    }

    @NonNull
    @Override
    public FileTabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileTabViewHolder(mInflater.inflate(R.layout.item_tab, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileTabViewHolder holder, final int position) {
        FileTabModel item = mTabs.get(position);
        holder.mRoot.setBackgroundResource(item.isCurrent() ? R.drawable.tab_selected : R.drawable.tab);
        holder.mTitle.setTextColor(item.isCurrent() ? 0xcc000000 : 0xccffffff);
        holder.mTitle.setText(item.getTitle());
        holder.mClose.setVisibility(item.isCurrent() ? View.VISIBLE : View.GONE);

        holder.mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setCurrentTab(position);
            }
        });
        holder.mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.closeFile();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTabs.size();
    }

    class FileTabViewHolder extends RecyclerView.ViewHolder {

        private View mRoot;
        private TextView mTitle;
        private Button mClose;

        public FileTabViewHolder(@NonNull View itemView) {
            super(itemView);
            mRoot = itemView;
            mTitle = mRoot.findViewById(R.id.title);
            mClose = mRoot.findViewById(R.id.close);
        }
    }
}
