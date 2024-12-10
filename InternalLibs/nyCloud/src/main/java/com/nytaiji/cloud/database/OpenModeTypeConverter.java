package com.nytaiji.cloud.database;

import androidx.room.TypeConverter;


import com.nytaiji.cloud.OpenMode;


public final class OpenModeTypeConverter {

    public static final OpenModeTypeConverter INSTANCE= new OpenModeTypeConverter();


    @TypeConverter
    public static int fromOpenMode(OpenMode from) {
        return from.ordinal();
    }

    @TypeConverter

    public static OpenMode fromDatabaseValue(int from) {
        return OpenMode.getOpenMode(from);
    }

    private OpenModeTypeConverter() {
    }

}

