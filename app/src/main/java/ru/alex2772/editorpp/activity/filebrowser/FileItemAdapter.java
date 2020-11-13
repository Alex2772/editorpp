package ru.alex2772.editorpp.activity.filebrowser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.util.Util;

public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.FileItemViewHolder> {
    private LayoutInflater mInflater;
    private ArrayList<IFileItem> mListFiles = new ArrayList<>();

    public FileItemAdapter(final Context context, final File currentDir, final ICallback callback) throws CouldNotListDirException {
        mInflater = LayoutInflater.from(context);

        if (currentDir.getParentFile() != null) {
            mListFiles.add(new IFileItem() {
                @Override
                public String getDisplayName() {
                    return "..";
                }

                @Override
                public Drawable getIcon() {
                    return context.getResources().getDrawable(R.drawable.ic_arrow_upward_green_24dp);
                }

                @Override
                public void onClick() {
                    callback.changeDir(currentDir.getParentFile());
                }
            });
        }
        File[] files = currentDir.listFiles();
        if (files == null && currentDir.getAbsolutePath().equals("/storage/emulated")) {
            // a workaround for (new File("/storage/emulated").listFiles()) == null
            files = new File[]{new File("/storage/emulated/0")};
        }
        if (files != null) {
            List<File> l = Arrays.asList(files);
            Collections.sort(l, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() == o2.isDirectory()) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return o1.isDirectory() ? -1 : 1;
                }
            });
            for (final File f : l) {
                mListFiles.add(new IFileItem() {
                    @Override
                    public String getDisplayName() {
                        return f.getName();
                    }

                    @Override
                    public Drawable getIcon() {
                        return f.isDirectory() ? context.getResources().getDrawable(R.drawable.ic_folder_black_24dp) : Util.getIconForFile(context, f.getName());
                    }

                    @Override
                    public void onClick() {
                        if (f.isDirectory()) {
                            callback.changeDir(f);
                        } else {
                            callback.fileSelected(f);
                        }
                    }
                });
            }
        } else {
            throw new CouldNotListDirException();
        }
    }

    @NonNull
    @Override
    public FileItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileItemViewHolder(mInflater.inflate(R.layout.item_icon_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileItemViewHolder holder, int position) {
        final IFileItem item = mListFiles.get(position);
        holder.getImageView().setImageDrawable(item.getIcon());
        holder.getTextView().setText(item.getDisplayName());
        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.onClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListFiles.size();
    }

    public interface ICallback {
        boolean changeDir(File newDir);

        void fileSelected(File targetFile);
    }

    public class FileItemViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private TextView mTextView;
        private ImageView mImageView;

        public FileItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.textView);
            mImageView = itemView.findViewById(R.id.imageView);
            mView = itemView;
        }

        public TextView getTextView() {
            return mTextView;
        }

        public ImageView getImageView() {
            return mImageView;
        }

        public View getView() {
            return mView;
        }
    }

    public class CouldNotListDirException extends Throwable {
    }
}
