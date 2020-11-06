package ru.alex2772.editorpp.activity;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import ru.alex2772.editorpp.BuildConfig;
import ru.alex2772.editorpp.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView version = findViewById(R.id.version);
        version.setText(getResources().getString(R.string.about_version, BuildConfig.VERSION_NAME));

        TextView sourceCode = findViewById(R.id.textView10);
        sourceCode.setText(Html.fromHtml(String.format("<a href=\"https://github.com/Alex2772/editorpp\">%s</a>", getResources().getString(R.string.source_code))));
        sourceCode.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
