package com.mobile.uph24si3;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.mobile.uph24si3.fragment.HomeFragment;
import com.mobile.uph24si3.fragment.ProfileFragment;
import com.mobile.uph24si3.fragment.SplitBillFragment;
import com.mobile.uph24si3.fragment.TransactionFragment;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FragmentManager fragmentManager;

    private HomeFragment homeFragment;
    private TransactionFragment transactionFragment;
    private SplitBillFragment splitBillFragment;
    private ProfileFragment profileFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNav = findViewById(R.id.bottomNavigationView);
        fragmentManager = getSupportFragmentManager();

        // Initialize fragments
        homeFragment = new HomeFragment();
        transactionFragment = new TransactionFragment();
        splitBillFragment = new SplitBillFragment();
        profileFragment = new ProfileFragment();

        // Add all fragments
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.fragmentContainer, homeFragment, "home");
        ft.add(R.id.fragmentContainer, transactionFragment, "transaction").hide(transactionFragment);
        ft.add(R.id.fragmentContainer, splitBillFragment, "split").hide(splitBillFragment);
        ft.add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment);
        ft.commit();

        activeFragment = homeFragment;

        // Bottom navigation listener
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) selected = homeFragment;
            else if (id == R.id.nav_transaction) selected = transactionFragment;
            else if (id == R.id.nav_split) selected = splitBillFragment;
            else if (id == R.id.nav_profile) selected = profileFragment;

            if (selected != null && selected != activeFragment) {
                fragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(selected)
                        .commit();
                activeFragment = selected;
            }
            return true;
        });
    }

    public void navigateTo(int navItemId) {
        bottomNav.setSelectedItemId(navItemId);
    }

    /** Switch to home tab and trigger SMS auto-read */
    public void switchToHome() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        homeFragment.requestSmsAutoRead();
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh home fragment data when returning to activity
        if (activeFragment instanceof HomeFragment) {
            ((HomeFragment) activeFragment).refreshData();
        }
    }
}
