package ru.alex2772.editorpp.activity.main;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.LinkedList;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.activity.AboutActivity;
import ru.alex2772.editorpp.activity.editor.EditorActivity;
import ru.alex2772.editorpp.activity.filebrowser.OpenFileBrowserActivity;
import ru.alex2772.editorpp.activity.settings.SettingsActivity;
import ru.alex2772.editorpp.database.DBHelper;
import ru.alex2772.editorpp.model.FileListModel;
import ru.alex2772.editorpp.util.ItemSwipeDeleteCallback;
import ru.alex2772.editorpp.util.MTP;
import ru.alex2772.editorpp.util.Util;

public class ScrollingActivity extends AppCompatActivity {

    private RecyclerView mRecyler;
    private FileListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.action_new_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ScrollingActivity.this, EditorActivity.class);
                ActivityCompat.startActivity(ScrollingActivity.this, i,
                        ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0,
                                view.getWidth(), view.getHeight()).toBundle());
            }
        });
        findViewById(R.id.open_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ScrollingActivity.this, OpenFileBrowserActivity.class);
                ActivityCompat.startActivityForResult(ScrollingActivity.this, i, 0,
                        ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0,
                                view.getWidth(), view.getHeight()).toBundle());
            }
        });

        mRecyler = findViewById(R.id.recycler);
        mRecyler.setLayoutManager(new LinearLayoutManager(ScrollingActivity.this,
                LinearLayoutManager.VERTICAL, false));
    }

    private void updateList() {

        MTP.schedule(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = new DBHelper(ScrollingActivity.this).getReadableDatabase();
                Cursor c = db.query("files", new String[]{"id", "path", "last_open"}, null, null, null, null, "last_open DESC");

                final LinkedList<FileListModel> items = new LinkedList<>();
                while (c.moveToNext()) {
                    // check whether file exists or not
                    if (new File(c.getString(1)).isFile()) {
                        items.add(new FileListModel(c.getLong(0), c.getString(1), c.getLong(2)));
                    }
                }
                c.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter == null) {
                            mRecyler.setAdapter(mAdapter = new FileListAdapter(ScrollingActivity.this, mRecyler, items));
                            new ItemTouchHelper(new ItemSwipeDeleteCallback(mAdapter)).attachToRecyclerView(mRecyler);
                        } else {
                            mAdapter.setListFiles(items);
                        }
                        findViewById(R.id.list_empty).setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }
        });
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            Util.startEditor(this, data.getData().getPath());
        } catch (NullPointerException ignored) {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
