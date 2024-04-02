package com.example.garageapp.Fragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.garageapp.Auth.LoginActivity;
import com.example.garageapp.Model.User;
import com.example.garageapp.R;
import com.example.garageapp.databinding.UpdatePasswordBinding;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.garageapp.databinding.FragmentProfileBinding;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refUsers = database.getReference("Users");
    User objUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        getUserData();
        setOnClickListener();

        return binding.getRoot();
    }


    void setOnClickListener() {
        binding.btnUpdate.setOnClickListener(v -> {
            loading(true);
            String inputFirstName = Objects.requireNonNull(binding.inputFirstName.getText()).toString().trim();
            if (inputFirstName.isEmpty()) {
                binding.inputFirstName.setError(getString(R.string.first_name_is_required));
                binding.inputFirstName.requestFocus();
                loading(false);
                return;
            }
            String inputLastName = Objects.requireNonNull(binding.inputLastName.getText()).toString().trim();
            if (inputLastName.isEmpty()) {
                binding.inputLastName.setError(getString(R.string.last_name_is_required));
                binding.inputLastName.requestFocus();
                loading(false);
                return;
            }

            objUser.setuFirstName(inputFirstName);
            objUser.setuLastName(inputLastName);

            updateUser(objUser);
        });
        binding.updatePassword.setOnClickListener(v -> {
            showUpdatePasswordLayout();
        });
        binding.logout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finishAffinity();
        });
    }


    void getUserData() {
        loading(true);
        refUsers.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    objUser = user;
                    binding.inputEmail.setText(user.getuEmail());
                    binding.inputFirstName.setText(user.getuFirstName());
                    binding.inputLastName.setText(user.getuLastName());
                }
                loading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loading(false);
            }
        });
    }

    public void showUpdatePasswordLayout() {
        androidx.appcompat.app.AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.update_password, null);
        dialogBuilder.setView(dialogView);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        UpdatePasswordBinding passwordBinding = UpdatePasswordBinding.bind(dialogView);

        passwordBinding.btnUpdate.setOnClickListener(v -> {

            String currentPassword = Objects.requireNonNull(passwordBinding.currentPassword.getText()).toString().trim();
            if (currentPassword.isEmpty()) {
                passwordBinding.currentPassword.setError(getString(R.string.passwordIsRequired));
                passwordBinding.currentPassword.requestFocus();
                return;
            }

            String newPassword = Objects.requireNonNull(passwordBinding.newPassword.getText()).toString().trim();
            if (newPassword.isEmpty()) {
                passwordBinding.newPassword.setError(getString(R.string.passwordIsRequired));
                passwordBinding.newPassword.requestFocus();
                return;
            }

            if (newPassword.length() < 8) {
                passwordBinding.newPassword.setError(getString(R.string.minimumLength));
                passwordBinding.newPassword.requestFocus();
                return;
            }

            String confirmPassword = Objects.requireNonNull(passwordBinding.confirmPassword.getText()).toString().trim();
            if (!confirmPassword.equals(newPassword)) {
                passwordBinding.confirmPassword.setError("Password Not Match");
                passwordBinding.confirmPassword.setFocusable(true);
                passwordBinding.confirmPassword.requestFocus();
                return;
            }

            if (!currentPassword.equals(objUser.getuPassword())) {
                passwordBinding.currentPassword.setError("Password Is Wrong");
                passwordBinding.currentPassword.requestFocus();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(objUser.getuEmail(), currentPassword);
            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                currentUser.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        objUser.setuPassword(newPassword);
                        updateUser(objUser);
                        alertDialog.dismiss();
                    } else {
                        try {
                            throw task1.getException();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        alertDialog.dismiss();
                    }
                });
            });
        });
    }

    void updateUser(User user) {
        refUsers.child(user.getuID()).setValue(user).addOnSuccessListener(unused -> {
            loading(false);
            Toasty.success(getContext(), "Update Successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            loading(false);
            if (e instanceof FirebaseNetworkException) {
                Toast.makeText(getContext(), getString(R.string.noConnection), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Exception -> " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnUpdate.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnUpdate.setVisibility(View.VISIBLE);
        }
    }
}