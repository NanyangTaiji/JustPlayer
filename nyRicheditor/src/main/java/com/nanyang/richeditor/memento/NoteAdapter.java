package com.nanyang.richeditor.memento;

import android.view.LayoutInflater;
import android.view.ViewGroup;


import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.Note;

import java.util.ArrayList;

public class NoteAdapter extends BaseAdapter<Note, NoteViewHolder> {
	public NoteAdapter(ArrayList<Note> items, ArrayList<Note> selected, ClickListener<Note> listener) {
		super(items, selected, listener);
	}

	@Override
	public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show_note, parent, false));
	}
}