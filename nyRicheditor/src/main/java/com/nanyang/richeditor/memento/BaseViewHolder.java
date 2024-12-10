package com.nanyang.richeditor.memento;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.DatabaseModel;


abstract public class BaseViewHolder<T extends DatabaseModel> extends RecyclerView.ViewHolder {
	public View holder;
	public View selected;

	public BaseViewHolder(View itemView) {
		super(itemView);
		holder = itemView.findViewById(R.id.holder);
		selected = itemView.findViewById(R.id.selected);
	}

	public void setSelected(boolean status) {
		selected.setVisibility(status ? View.VISIBLE : View.GONE);
	}

	abstract public void populate(T item);
}
