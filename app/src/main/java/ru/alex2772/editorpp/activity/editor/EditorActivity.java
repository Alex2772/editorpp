package ru.alex2772.editorpp.activity.editor;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Visibility;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.activity.AboutActivity;
import ru.alex2772.editorpp.activity.editor.highlight.SyntaxHighlighter;
import ru.alex2772.editorpp.activity.editor.highlight.SyntaxManager;
import ru.alex2772.editorpp.activity.editor.highlight.syntax.IHighlighter;
import ru.alex2772.editorpp.activity.editor.theme.ThemeManager;
import ru.alex2772.editorpp.activity.filebrowser.OpenFileBrowserActivity;
import ru.alex2772.editorpp.activity.filebrowser.SaveFileBrowserActivity;
import ru.alex2772.editorpp.activity.settings.SettingsActivity;
import ru.alex2772.editorpp.database.DBHelper;
import ru.alex2772.editorpp.model.FileTabModel;
import ru.alex2772.editorpp.util.MTP;
import ru.alex2772.editorpp.util.Util;
import ru.alex2772.editorpp.view.HighlightEditText;
import ru.alex2772.editorpp.view.Trackpad;

public class EditorActivity extends AppCompatActivity implements HighlightEditText.ISelectionChangedListener, TextWatcher, SharedPreferences.OnSharedPreferenceChangeListener {

    private static int newFileCounter = 1;

    private LinkedList<FileTabModel> mTabs = new LinkedList<>();

    private TextView mDisplayRow;
    private TextView mDisplayCol;
    private HighlightEditText mEdit;
    private Button mSaveButton;
    private String mFilePath;
    private SaveState mSaveState = SaveState.SAVED;
    private RecyclerView mTabRecyler;
    private FileTabAdapter mTabsAdapter;
    private FileTabModel mCurrentTab = null;

    private ThemeManager mThemeManager;
    private SyntaxManager mSyntaxManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Statusbar height
        setContentView(R.layout.activity_editor);
        {
            try {
                int statusBarHeight = 0;
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId <= 0)
                    throw new UnsupportedOperationException("Resource is not available");
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);

                ((CoordinatorLayout.LayoutParams)findViewById(R.id.app_bar).getLayoutParams()).setMargins(0, -statusBarHeight, 0, 0);
                Util.setHeight(findViewById(R.id.app_bar), findViewById(R.id.app_bar).getLayoutParams().height + statusBarHeight);
                findViewById(R.id.app_bar).setBackgroundResource(R.color.colorPrimary);
                ((LinearLayout.LayoutParams)findViewById(R.id.toolbar_layout).getLayoutParams()).setMargins(0, statusBarHeight, 0, 0);
                ((CollapsingToolbarLayout.LayoutParams)findViewById(R.id.tabs_wrap).getLayoutParams()).setMargins(0, statusBarHeight, 0, 0);
                ((CoordinatorLayout.LayoutParams)findViewById(R.id.nested).getLayoutParams()).setMargins(0, 0, 0, -statusBarHeight);
                Util.setHeight(findViewById(R.id.statusBar), statusBarHeight);
                Util.setHeight(findViewById(R.id.statusBarExtended), statusBarHeight);
                Util.setHeight(findViewById(R.id.toolbar), statusBarHeight);
            } catch (Exception e) {
                Log.e("++", "Could not set custom appbar", e);
            } finally {
                if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }

                if (Build.VERSION.SDK_INT >= 19)
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            //| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // | View.SYSTEM_UI_FLAG_FULLSCREEN
                    );

                if (Build.VERSION.SDK_INT >= 21) {
                    //getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    //getWindow().setStatusBarColor(getResources().getColor(R.color.blackTransparent));
                    getWindow().setStatusBarColor(Color.TRANSPARENT);
                    getWindow().setNavigationBarColor(0xff0000ff);

                }
            }

            // some devices have soft buttons or custom status bar so we will ask android about
            // dimensions which are taken by the OS.
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                    int x = insets.getSystemWindowInsetRight();

                    // the left side should be padded using tabs view because whe want to keep
                    // left side with accent background
                    findViewById(R.id.tabs).setPadding(insets.getSystemWindowInsetLeft(), 0, 0, 0);

                    // but the right side we will pad with tabs wrap because we want to make the right
                    // padding filled with gray color
                    findViewById(R.id.tabs_wrap).setPadding(0, 0, insets.getSystemWindowInsetRight(), 0);


                    // controls frame contains controls so we should pad it from all sides except
                    // top because we have a toolbar on top
                    findViewById(R.id.controlsFrame).setPadding(insets.getSystemWindowInsetLeft(),
                                                                0,
                                                                insets.getSystemWindowInsetRight(),
                                                                insets.getStableInsetBottom());
                    // pad left side of editText.
                    findViewById(R.id.nested).setPadding(insets.getSystemWindowInsetLeft(), 0, 0, 0);

                    return insets;
                }
            });
        }
        final View root = findViewById(R.id.root);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            public void onGlobalLayout(){
                int screenHeight = root.getRootView().getHeight();
                Rect r = new Rect();
                View view = getWindow().getDecorView();
                view.getWindowVisibleDisplayFrame(r);

                int keyBoardHeight = screenHeight - r.bottom;
                Log.d("++", "keyboard height: " + (keyBoardHeight));

                root.setPadding(0, 0, 0, keyBoardHeight);
            }
        });

        mThemeManager = new ThemeManager(this);
        mSyntaxManager = new SyntaxManager(this);

        findViewById(R.id.fakeMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(EditorActivity.this, v);
                p.inflate(R.menu.menu_editor);
                MenuItem setSyntax = p.getMenu().findItem(R.id.set_syntax);

                int counter = 0;
                p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.open: {
                                open();
                                break;
                            }
                            case R.id.new_file: {
                                String file = getResources().getString(R.string.new_file) + " " + ++newFileCounter + ".txt";
                                mTabs.add(new FileTabModel(file, file));
                                setCurrentTab(mTabs.size() - 1);
                                break;
                            }
                            case R.id.save_dummy:
                                saveDummy();
                                break;
                            case R.id.save_as:
                                saveAs();
                                break;
                            case R.id.find_replace:

                                break;
                            case R.id.settings:
                                startActivity(new Intent(EditorActivity.this, SettingsActivity.class));
                                break;
                            case R.id.action_about:
                                startActivity(new Intent(EditorActivity.this, AboutActivity.class));
                                break;
                        }
                        return false;
                    }
                });
                for (IHighlighter h : mSyntaxManager.getSyntaxes()) {
                    final int index = counter++;
                    setSyntax.getSubMenu().add(h.getName()).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            mCurrentTab.setSyntax(index);
                            mEdit.invalidateHighlight();
                            DBHelper.updateFile(EditorActivity.this, mCurrentTab);
                            return true;
                        }
                    }).setChecked(index == mCurrentTab.getSyntax());
                }
                setSyntax.getSubMenu().setGroupCheckable(0, true, true);
                p.show();
            }
        });

        mDisplayRow = findViewById(R.id.displayRow);
        mDisplayCol = findViewById(R.id.displayCol);

        //NestedScrollView nested = findViewById(R.id.nested);

        mEdit = findViewById(R.id.editText);
        mEdit.setSelectionChangedListener(this);
        /*
        mEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mEdit.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        //return true;
                    }
                }
                return false;
            }
        });*/

        ((Trackpad) findViewById(R.id.trackpad)).setEditText(mEdit);
        mEdit.addTextChangedListener(this);

        mSaveButton = findViewById(R.id.save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSaveState == SaveState.SAVED) {
                    saveAs();
                } else if (mSaveState == SaveState.UNSAVED) {
                    saveDummy();
                }
            }
        });
        mTabRecyler = findViewById(R.id.tabs);
        mTabRecyler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mTabRecyler.setAdapter(mTabsAdapter = new FileTabAdapter(this, mTabs));

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("file")) {
            openFile(getIntent().getExtras().getString("file"));
        } else if (getIntent().getData() != null) {
            openFile(getIntent().getData().getPath());
        } else {
            String file = getResources().getString(R.string.new_file) + ".txt";
            mTabs.add(new FileTabModel(file, file));
            setCurrentTab(0);
        }

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
        sPref.registerOnSharedPreferenceChangeListener(mEdit);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        mEdit.flushPreferences();

        findViewById(R.id.btnTAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdit.getText().insert(mEdit.getSelectionStart(), mEdit.getTabs());
            }
        });
        findViewById(R.id.btnEND).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i;
                for (i = mEdit.getSelectionStart(); i < mEdit.getText().length() && mEdit.getText().charAt(i) != '\n'; ++i)
                    ;
                mEdit.setSelection(i);
            }
        });
        findViewById(R.id.btnNL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i;
                for (i = mEdit.getSelectionStart(); i < mEdit.getText().length() && mEdit.getText().charAt(i) != '\n'; ++i)
                    ;
                mEdit.setSelection(i);
                mEdit.getText().insert(i, "\n");
            }
        });

        mEdit.requestFocus();

        handleTitlebarAdditionalLabels();
    }



    public void setCurrentTab(int index) {
        if (mCurrentTab != null) {
            findViewById(R.id.root).setFocusable(true);
            findViewById(R.id.root).setFocusableInTouchMode(true);
            mCurrentTab.setText(mEdit.getText());
            mCurrentTab.setSaveState(mSaveState);
            mCurrentTab.setSelectionStart(mEdit.getSelectionStart());
            mCurrentTab.setSelectionEnd(mEdit.getSelectionEnd());
            mCurrentTab.setScrollX(mEdit.getScrollX());
            mCurrentTab.setScrollY(mEdit.getScrollY());
            mCurrentTab.setCurrent(false);
        }
        mCurrentTab = mTabs.get(index);
        mEdit.setEnabled(mCurrentTab.isEditable());
        mEdit.setTextChangedFlag(false);
        mEdit.setText(mCurrentTab.getText());
        mEdit.setTextChangedFlag(true);
        mEdit.setSelection(mCurrentTab.getSelectionStart(), mCurrentTab.getSelectionEnd());
        mEdit.setScrollX(mCurrentTab.getScrollX());
        mEdit.setScrollY(mCurrentTab.getScrollY());
        mCurrentTab.setCurrent(true);
        mSaveState = mCurrentTab.getSaveState();
        setFilePath(mCurrentTab.getFilePath());
        mTabsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        handleTitlebarAdditionalLabels();
    }

    private void handleTitlebarAdditionalLabels() {
        int v = getResources().getConfiguration().screenWidthDp > 450 ? View.VISIBLE : View.INVISIBLE;
        findViewById(R.id.fileName).setVisibility(v);
        findViewById(R.id.statusBar).setVisibility(v);
    }

    /**
     * Sometimes files are big enough to crash the app. This dialog tells user that we will not
     * do the desired action because file is too big.
     */
    private void showVeryBigFileWarning(@StringRes int action) {
        new AlertDialog.Builder(EditorActivity.this)
                .setTitle(action)
                .setMessage(R.string.file_too_big_anr_warning)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();

    }

    /**
     * Save without save dialog.
     */
    private void saveDummy() {
        if (mCurrentTab != null && !mCurrentTab.isEditable()) {
            showVeryBigFileWarning(R.string.save_is_not_available);
            return;
        }
        if (new File(mFilePath).exists()) {
            saveFile(mFilePath);
        } else {
            saveAs();
        }
    }


    private void open() {
        Intent i = new Intent(this, OpenFileBrowserActivity.class);
        i.putExtra("file", mFilePath);
        ActivityCompat.startActivityForResult(this, i, 1, null);
    }

    /**
     * Forces save dialog.
     */
    private void saveAs() {
        if (mCurrentTab != null && !mCurrentTab.isEditable()) {
            showVeryBigFileWarning(R.string.save_is_not_available);
            return;
        }
        Intent i = new Intent(this, SaveFileBrowserActivity.class);
        i.putExtra("file", mFilePath);
        ActivityCompat.startActivityForResult(this, i, 0, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: // saveAs
            {
                if (data != null && data.getData() != null) {
                    saveFile(data.getData().getPath());
                }
                break;
            }
            case 1: // open
            {
                if (data != null && data.getData() != null) {
                    openFile(data.getData().getPath());
                }
                break;
            }
        }
    }

    private void openFile(final String path) {
        openFileForce(new File(path));
    }
    private void openFileForce(final File f) {
        MTP.schedule(new Runnable() {
            @Override
            public void run() {
                final StringBuilder s = new StringBuilder();
                DataInputStream fis = null;
                try {
                    byte[] buf = new byte[(int) f.length()];
                    fis = new DataInputStream(new FileInputStream(f));
                    fis.readFully(buf);
                    s.append(new String(buf));
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.handleException(EditorActivity.this, R.string.could_not_open, e);
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FileTabModel m = new FileTabModel(f.getAbsolutePath(), f.getName());

                            String finalString;
                            if (s.length() > 0x8000) {
                                finalString = s.substring(0, 0x8000);
                                m.setEditable(false);
                                new AlertDialog.Builder(EditorActivity.this)
                                        .setTitle(R.string.file_too_big)
                                        .setMessage(R.string.file_too_big_anr_warning)
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .create().show();

                            } else {
                                finalString = s.toString();
                            }

                            m.setText(Editable.Factory.getInstance().newEditable(finalString));
                            mTabs.add(m);
                            setCurrentTab(mTabs.size() - 1);
                            DBHelper.getFileData(EditorActivity.this, m, new Runnable() {
                                @Override
                                public void run() {
                                    mEdit.invalidateHighlight();
                                }
                            });
                            DBHelper.updateFile(EditorActivity.this, m);
                        }
                    });
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Saves the file.
     *
     * @param path path to save.
     */
    private void saveFile(final String path) {
        setFilePath(path);
        DBHelper.updateFile(this, mCurrentTab);
        setSavedState(SaveState.SAVING);
        final byte[] s = mEdit.getText().toString().getBytes();
        MTP.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = new File(path);
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(s);
                    fos.close();
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.handleException(EditorActivity.this, R.string.could_not_save, e);
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSavedState(SaveState.SAVED);
                        }
                    });
                }
            }
        });
    }

    /**
     * Update UI with save state.
     */
    @SuppressLint("SetTextI18n")
    private void setSavedState(SaveState b) {
        if (mSaveState != b) {
            switch (b) {
                case UNSAVED:
                    mSaveButton.setText("");
                    ((TextView) findViewById(R.id.fileName)).setText(((TextView) findViewById(R.id.fileName)).getText() + "*");
                    break;
                case SAVED:
                    mSaveButton.setText(R.string.saved);
                    break;
                case SAVING:
                    mSaveButton.setText(R.string.saving);
                    break;
            }
            mSaveState = b;
        }
    }

    private void setFilePath(String name) {
        mFilePath = name;
        mCurrentTab.setFilePath(name);
        ((TextView) findViewById(R.id.fileName)).setText(new File(name).getName());
        mTabsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSelectionChanged(EditText editText, int selStart, int selEnd) {
        try {
            int line = editText.getLayout().getLineForOffset(editText.getSelectionStart());
            int column = editText.getSelectionStart() - editText.getLayout().getLineStart(line);

            mDisplayRow.setText(String.valueOf(line + 1));
            mDisplayCol.setText(String.valueOf(column + 1));
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeFile();
            return mTabs.isEmpty();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        try {
            mEdit.setHighlighter(mSyntaxManager.getSyntaxes().get(mCurrentTab.getSyntax()));
        } catch (Exception ignored) {
            // possibly NPE will be catched.
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            {
                int lineBegin = start;
                //noinspection StatementWithEmptyBody
                for (; lineBegin > 0 && s.charAt(lineBegin - 1) != '\n'; --lineBegin) ;

                int lineEnd = start + count - 1;
                //noinspection StatementWithEmptyBody
                for (; lineEnd < s.length() && s.charAt(lineEnd) != '\n'; ++lineEnd) ;
                lineEnd -= 1;

                SyntaxHighlighter.highlight(this, mThemeManager.getTheme(),
                        mSyntaxManager.getSyntaxes().get(mCurrentTab.getSyntax()), mEdit.getText(),
                        lineBegin, lineEnd);
            }
        } catch (Exception ignored) {
            /*
             * I love java for it's internal checks. You do not need check it by yourself. You just
             * have to catch exception and say 'Okay. No harm in trying.'
             */
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        setSavedState(SaveState.UNSAVED);
    }

    public boolean closeFile() {

        if (mSaveState != SaveState.SAVED && mEdit.getText().length() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.file_unsaved)
                    .setMessage(R.string.file_unsaved_are_u_sure)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveDummy();
                        }
                    })
                    .setNeutralButton(R.string.dont_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            closeFileSure();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
            return false;
        }
        closeFileSure();
        return true;
    }

    private void closeFileSure() {

        int index = mTabs.indexOf(mCurrentTab);
        if (index < 0)
            mCurrentTab = null;
        else {
            mTabs.remove(mCurrentTab);
            index -= 1;
            mSaveState = SaveState.SAVED;
            if (index < 0)
                index = 0;
            if (index < mTabs.size())
                setCurrentTab(index);
            else
                mTabsAdapter.notifyDataSetChanged();
        }

        if (mTabs.isEmpty())
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mEdit);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "bp_tab":
                findViewById(R.id.btnTAB).setVisibility(sharedPreferences.getBoolean(key, true) ? View.VISIBLE : View.GONE);
                break;
            case "bp_end":
                findViewById(R.id.btnEND).setVisibility(sharedPreferences.getBoolean(key, true) ? View.VISIBLE : View.GONE);
                break;
            case "bp_trackpad":
                findViewById(R.id.trackpad).setVisibility(sharedPreferences.getBoolean(key, true) ? View.VISIBLE : View.GONE);
                break;
            case "bp_nl":
                findViewById(R.id.btnNL).setVisibility(sharedPreferences.getBoolean(key, true) ? View.VISIBLE : View.GONE);
                break;
        }
    }
}
