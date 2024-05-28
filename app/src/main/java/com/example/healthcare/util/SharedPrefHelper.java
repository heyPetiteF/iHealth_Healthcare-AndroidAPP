package com.example.healthcare.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {

    private static final String PREF_NAME = "healthcare_prefs";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveCurrentUserEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getCurrentUserEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clear() {
        editor.remove(KEY_EMAIL);
        editor.apply();
    }
}
