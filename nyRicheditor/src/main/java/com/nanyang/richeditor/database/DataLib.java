package com.nanyang.richeditor.database;

public class DataLib {

    private int order = -1;  // display order in viewpager
    private String name;     // database name xxx.db
    private String title;    // display title for the database
    private long updateDate = -1L; // last updated date
    private boolean isModified = false; // if false, no need to synchronize with remote database

    public DataLib(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public DataLib(int order, String name, String title) {
        this.order = order;
        this.name = name;
        this.title = title;
    }

    // Getter and Setter methods...

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }
}