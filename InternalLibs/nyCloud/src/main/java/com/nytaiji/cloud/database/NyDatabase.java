package com.nytaiji.cloud.database;

import static com.nytaiji.cloud.database.NyDatabase.DATABASE_VERSION;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.arch.core.util.Function;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Repository for   {@link CloudEntry} in
 * explorer.db in Amaze.
 *
 * @see RoomDatabase
 */
@Database(
    entities = { CloudEntry.class},
    version = DATABASE_VERSION)
public abstract class NyDatabase extends RoomDatabase {

  private static final String DATABASE_NAME = "explorer.db";
  protected static final int DATABASE_VERSION = 1;

  public static final String TABLE_CLOUD_PERSIST = "cloud";
  public static final String COLUMN_PATH = "path";
  public static final String COLUMN_HOME = "home";
  public static final String COLUMN_CLOUD_ID = "_id";
  public static final String COLUMN_CLOUD_SERVICE = "service";
  public static final String COLUMN_CLOUD_PERSIST = "persist";
  public static final String COLUMN_SORT_PATH = "path";
  public static final String COLUMN_SORT_TYPE = "type";

  @VisibleForTesting
  public static Function<Context, Builder<NyDatabase>> overrideDatabaseBuilder = null;

  private static final String TEMP_TABLE_PREFIX = "temp_";

  // 1->2: add encrypted table (66f08f34)


  protected abstract CloudEntryDao cloudEntryDao();

  public static synchronized NyDatabase initialize(@NonNull Context context) {
    Builder<NyDatabase> builder =
        (overrideDatabaseBuilder == null)
            ? Room.databaseBuilder(context, NyDatabase.class, DATABASE_NAME)
            : overrideDatabaseBuilder.apply(context);
    return builder
        .allowMainThreadQueries()
        .build();
  }
}
