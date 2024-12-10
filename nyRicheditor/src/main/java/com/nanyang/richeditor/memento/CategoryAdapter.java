package com.nanyang.richeditor.memento;

import android.view.LayoutInflater;
import android.view.ViewGroup;


import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.Category;

import java.util.ArrayList;

public class CategoryAdapter extends BaseAdapter<Category, CategoryViewHolder> {
	public CategoryAdapter(ArrayList<Category> items, ArrayList<Category> selected, ClickListener<Category> listener) {
		super(items, selected, listener);
	}

	@Override
	public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show_category, parent, false));
	}
}
