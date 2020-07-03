package ru.alex2772.editorpp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import ru.alex2772.editorpp.model.FileTabModel;
import ru.alex2772.editorpp.util.MTP;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context) {
        super(context, "Data.db", null, 1);
    }

    public static void updateFile(final Context c, final FileTabModel file) {

        MTP.schedule(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = new DBHelper(c).getWritableDatabase();

                ContentValues v = new ContentValues();
                v.put("last_open", System.currentTimeMillis() / 1000L);
                v.put("syntax", file.getSyntax());

                int a = db.update("files", v, "path = ?", new String[]{file.getFilePath()});
                if (a == 0) {
                    v.put("path", file.getFilePath());
                    db.insert("files", null, v);
                }
            }
        });
    }

    public static void getFileData(final Context c, final FileTabModel file, final Runnable onOkCallback) {
        MTP.schedule(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = new DBHelper(c).getReadableDatabase();

                Cursor c = db.query("files", new String[]{"syntax"}, "path = ?", new String[]{file.getFilePath()}, null, null, null);
                if (c.moveToNext()) {
                    file.setSyntax(c.getInt(c.getColumnIndex("syntax")));
                    onOkCallback.run();
                }
                c.close();
            }
        });
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE files (id INTEGER PRIMARY KEY AUTOINCREMENT, path VARCHAR(256), last_open BIGINT, syntax INT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
