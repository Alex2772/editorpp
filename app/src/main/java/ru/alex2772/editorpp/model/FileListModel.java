package ru.alex2772.editorpp.model;

public class FileListModel implements IItemType {
    private long mId;
    private String mPath;
    private long mLastOpened;

    public FileListModel(long id, String path, long lastOpened) {
        mId = id;
        mPath = path;
        mLastOpened = lastOpened;
    }

    public long getId() {
        return mId;
    }

    public String getPath() {
        return mPath;
    }

    public long getLastOpened() {
        return mLastOpened;
    }

    @Override
    public int getItemType() {
        return 0;
    }
}
