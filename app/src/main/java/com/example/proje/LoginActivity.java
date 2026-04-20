package com.example.proje;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.proje.auth.UserManager;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private UserManager userManager;
    private MaterialButtonToggleGroup toggleAuth;
    private View layoutLogin, layoutRegister;

    private TextInputEditText etLoginEmail, etLoginPassword;
    private TextInputEditText etRegName, etRegEmail, etRegPassword, etRegPasswordConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userManager = new UserManager(this);

        toggleAuth = findViewById(R.id.toggle_auth);
        layoutLogin = findViewById(R.id.layout_login);
        layoutRegister = findViewById(R.id.layout_register);

        etLoginEmail = findViewById(R.id.et_login_email);
        etLoginPassword = findViewById(R.id.et_login_password);
        etRegName = findViewById(R.id.et_reg_name);
        etRegEmail = findViewById(R.id.et_reg_email);
        etRegPassword = findViewById(R.id.et_reg_password);
        etRegPasswordConfirm = findViewById(R.id.et_reg_password_confirm);

        toggleAuth.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_tab_login) {
                layoutLogin.setVisibility(View.VISIBLE);
                layoutRegister.setVisibility(View.GONE);
            } else {
                layoutLogin.setVisibility(View.GONE);
                layoutRegister.setVisibility(View.VISIBLE);
            }
        });

        // Hesap yoksa kayıt tabına geç
        if (!userManager.hasAccount()) {
            toggleAuth.check(R.id.btn_tab_register);
        }

        findViewById(R.id.btn_login).setOnClickListener(v -> handleLogin());
        findViewById(R.id.btn_register).setOnClickListener(v -> handleRegister());
        findViewById(R.id.tv_forgot_password).setOnClickListener(v ->
                Toast.makeText(this, "Şifre sıfırlama: ayarlardan şifrenizi değiştirebilirsiniz", Toast.LENGTH_LONG).show());
    }

    private void handleLogin() {
        String email = text(etLoginEmail);
        String password = text(etLoginPassword);

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "E-posta ve şifreyi girin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userManager.login(email, password)) {
            goToMain();
        } else {
            Toast.makeText(this, "E-posta veya şifre hatalı", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegister() {
        String name = text(etRegName);
        String email = text(etRegEmail);
        String password = text(etRegPassword);
        String confirm = text(etRegPasswordConfirm);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Şifreler eşleşmiyor", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Geçerli bir e-posta girin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userManager.hasAccount()) {
            Toast.makeText(this, "Bu cihazda zaten bir hesap var. Giriş yapın.", Toast.LENGTH_LONG).show();
            toggleAuth.check(R.id.btn_tab_login);
            return;
        }

        if (userManager.register(name, email, password)) {
            goToMain();
        } else {
            Toast.makeText(this, "Kayıt oluşturulamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
