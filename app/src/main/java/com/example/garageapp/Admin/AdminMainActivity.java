package com.example.garageapp.Admin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.example.garageapp.Fragment.HomeFragment;
import com.example.garageapp.Fragment.ProfileFragment;
import com.example.garageapp.Fragment.ReservationFragment;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivityAdminMainBinding;

public class AdminMainActivity extends AppCompatActivity {

    ActivityAdminMainBinding binding;

    HomeFragment homeFragment = new HomeFragment();
    ReservationFragment reservationFragment = new ReservationFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getHomePage();
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.adminHome) {
                getHomePage();
            } else if (item.getItemId() == R.id.adminReservation) {
                getReservationPage();
            } else if (item.getItemId() == R.id.adminProfile) {
                getProfilePage();
            } else {
                getHomePage();
            }
            return true;
        });
    }


    void getHomePage() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
    }

    void getReservationPage() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, reservationFragment).commit();
    }

    void getProfilePage() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commit();
    }
}