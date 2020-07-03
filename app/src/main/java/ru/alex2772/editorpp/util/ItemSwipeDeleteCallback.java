package ru.alex2772.editorpp.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ru.alex2772.editorpp.R;

public class ItemSwipeDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final Drawable mIcon;
    private final ColorDrawable mColor;
    private final int mDpSize;
    private IItemDeletable mIItemDeletable;


    public ItemSwipeDeleteCallback(IItemDeletable iItemDeletable) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mIItemDeletable = iItemDeletable;
        mIcon = iItemDeletable.getContext().getResources().getDrawable(R.drawable.ic_close_white_24dp);
        mColor = new ColorDrawable(iItemDeletable.getContext().getResources().getColor(R.color.colorPrimaryDark));
        mDpSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18.f, iItemDeletable.getContext().getResources().getDisplayMetrics());
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof IItemDeletableShield)
            return 0;
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mIItemDeletable.onItemDelete(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        mColor.setBounds(viewHolder.itemView.getLeft(), viewHolder.itemView.getTop(), viewHolder.itemView.getRight(), viewHolder.itemView.getBottom());
        mColor.draw(c);

        int margin = (viewHolder.itemView.getBottom() - viewHolder.itemView.getTop()) / 2;
        int yPos = margin + viewHolder.itemView.getTop();
        if (dX > 0) {
            mIcon.setBounds(viewHolder.itemView.getLeft() + margin / 2, yPos - mDpSize, viewHolder.itemView.getLeft() + margin / 2 + mDpSize * 2, yPos + mDpSize);
            mIcon.draw(c);
        } else if (dX < 0) {
            mIcon.setBounds(viewHolder.itemView.getRight() - margin / 2 - mDpSize * 2, yPos - mDpSize, viewHolder.itemView.getRight() - margin / 2, yPos + mDpSize);
            mIcon.draw(c);
        }
    }

    public interface IItemDeletable {
        void onItemDelete(int index);
        Context getContext();
    }
    public interface IItemDeletableShield {
    }
}
