package com.example.healthcare.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alert")
public class Alert {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String message;
    public long timestamp;
    public float temperature;
}
