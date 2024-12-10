package com.nytaiji.nybase.filePicker;

public class Folder {
	public String name;
	public String path;
	public String size;
	public boolean isBack;
	public boolean isDirectory;

	public Folder(String name, String path, boolean isBack) {
		this.name = name;
		this.path = path;
		this.isBack = isBack;
		this.isDirectory = true;
	}

	public Folder(String name, String path, boolean isBack, boolean isDirectory) {
		this.name = name;
		this.path = path;
		this.isBack = isBack;
		this.isDirectory = isDirectory;
	}

	public Folder(String name, String path, String size, boolean isBack, boolean isDirectory) {
		this.name = name;
		this.path = path;
		this.size = size;
		this.isBack = isBack;
		this.isDirectory = isDirectory;
	}
}
