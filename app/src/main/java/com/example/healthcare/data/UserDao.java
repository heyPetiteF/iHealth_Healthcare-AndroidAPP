package com.example.healthcare.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(UserInfo userInfo);
    @Update
    void update(UserInfo userInfo);
    @Query("SELECT * FROM user_info WHERE id = :id")
    UserInfo getUserById(int id);

    @Query("SELECT * FROM user_info WHERE email = :email")
    UserInfo getUserByEmail(String email);

    @Query("SELECT * FROM user_info WHERE email = :email AND password = :password")
    UserInfo login(String email, String password);

    @Query("SELECT * FROM user_info WHERE email = :email AND firstName = :firstName AND dob = :dob")
    UserInfo getUserByEmailAndNameAndDob(String email, String firstName, String dob);

    @Query("SELECT * FROM user_info")
    List<UserInfo> getAllUsers();
}
