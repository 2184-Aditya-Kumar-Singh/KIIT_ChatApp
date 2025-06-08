package com.example.kiit_chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.fragments.AdminPanelFragment;
import com.example.kiit_chatapp.fragments.GroupsFragment;
import com.example.kiit_chatapp.fragments.PrivateChatsFragment;
import com.example.kiit_chatapp.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private boolean isAdmin = false;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CHECK IF USER IS LOGGED IN
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);

        if ("22052184@kiit.ac.in".equalsIgnoreCase(currentUser.getEmail())) {
            isAdmin = true;
        }

        // Set admin menu item visibility depending on isAdmin
        Menu menu = bottomNav.getMenu();
        MenuItem adminMenuItem = menu.findItem(R.id.nav_admin);
        if (adminMenuItem != null) {
            adminMenuItem.setVisible(isAdmin);
        }

        loadFragment(new GroupsFragment());

        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment;

        int id = item.getItemId();
        if (id == R.id.nav_groups) {
            selectedFragment = new GroupsFragment();
        } else if (id == R.id.nav_private) {
            selectedFragment = new PrivateChatsFragment();
        } else if (id == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (id == R.id.nav_admin && isAdmin) {
            selectedFragment = new AdminPanelFragment();
        } else {
            return false;
        }

        loadFragment(selectedFragment);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}