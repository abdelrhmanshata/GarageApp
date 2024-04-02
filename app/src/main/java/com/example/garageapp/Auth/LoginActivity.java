package com.example.garageapp.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.garageapp.Admin.AdminMainActivity;
import com.example.garageapp.Admin.SuperAdminActivity;
import com.example.garageapp.Model.Parking;
import com.example.garageapp.Model.User;
import com.example.garageapp.Normal.NormalMainActivity;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivityLoginBinding;
import com.example.garageapp.databinding.ForgetPasswordLayoutBinding;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refUsers = database.getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        onClickListener();

    }


    void onClickListener() {
        binding.registerText.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        binding.buttonLogin.setOnClickListener(v -> {
            validationInput();
        });
        binding.forgetPassword.setOnClickListener(v -> {
            showDialogForgetPasswordLayout();
        });
    }


    void validationInput() {
        loading(true);

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


        Login(inputEmail, inputPassword);
    }

    void Login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser = firebaseAuth.getCurrentUser();
                refUsers.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getuUserType().equals("SuperAdmin")) {
                                startActivity(new Intent(LoginActivity.this, SuperAdminActivity.class));
                            } else if (user.getuUserType().equals("Admin")) {
                                startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, NormalMainActivity.class));
                            }
                            ActivityCompat.finishAffinity(LoginActivity.this);
                            Toast.makeText(LoginActivity.this, getString(R.string.successLogin), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(LoginActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                loading(false);
            } else if (task.getException() instanceof FirebaseNetworkException) {
                Toast.makeText(this, getString(R.string.noConnection), Toast.LENGTH_SHORT).show();
            } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                Toast.makeText(this, getString(R.string.userNotFound), Toast.LENGTH_SHORT).show();
            } else if ((task.getException() instanceof FirebaseAuthInvalidCredentialsException)) {
                Toast.makeText(this, getString(R.string.passwordIncorrect), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error+->" + task.getException(), Toast.LENGTH_SHORT).show();
            }
            loading(false);
        });
    }

    void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonLogin.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonLogin.setVisibility(View.VISIBLE);
        }
    }


    public void showDialogForgetPasswordLayout() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.forget_password_layout, null);
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        ForgetPasswordLayoutBinding forgetPasswordBinding = ForgetPasswordLayoutBinding.bind(dialogView);

        forgetPasswordBinding.buttonCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        forgetPasswordBinding.buttonRecover.setOnClickListener(v -> {
            forgetPasswordBinding.progressCircle.setVisibility(View.VISIBLE);
            String inputEmail = Objects.requireNonNull(forgetPasswordBinding.inputRecoverEmail.getText()).toString().trim();
            if (inputEmail.isEmpty()) {
                forgetPasswordBinding.inputRecoverEmail.setError(getString(R.string.email_is_required));
                forgetPasswordBinding.inputRecoverEmail.requestFocus();
                forgetPasswordBinding.progressCircle.setVisibility(View.INVISIBLE);
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
                forgetPasswordBinding.inputRecoverEmail.setError(getString(R.string.please_enter_valid_email));
                forgetPasswordBinding.inputRecoverEmail.requestFocus();
                forgetPasswordBinding.progressCircle.setVisibility(View.INVISIBLE);
                return;
            }

            refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getuEmail().equals(inputEmail)) {
                                firebaseAuth.sendPasswordResetEmail(inputEmail).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toasty.info(LoginActivity.this, "Check Your Email : " + inputEmail, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toasty.error(LoginActivity.this, "Falid ..." + Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                    }
                                    forgetPasswordBinding.progressCircle.setVisibility(View.INVISIBLE);
                                    alertDialog.dismiss();
                                }).addOnFailureListener(e -> {
                                    Toasty.error(LoginActivity.this, "Error ->" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    forgetPasswordBinding.progressCircle.setVisibility(View.INVISIBLE);
                                    alertDialog.dismiss();
                                });
                                forgetPasswordBinding.progressCircle.setVisibility(View.INVISIBLE);
                                return;
                            }
                        }
                    }
                    Toasty.error(LoginActivity.this, "This Email Not Found", Toast.LENGTH_SHORT).show();
                    forgetPasswordBinding.progressCircle.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        });
    }

}