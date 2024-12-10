package com.nanyang.richeditor.view;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class Drawer {

	public int type;
	@DrawableRes
	public int resId;
	@StringRes
	public int title;

	public Drawer() {}

	public Drawer(int type, @DrawableRes int resId, @StringRes int title) {
		this.type = type;
		this.resId = resId;
		this.title = title;
	}
}
