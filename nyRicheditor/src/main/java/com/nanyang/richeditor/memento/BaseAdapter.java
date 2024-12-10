package com.nanyang.richeditor.memento;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;


import com.nanyang.richeditor.database.DatabaseModel;

import java.util.ArrayList;

abstract public class BaseAdapter<T extends DatabaseModel, VH extends BaseViewHolder<T>> extends RecyclerView.Adapter<VH> {
    private final ArrayList<T> items;
    private ArrayList<T> selected;
    private final ClickListener<T> clickListener;

    public BaseAdapter(ArrayList<T> items, ArrayList<T> selected, ClickListener<T> listener) {
        this.items = items;
        this.selected = selected;
        this.clickListener = listener;
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        final T item = items.get(position);

        // Populate view
        holder.populate(item);

        // TODO ny for the call from EditorActivity
     //   if (selected==null) selected=new ArrayList<T>();

        // Check if item is selected
        holder.setSelected(selected.contains(item));

        holder.holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected.isEmpty()) clickListener.onClick(item, items.indexOf(item));
                else toggleSelection(holder, item);
            }
        });

        holder.holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                 toggleSelection(holder, item);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (items!=null)
        return items.size();
        else return -1;
    }

    private void toggleSelection(VH holder, T item) {
        if (selected.contains(item)) {
            selected.remove(item);
            holder.setSelected(false);
            if (selected.isEmpty()) clickListener.onChangeSelection(false);
        } else {
            if (selected.isEmpty()) clickListener.onChangeSelection(true);
            selected.add(item);
            holder.setSelected(true);
        }
        clickListener.onCountSelection(selected.size());
    }

    public interface ClickListener<M extends DatabaseModel> {
        void onClick(M item, int position);

        void onChangeSelection(boolean haveSelected);

        void onCountSelection(int count);
    }

    public interface SelectionListener<M extends DatabaseModel> {
        void onItemSelected(ArrayList<M> selected);
    }

}
