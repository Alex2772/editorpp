package ru.alex2772.editorpp.activity.settings;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ru.alex2772.editorpp.util.Util;

public class FontAdapter extends BaseAdapter {
    private Context mContext;
    private List<Item> mItems;
    private int mPadding;

    public FontAdapter(Context context) {
        mContext = context;
        mItems = getTypefaces(context);
        mPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
    }
    public static List<Item> getTypefaces(Context context) {
        ArrayList<Item> typefaces = new ArrayList<>();
        Map<String, Typeface> items = Util.getSSystemFontMap();
        for (Map.Entry<String, Typeface> i : items.entrySet()) {
            String s = i.getKey();
            s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
            typefaces.add(new Item(s, i.getValue()));
        }

        Collections.sort(typefaces, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.mName.compareTo(o2.mName);
            }
        });
        typefaces.add(0, new Item("JetBrains Mono", Typeface.createFromAsset(context.getAssets(), "fonts/JetBrainsMono-Regular.ttf")));
        return typefaces;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView v = new TextView(mContext);
        v.setText(mItems.get(position).mName);
        v.setTypeface(mItems.get(position).mTypeface);
        v.setPadding(mPadding, mPadding, mPadding, mPadding);
        return v;
    }

    public static class Item {
        public String mName;
        public Typeface mTypeface;

        public Item(String name, Typeface typeface) {
            mName = name;
            mTypeface = typeface;
        }
    }
}
