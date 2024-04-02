package com.example.garageapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.garageapp.Admin.AdminMainActivity;
import com.example.garageapp.Admin.SuperAdminActivity;
import com.example.garageapp.Auth.LoginActivity;
import com.example.garageapp.Model.User;
import com.example.garageapp.Normal.NormalMainActivity;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivityIntroBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class IntroActivity extends AppCompatActivity {

    ActivityIntroBinding binding;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refUsers = database.getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (isFirstTime().get("FirstTime")) {
            binding.goBtn.setOnClickListener(v -> {
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                setFirstTime(false);
            });
        } else {
            if (currentUser != null) {
                openPage();
            } else {
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            }
        }
    }

    void openPage() {
        refUsers.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    if (user.getuUserType().equals("SuperAdmin")) {
                        startActivity(new Intent(IntroActivity.this, SuperAdminActivity.class));
                    } else if (user.getuUserType().equals("Admin")) {
                        startActivity(new Intent(IntroActivity.this, AdminMainActivity.class));
                    } else {
                        startActivity(new Intent(IntroActivity.this, NormalMainActivity.class));
                    }
                    ActivityCompat.finishAffinity(IntroActivity.this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(IntroActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public HashMap<String, Boolean> isFirstTime() {
        SharedPreferences sp = getSharedPreferences("FirstTime", Activity.MODE_PRIVATE);
        HashMap<String, Boolean> UserInfo = new HashMap<>();
        UserInfo.put("FirstTime", sp.getBoolean("FirstTime", true));
        return UserInfo;
    }

    public void setFirstTime(boolean FirstTime) {
        SharedPreferences sp = this.getSharedPreferences("FirstTime", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("FirstTime", FirstTime);
        editor.apply();
    }
}