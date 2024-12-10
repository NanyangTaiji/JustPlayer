package com.nanyang.richeditor.memento;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nanyang.richeditor.App;
import com.nanyang.richeditor.R;
import com.nanyang.richeditor.database.Controller;
import com.nanyang.richeditor.database.DatabaseModel;
import com.nanyang.richeditor.database.Note;
import com.nytaiji.nybase.utils.NyFormatter;


public class NoteViewHolder extends BaseViewHolder<Note> {
    public ImageView lock,star, badge;
    public TextView date;
    public TextView category, title;
    public TextView keywords;

    public NoteViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title_txt);
        lock = (ImageView) itemView.findViewById(R.id.locker);
        star = (ImageView) itemView.findViewById(R.id.star);
        date = (TextView) itemView.findViewById(R.id.item_title);
        keywords = (TextView) itemView.findViewById(R.id.keywords_txt);
        category = (TextView) itemView.findViewById(R.id.category_txt);
        badge = (ImageView) itemView.findViewById(R.id.badge_image);
    }

    @Override
    public void populate(Note item) {
        lock.setVisibility(item.isProtected ? View.VISIBLE : View.GONE);
        //all locked item must be stared
        if(!item.isProtected) star.setVisibility(item.isStard? View.VISIBLE : View.GONE);
        else star.setVisibility(View.GONE);

        date.setText(NyFormatter.nyFormat(item.datelong));
        title.setText(item.title);
      // badge.setText(item.title.substring(0, 1).toUpperCase(Locale.US));
        if (item.type == DatabaseModel.TYPE_WEBSITE) {
            badge.setImageResource(R.drawable.ic_root_cloud);
        } else {
            badge.setImageResource(R.drawable.ic_content);
        }

        if(item.type!= DatabaseModel.TYPE_WEBSITE) keywords.setText(item.keywords);
        //to fix the exception for newly created note
        try {
            category.setText(new Controller(App.instance).findCategoryById(item.parentId).title);
        } catch (Exception ignored) {
        }
        //  badge.setBackgroundResource(item.getThemeBackground());
    }
}
