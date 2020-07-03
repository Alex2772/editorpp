package ru.alex2772.editorpp.activity.filebrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import androidx.preference.PreferenceManager;

import java.io.File;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.util.Util;

public class OpenFileBrowserActivity extends FileBrowserActivity {

    @Override
    public void fileSelected(final File targetFile) {
        if ((targetFile.getParentFile() != null && !targetFile.getParentFile().canRead()) || (targetFile.exists() && !targetFile.canRead())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.file_not_readable)
                    .setMessage(R.string.choose_another_location)
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
            return;
        }
        if (!targetFile.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.file_dont_exist)
                    .setMessage(R.string.choose_another_file)
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
        } else {

            if (targetFile.length() > 4096 && !PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ignore_file_size", false)) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.file_too_big)
                        .setMessage(getResources().getString(R.string.file_too_big_desc, Util.prettySize(targetFile.length())))
                        .setPositiveButton(R.string.ignore_and_open, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                returnResult(targetFile);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            } else {
                returnResult(targetFile);
            }
        }
    }

    private void returnResult(File targetFile) {
        Intent data = new Intent();
        data.setData(Uri.fromFile(targetFile));
        setResult(0, data);
        finish();
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_folder_open_black_24dp;
    }

    @Override
    protected int getTitleText() {
        return R.string.open_file;
    }
}
