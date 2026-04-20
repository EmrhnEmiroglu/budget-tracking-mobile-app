package com.example.proje.auth;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserManager {

    private static final String PREFS = "vault_auth";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PASSWORD_HASH = "password_hash";

    private final SharedPreferences prefs;

    public UserManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public boolean hasAccount() {
        return prefs.contains(KEY_EMAIL);
    }

    public boolean register(String name, String email, String password) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) return false;
        if (hasAccount()) return false;
        prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD_HASH, hash(password))
                .putBoolean(KEY_LOGGED_IN, true)
                .apply();
        return true;
    }

    public boolean login(String email, String password) {
        String storedEmail = prefs.getString(KEY_EMAIL, null);
        String storedHash = prefs.getString(KEY_PASSWORD_HASH, null);
        if (storedEmail == null || !storedEmail.equalsIgnoreCase(email)) return false;
        if (!hash(password).equals(storedHash)) return false;
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply();
        return true;
    }

    public void logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply();
    }

    public boolean changePassword(String currentPassword, String newPassword) {
        String storedHash = prefs.getString(KEY_PASSWORD_HASH, null);
        if (storedHash == null || !hash(currentPassword).equals(storedHash)) return false;
        prefs.edit().putString(KEY_PASSWORD_HASH, hash(newPassword)).apply();
        return true;
    }

    public void updateProfile(String name, String email) {
        prefs.edit().putString(KEY_NAME, name).putString(KEY_EMAIL, email).apply();
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "Kullanıcı");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    private static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return input;
        }
    }
}
