package com.example.healthcare.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_info")
public class UserInfo {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String weight;
    public String height;
    public String allergyMedications;
    public String emergencyContact;
    public String firstName;
    public String lastName;
    public String dob;
    public String phone;
    public String email;
    public String password;
    public String sex;
    public boolean isAdmin;
    public String steps;
    public String bodyTemperature;
}
