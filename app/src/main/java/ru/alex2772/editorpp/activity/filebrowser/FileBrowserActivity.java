package ru.alex2772.editorpp.activity.filebrowser;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.util.AppBarStateChangeListener;

public abstract class FileBrowserActivity extends AppCompatActivity implements FileItemAdapter.ICallback {

    private RecyclerView mRecycler;
    private File mCurrentDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(getIcon());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getEditText().getText().toString().isEmpty()) {
                    new AlertDialog.Builder(FileBrowserActivity.this)
                            .setMessage(R.string.type_filename)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                } else {
                    fileSelected(new File(mCurrentDir, getEditText().getText().toString()));
                }
            }
        });
        mRecycler = findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        changeDir(Environment.getDataDirectory());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        toolbar.setTitle(getTitleText());

        ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.COLLAPSED) {
                    getEditText().clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(getEditText().getWindowToken(), 0);
                    }
                }
            }
        });

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getString("file") != null) {
                File f = new File(getIntent().getExtras().getString("file"));
                changeDir(f.getParentFile());
                getEditText().setText(f.getName());
            }
        }
    }

    protected abstract int getIcon();

    protected abstract int getTitleText();

    protected EditText getEditText() {
        return findViewById(R.id.editText2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
            finish();
        else
            changeDir(Environment.getExternalStorageDirectory());

    }

    @Override
    public void changeDir(File newDir) {
        if (newDir == null)
            return;
        mCurrentDir = newDir;
        mRecycler.setAdapter(new FileItemAdapter(this, newDir, this));
    }
}
