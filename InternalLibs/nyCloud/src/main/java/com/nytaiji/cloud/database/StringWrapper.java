package com.nytaiji.cloud.database;

/** Simple StringWrapper. */
public class StringWrapper {

  public final String value;

  public StringWrapper(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
