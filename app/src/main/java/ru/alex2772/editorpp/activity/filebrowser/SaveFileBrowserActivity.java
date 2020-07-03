package ru.alex2772.editorpp.activity.filebrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

import ru.alex2772.editorpp.R;

public class SaveFileBrowserActivity extends FileBrowserActivity {

    @Override
    public void fileSelected(final File targetFile) {
        if ((targetFile.getParentFile() != null && !targetFile.getParentFile().canWrite()) || (targetFile.exists() && !targetFile.canWrite())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.file_not_writable)
                    .setMessage(R.string.choose_another_location)
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
            return;
        }
        if (targetFile.exists() && !getIntent().getExtras().getString("file").equals(targetFile.getAbsolutePath())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.file_already_exists)
                    .setMessage(R.string.overwrite_file)
                    .setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            returnResult(targetFile);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
        } else {
            returnResult(targetFile);
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
        return R.drawable.ic_save_white_24dp;
    }

    @Override
    protected int getTitleText() {
        return R.string.save_file;
    }
}
