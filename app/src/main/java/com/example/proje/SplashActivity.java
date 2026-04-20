package com.example.proje;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.proje.auth.UserManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.iv_logo);
        TextView name = findViewById(R.id.tv_app_name);

        logo.setAlpha(0f);
        logo.setScaleX(0f);
        logo.setScaleY(0f);
        name.setAlpha(0f);

        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        UserManager userManager = new UserManager(this);

        name.animate()
                .alpha(1f)
                .setDuration(1200)
                .setStartDelay(300)
                .withEndAction(() -> {
                    Class<?> dest = userManager.isLoggedIn() ? MainActivity.class : LoginActivity.class;
                    startActivity(new Intent(SplashActivity.this, dest));
                    finish();
                })
                .start();
    }
}