package com.example.proje;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.proje.fragment.AddTransactionFragment;
import com.example.proje.fragment.BudgetLimitFragment;
import com.example.proje.fragment.GoalFragment;
import com.example.proje.fragment.HomeFragment;
import com.example.proje.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                f = new HomeFragment();
            } else if (id == R.id.nav_budget) {
                f = new BudgetLimitFragment();
            } else if (id == R.id.nav_add) {
                f = new AddTransactionFragment();
            } else if (id == R.id.nav_goals) {
                f = new GoalFragment();
            } else if (id == R.id.nav_profile) {
                f = new ProfileFragment();
            }
            
            if (f != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, f)
                        .commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_home);
        }
    }

    public void navigateToHome() {
        nav.setSelectedItemId(R.id.nav_home);
    }

    public void setSelectedNav(int id) {
        nav.setSelectedItemId(id);
    }

    public void navigateToNotificationSettings() {
        ProfileFragment profile = new ProfileFragment();
        Bundle args = new Bundle();
        args.putBoolean("scroll_to_notifications", true);
        profile.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, profile)
                .commit();
        nav.setSelectedItemId(R.id.nav_profile);
    }
}