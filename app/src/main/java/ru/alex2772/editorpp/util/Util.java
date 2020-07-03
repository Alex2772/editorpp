package ru.alex2772.editorpp.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.alex2772.editorpp.R;
import ru.alex2772.editorpp.activity.editor.EditorActivity;
import ru.alex2772.editorpp.drawable.FileIconDrawable;

public class Util {
    public static void handleException(Context c, int title, Exception e) {
        AlertDialog.Builder b = new AlertDialog.Builder(c);
        b.setTitle(title);
        if (e instanceof FileNotFoundException) {
            b.setMessage(R.string.not_found_error);
        } else if (e instanceof IOException) {
            b.setMessage(R.string.io_error);
        } else {
            b.setMessage(R.string.unkown_error);
        }
        b.setPositiveButton(android.R.string.ok, null);
        b.create().show();

        e.printStackTrace();
    }

    public static void startEditor(Context c, String path) {
        Intent i = new Intent(c, EditorActivity.class);
        i.putExtra("file", path);
        c.startActivity(i);
    }

    public static Drawable getIconForFile(Context context, String path) {
        String p = path.toLowerCase();
        if (p.endsWith(".cpp") || p.endsWith(".cxx") || p.endsWith(".cc"))
            return new FileIconDrawable(context, "++");
        if (p.endsWith(".c"))
            return new FileIconDrawable(context, "C");
        if (p.endsWith(".h") || p.endsWith(".hxx") || p.endsWith(".hpp"))
            return new FileIconDrawable(context, "H");
        if (p.endsWith(".java"))
            return new FileIconDrawable(context, "J");
        if (p.endsWith(".py"))
            return new FileIconDrawable(context, "PY");
        if (p.endsWith(".xml"))
            return new FileIconDrawable(context, "XML");
        if (p.endsWith(".php"))
            return new FileIconDrawable(context, "PHP");
        if (p.endsWith(".txt"))
            return new FileIconDrawable(context, "TXT");
        if (p.endsWith(".ini"))
            return new FileIconDrawable(context, "INI");
        if (p.endsWith(".cfg"))
            return new FileIconDrawable(context, "CFG");
        if (p.endsWith(".conf"))
            return new FileIconDrawable(context, "CONF");

        return context.getResources().getDrawable(R.drawable.ic_file);
    }

    public static Map<String, Typeface> getSSystemFontMap() {
        Map<String, Typeface> sSystemFontMap = new HashMap<>();
        try {
            //Typeface typeface = Typeface.class.newInstance();
            Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
            Field f = Typeface.class.getDeclaredField("sSystemFontMap");
            f.setAccessible(true);
            sSystemFontMap = (Map<String, Typeface>) f.get(typeface);
            for (Map.Entry<String, Typeface> entry : sSystemFontMap.entrySet()) {
                Log.d("FontMap", entry.getKey() + " ---> " + entry.getValue() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sSystemFontMap;
    }

    public static List<String> getKeyWithValue(Map map, Typeface value) {
        Set set = map.entrySet();
        List<String> arr = new ArrayList<>();
        for (Object obj : set) {
            Map.Entry entry = (Map.Entry) obj;
            if (entry.getValue().equals(value)) {
                String str = (String) entry.getKey();
                arr.add(str);
            }
        }
        return arr;
    }

    public static void setHeight(View v, int height) {
        ViewGroup.LayoutParams p = v.getLayoutParams();
        p.height = height;
        v.setLayoutParams(p);
    }

    public static String prettySize(long size) {
        double fsize = size;
        double power = Math.log(fsize) / Math.log(2);
        double index = Math.max(Math.floor(power / 10 - 0.01), 0.0);

        String[] strs = {
                        "B",
                        "KB",
                        "MB",
                        "GB",
                        "TB",
                        "PB"
                };

        return ((int)(size / Math.pow(1024, index))) + " " + strs[(int) Math.ceil(index)];

    }
}
