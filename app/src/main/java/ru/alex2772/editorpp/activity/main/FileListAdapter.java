package ru.alex2772.editorpp.activity.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.database.DBHelper;
import ru.alex2772.editorpp.drawable.FileIconDrawable;
import ru.alex2772.editorpp.model.FileListModel;
import ru.alex2772.editorpp.model.IItemType;
import ru.alex2772.editorpp.util.ItemSwipeDeleteCallback;
import ru.alex2772.editorpp.util.MTP;
import ru.alex2772.editorpp.util.Util;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileListViewHolder> implements ItemSwipeDeleteCallback.IItemDeletable {
    private final RecyclerView mRecyclerView;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<IItemType> mItems = new LinkedList<>();

    public FileListAdapter(Context context, RecyclerView recyclerView, List<FileListModel> listFiles) {
        mContext = context;
        mRecyclerView  = recyclerView;
        mInflater = LayoutInflater.from(context);
        setListFiles(listFiles);
    }

    private static int getTitleForDiff(long diff) {
        int title = R.string.recently;

        if (diff > 60L * 60L * 24L * 30L * 12L) // year
        {
            title = R.string.older_files;
        } else if (diff > 60L * 60L * 24L * 30L) // month
        {
            title = R.string.last_year;
        } else if (diff > 60L * 60L * 24L * 7L) // week
        {
            title = R.string.last_month;
        } else if (diff > 60L * 60L * 48L) // day
        {
            title = R.string.last_week;
        } else if (diff > 60L * 60L * 16L) // hour
        {
            title = R.string.yesterday;
        } else if (diff > 60L * 60L) // hour
        {
            title = R.string.today;
        }
        return title;
    }

    public void setListFiles(List<FileListModel> listFiles) {
        mItems.clear();
        int prevStr = 0;
        for (FileListModel m : listFiles) {
            int currentStr = getTitleForDiff(System.currentTimeMillis() / 1000L - m.getLastOpened());
            if (prevStr == 0) {
                // add initial title
                prevStr = currentStr;
                mItems.add(new TitledDividerItem(prevStr));
            } else {
                if (prevStr == currentStr) {
                    mItems.add(new DividerItem());
                } else {
                    mItems.add(new TitledDividerItem(currentStr));
                }
            }

            mItems.add(m);
            prevStr = currentStr;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getItemType();
    }

    @NonNull
    @Override
    public FileListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new FileListEntryViewHolder(mInflater.inflate(R.layout.item_file, parent, false));
            case 1:
                return new FileListTitledDividerViewHolder(mInflater.inflate(R.layout.item_titled_divider, parent, false));
            case 2:
                return new FileListDividerViewHolder(mInflater.inflate(R.layout.item_divider, parent, false));

        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onBindViewHolder(@NonNull FileListViewHolder h, int position) {
        final IItemType item = mItems.get(position);
        switch (item.getItemType()) {
            case 0: {
                FileListEntryViewHolder holder = (FileListEntryViewHolder) h;
                final FileListModel currentItem = (FileListModel) item;
                holder.getImageView().setImageDrawable(Util.getIconForFile(mContext, currentItem.getPath()));
                File f = new File(currentItem.getPath());
                holder.getTextView().setText(f.getName());
                holder.getTextView2().setText(currentItem.getPath());
                //holder.getTextView2().setText(new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z").format(new Date(currentItem.getLastOpened() * 1000L)));
                holder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.startEditor(getContext(), currentItem.getPath());
                    }
                });
                break;
            }
            case 1: {
                FileListTitledDividerViewHolder holder = (FileListTitledDividerViewHolder) h;
                holder.getTitle().setText(((TitledDividerItem)item).getString());
                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onItemDelete(int index) {
        final FileListModel item = (FileListModel)mItems.get(index);
        MTP.schedule(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = new DBHelper(getContext()).getWritableDatabase();
                db.delete("files", "id = ?", new String[] {String.valueOf(item.getId())});
            }
        });
        mItems.remove(index);
        notifyItemRemoved(index);
        try {
            mItems.remove(index);
            notifyItemRemoved(index);
        } catch (IndexOutOfBoundsException ignored){

            try {
                mItems.remove(index - 1);
                notifyItemRemoved(index - 1);
            } catch (IndexOutOfBoundsException ignored2){}
        }
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    public class FileListViewHolder extends RecyclerView.ViewHolder {
        public FileListViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
    public class FileListDividerViewHolder extends FileListViewHolder implements ItemSwipeDeleteCallback.IItemDeletableShield {
        public FileListDividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
    public class FileListEntryViewHolder extends FileListViewHolder {
        private final View mView;
        private TextView mTextView;
        private TextView mTextView2;
        private ImageView mImageView;

        public FileListEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.textView);
            mTextView2 = itemView.findViewById(R.id.textView2);
            mImageView = itemView.findViewById(R.id.imageView);
            mView = itemView;
        }

        public TextView getTextView() {
            return mTextView;
        }
        public TextView getTextView2() {
            return mTextView2;
        }
        public ImageView getImageView() {
            return mImageView;
        }
        public View getView() {
            return mView;
        }
    }
    public class FileListTitledDividerViewHolder extends FileListViewHolder implements ItemSwipeDeleteCallback.IItemDeletableShield {
        private TextView mTitle;

        public FileListTitledDividerViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
        }

        public TextView getTitle() {
            return mTitle;
        }
    }
}
