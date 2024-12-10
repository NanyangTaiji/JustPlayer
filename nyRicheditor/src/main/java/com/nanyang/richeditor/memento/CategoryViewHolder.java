package com.nanyang.richeditor.memento;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.Category;
import com.nytaiji.nybase.utils.NyFormatter;

import java.util.Locale;

public class CategoryViewHolder extends BaseViewHolder<Category> {
	public TextView badge;
	public TextView title;
	public TextView date;
	public TextView keywords;
	public TextView counter;
	public ImageView lock,star;

	public CategoryViewHolder(View itemView) {
		super(itemView);
		badge = (TextView) itemView.findViewById(R.id.badge_image);
		title = (TextView) itemView.findViewById(R.id.item_title);
		keywords = (TextView) itemView.findViewById(R.id.keywords_txt);
		counter = (TextView) itemView.findViewById(R.id.title_txt);
		date = (TextView) itemView.findViewById(R.id.category_txt);
		lock = (ImageView) itemView.findViewById(R.id.locker);
		star = (ImageView) itemView.findViewById(R.id.star);
	}

	@Override
	public void populate(Category item) {
		badge.setText(item.title.substring(0, 1).toUpperCase(Locale.US));
		badge.setBackgroundResource(item.getThemeBackground());
		title.setText(item.title);
		if (item.counter == 0) counter.setText("");
		else if (item.counter == 1) counter.setText(R.string.one_note);
		else counter.setText(String.format(Locale.US, "%d notes", item.counter));
		date.setText(NyFormatter.formatShortDate(item.datelong));
		lock.setVisibility(item.isProtected ? View.VISIBLE : View.GONE);
		//all locked item must be stared
		if(!item.isProtected) star.setVisibility(item.isStard? View.VISIBLE : View.GONE);
		else star.setVisibility(View.GONE);
		keywords.setText(item.keywords);
	}
}
