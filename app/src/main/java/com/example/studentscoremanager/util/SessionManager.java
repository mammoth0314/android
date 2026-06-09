package com.example.studentscoremanager.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_REAL_NAME = "real_name";

    private SessionManager() {
    }

    public static void save(Context context, String username, String role, String realName) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .putString(KEY_REAL_NAME, realName)
                .apply();
    }

    public static String getUsername(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USERNAME, "");
    }

    public static String getRole(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ROLE, "");
    }

    public static String getRealName(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_REAL_NAME, "");
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}

