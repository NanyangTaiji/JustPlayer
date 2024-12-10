/*
 * Copyright (C) 2014-2020 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com> and Contributors.
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nytaiji.cloud.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.nytaiji.cloud.OpenMode;

/** Created by vishal on 18/4/17. */
@Entity(tableName = NyDatabase.TABLE_CLOUD_PERSIST)
public class CloudEntry {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = NyDatabase.COLUMN_CLOUD_ID)
  private int _id;

  @ColumnInfo(name = NyDatabase.COLUMN_CLOUD_SERVICE)
  @TypeConverters(OpenModeTypeConverter.class)
  private OpenMode serviceType;

  @ColumnInfo(name = NyDatabase.COLUMN_CLOUD_PERSIST)
  //TODO ny
 // @TypeConverters(EncryptedStringTypeConverter.class)
  private StringWrapper persistData;

  public CloudEntry() {}

  public CloudEntry(OpenMode serviceType, String persistData) {
    this.serviceType = serviceType;
    this.persistData = new StringWrapper(persistData);
  }

  public void setId(int _id) {
    this._id = _id;
  }

  public int getId() {
    return this._id;
  }

  public void setPersistData(StringWrapper persistData) {
    this.persistData = persistData;
  }

  public StringWrapper getPersistData() {
    return this.persistData;
  }

  /** Set the service type Support values from {@link OpenMode} */
  public void setServiceType(OpenMode openMode) {
    this.serviceType = openMode;
  }

  /** Returns ordinal value of service from {@link OpenMode} */
  public OpenMode getServiceType() {
    return this.serviceType;
  }
}
