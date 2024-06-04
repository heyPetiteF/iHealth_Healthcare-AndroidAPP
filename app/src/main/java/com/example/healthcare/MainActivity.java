package com.example.healthcare;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.healthcare.ui.browse.BrowseFragment;
import com.example.healthcare.ui.notification.NotificationFragment;
import com.example.healthcare.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_browse, R.id.navigation_notification)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    public void resetAllFragments() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.navigation_home);

        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if (homeFragment != null) {
            homeFragment.resetToInitialState();
        }

        BrowseFragment browseFragment = (BrowseFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if (browseFragment != null) {
            browseFragment.resetToInitialState();
        }

        NotificationFragment notificationFragment = (NotificationFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if (notificationFragment != null) {
            notificationFragment.resetToInitialState();
        }
    }
}
