package com.example.studentscoremanager.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class PrefUtils {
    private static final String PREF_NAME = "login_prefs";
    private static final String KEY_REMEMBER = "remember_account";
    private static final String KEY_REMEMBER_ENABLED = "remember_enabled";

    private PrefUtils() {
    }

    public static void saveRememberedAccount(Context context, String account) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_REMEMBER, account)
                .putBoolean(KEY_REMEMBER_ENABLED, true)
                .apply();
    }

    public static String getRememberedAccount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_REMEMBER, "");
    }

    public static void clearRememberedAccount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .remove(KEY_REMEMBER)
                .putBoolean(KEY_REMEMBER_ENABLED, false)
                .apply();
    }

    public static boolean isRememberEnabled(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_REMEMBER_ENABLED, false);
    }
}
