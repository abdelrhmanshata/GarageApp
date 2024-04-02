package com.example.garageapp.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.garageapp.Model.User;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivityLoginBinding;
import com.example.garageapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refUsers = database.getReference("Users");

    String UserType = "Admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getUserType();
        onClickListener();
    }

    void onClickListener() {
        binding.loginText.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.buttonRegister.setOnClickListener(v -> {
            validationInput();
        });
    }


    void validationInput() {
        loading(true);

        String inputFirstName = Objects.requireNonNull(binding.inputFirstName.getText()).toString().trim();
        if (inputFirstName.isEmpty()) {
            binding.inputFirstName.setError(getString(R.string.first_name_is_required));
            binding.inputFirstName.requestFocus();
            binding.progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        String inputLastName = Objects.requireNonNull(binding.inputLastName.getText()).toString().trim();
        if (inputLastName.isEmpty()) {
            binding.inputLastName.setError(getString(R.string.last_name_is_required));
            binding.inputLastName.requestFocus();
            loading(false);
            return;
        }

        String inputEmail = Objects.requireNonNull(binding.inputEmail.getText()).toString().trim();
        if (inputEmail.isEmpty()) {
            binding.inputEmail.setError(getString(R.string.email_is_required));
            binding.inputEmail.requestFocus();
            loading(false);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
            binding.inputEmail.setError(getString(R.string.please_enter_valid_email));
            binding.inputEmail.requestFocus();
            loading(false);
            return;
        }

        String inputPassword = Objects.requireNonNull(binding.inputPassword.getText()).toString().trim();
        if (inputPassword.isEmpty()) {
            binding.inputPassword.setError(getString(R.string.password_is_required));
            binding.inputPassword.requestFocus();
            loading(false);
            return;
        }
        if (inputPassword.length() < 8) {
            binding.inputPassword.setError(getString(R.string.minimumLength));
            binding.inputPassword.requestFocus();
            loading(false);
            return;
        }

        if (inputPassword.contains(" ")) {
            binding.inputPassword.setError(getString(R.string.the_password_must_not_contain_white_space));
            binding.inputPassword.setFocusable(true);
            binding.inputPassword.requestFocus();
            loading(false);
            return;
        }

        String confirmPassword = Objects.requireNonNull(binding.inputConfirmationPassword.getText()).toString().trim();
        if (!confirmPassword.equals(inputPassword)) {
            binding.inputConfirmationPassword.setError(getString(R.string.password_doesn_t_match));
            binding.inputConfirmationPassword.setFocusable(true);
            binding.inputConfirmationPassword.requestFocus();
            loading(false);
            return;
        }

        //
        User user = new User();
        user.setuFirstName(inputFirstName);
        user.setuLastName(inputLastName);
        user.setuEmail(inputEmail);
        user.setuPassword(inputPassword);
        user.setuUserType(UserType);

        Register(user);
    }

    public void Register(User user) {
        firebaseAuth.createUserWithEmailAndPassword(user.getuEmail(), user.getuPassword()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser CurrentUser = firebaseAuth.getCurrentUser();
                user.setuID(CurrentUser.getUid());

                refUsers.child(user.getuID()).setValue(user).addOnCompleteListener(task1 -> {

                    if (task1.isSuccessful()) {
                        loading(false);
                        Toast.makeText(RegisterActivity.this, getString(R.string.successRegistered), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finishAffinity();
                    }
                }).addOnFailureListener(e -> {
                    loading(false);
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, getString(R.string.AlreadyRegistered), Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FirebaseNetworkException) {
                        Toast.makeText(this, getString(R.string.noConnection), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Exception -> " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(e -> {
            loading(false);
            if (e instanceof FirebaseAuthUserCollisionException) {
                Toast.makeText(this, getString(R.string.AlreadyRegistered), Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseNetworkException) {
                Toast.makeText(this, getString(R.string.noConnection), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Exception -> " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void getUserType() {
        binding.radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.adminType) {
                UserType = "Admin";
            } else {
                UserType = "Normal";
            }
        });
    }

    void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonRegister.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonRegister.setVisibility(View.VISIBLE);
        }
    }
}