package ru.alex2772.editorpp.activity.filebrowser;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
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
                getEditText().setText(f.getName());
                initialOpen(f.getParentFile());
                return;
            }
        }
        initialOpen(Environment.getDataDirectory());
    }

    private void initialOpen(File directory) {
        try {
            changeDirUnsafe(directory);
        } catch (FileItemAdapter.CouldNotListDirException e) {
            Log.e("Editor", "User has not gained access to "
                    + Environment.getDataDirectory().toString(), e);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                new AlertDialog.Builder(FileBrowserActivity.this)
                        .setTitle(R.string.could_not_open_dir)
                        .setMessage(getResources().getString(R.string.could_not_open_permission_denied_dir) + "\n" + Environment.getDataDirectory().toString())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                finish();
                            }
                        })
                        .show();
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
        switch (requestCode) {
            case 1: // initial directory
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        // try again
                        changeDirUnsafe(Environment.getDataDirectory());
                    } catch (FileItemAdapter.CouldNotListDirException e) {
                        Log.e("Editor", "Could not access initial directory but rights are sufficient", e);

                        // try to access the common dir.
                        changeDir(new File("/storage/emulated/0/"));
                    }
                } else {
                    // we should explain to user why do we need for this permission
                    new AlertDialog.Builder(FileBrowserActivity.this)
                            .setTitle(R.string.android_permission_manager)
                            .setMessage(R.string.android_permission_manager_desc)
                            .setPositiveButton(R.string.im_sure, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.try_again, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, 0);
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                break;

            case 0: // other directory
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                finish();
            else
                changeDir(Environment.getExternalStorageDirectory());
        }

    }

    /**
     * Tries to open directory. If failed, shows error message dialog and returns false
     * @param newDir new directory
     * @return true, if opened successly
     */
    @Override
    public boolean changeDir(File newDir) {
        if (newDir == null)
            return false;
        mCurrentDir = newDir;
        try {
            changeDirUnsafe(newDir);
        } catch (FileItemAdapter.CouldNotListDirException e) {

            new AlertDialog.Builder(FileBrowserActivity.this)
                    .setTitle(R.string.could_not_open_dir)
                    .setMessage(R.string.could_not_open_permission_denied_dir)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }
        return false;
    }

    private void changeDirUnsafe(File newDir) throws FileItemAdapter.CouldNotListDirException {
        mRecycler.setAdapter(new FileItemAdapter(this, newDir, this));
    }
}
