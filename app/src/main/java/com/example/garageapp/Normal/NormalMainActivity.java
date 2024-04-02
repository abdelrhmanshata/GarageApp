package com.example.garageapp.Normal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.garageapp.Fragment.NormalOrderFragment;
import com.example.garageapp.Fragment.ProfileFragment;
import com.example.garageapp.Fragment.NormalHomeFragment;
import com.example.garageapp.Fragment.NormalReservationFragment;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivityNormalMainBinding;

public class NormalMainActivity extends AppCompatActivity {

    ActivityNormalMainBinding binding;
    NormalHomeFragment homeFragment = new NormalHomeFragment();
    NormalReservationFragment reservationFragment = new NormalReservationFragment();
    NormalOrderFragment orderFragment = new NormalOrderFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNormalMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getHomePage();
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.normalHome) {
                getHomePage();
            } else if (item.getItemId() == R.id.normalReservation) {
                getReservationPage();
            } else if (item.getItemId() == R.id.normalOrder) {
                getOrderPage();
            } else if (item.getItemId() == R.id.normalProfile) {
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

    void getOrderPage() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, orderFragment).commit();
    }

    void getProfilePage() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commit();
    }
}