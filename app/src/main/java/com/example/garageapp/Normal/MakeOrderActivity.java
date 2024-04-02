package com.example.garageapp.Normal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;
import com.example.garageapp.Model.Order;
import com.example.garageapp.Model.User;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivityMakeOrderBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class MakeOrderActivity extends AppCompatActivity {

    ActivityMakeOrderBinding binding;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refUsers = database.getReference("Users");
    DatabaseReference refOrder = database.getReference("Orders");

    User objUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMakeOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getUserInfo();

        binding.cardForm
                .cardholderName(CardForm.FIELD_REQUIRED)
                .cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .setup(this);
        binding.cardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        binding.buttonOrder.setOnClickListener(v -> {
            orderSensor();
        });
    }

    void orderSensor() {
        // Hide the android keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);

        loading(true);

        String inputCardholderName = binding.cardForm.getCardholderName();
        if (inputCardholderName.isEmpty()) {
            Toasty.error(this, "Card holder name is required", Toast.LENGTH_SHORT).show();
            loading(false);
            return;
        }

        String inputCardNumber = binding.cardForm.getCardNumber();
        if (inputCardNumber.isEmpty()) {
            Toasty.error(this, "Card number is required", Toast.LENGTH_SHORT).show();
            loading(false);
            return;
        }

        String inputExpirationMonth = binding.cardForm.getExpirationMonth();
        if (inputExpirationMonth.isEmpty()) {
            Toasty.error(this, "Expiration month is required", Toast.LENGTH_SHORT).show();
            loading(false);
            return;
        }

        String inputExpirationYear = binding.cardForm.getExpirationYear();
        if (inputExpirationYear.isEmpty()) {
            Toasty.error(this, "Expiration year is required", Toast.LENGTH_SHORT).show();
            loading(false);
            return;
        }

        String inputCvv = binding.cardForm.getCvv();
        if (inputCvv.isEmpty()) {
            Toasty.error(this, "CVV is required", Toast.LENGTH_SHORT).show();
            loading(false);
            return;
        }

        String inputPhone = Objects.requireNonNull(binding.inputPhone.getText()).toString();
        if (inputPhone.isEmpty()) {
            binding.inputPhone.setError("Phone must be Required");
            binding.inputPhone.setFocusable(true);
            binding.inputPhone.requestFocus();
            loading(false);
            return;
        }

        String inputLocation = Objects.requireNonNull(binding.inputLocation.getText()).toString();
        if (inputLocation.isEmpty()) {
            binding.inputLocation.setError("Location must be Required");
            binding.inputLocation.setFocusable(true);
            binding.inputLocation.requestFocus();
            loading(false);
            return;
        }

        String inputLocationDescription = Objects.requireNonNull(binding.inputLocationDescription.getText()).toString();
        if (inputLocationDescription.isEmpty()) {
            binding.inputLocationDescription.setError("Location Description must be Required");
            binding.inputLocationDescription.setFocusable(true);
            binding.inputLocationDescription.requestFocus();
            loading(false);
            return;
        }

        // Save To Database

        Order order = new Order();
        order.setID(refOrder.push().getKey());
        order.setUserID(firebaseUser.getUid());
        order.setUserEmail(objUser.getuEmail());
        order.setUserName(objUser.getuFirstName());
        order.setDate(getDate());
        order.setStatus("Waiting");

        order.setCardholderName(inputCardholderName);
        order.setCardNumber(inputCardNumber);
        order.setExpirationMonth(inputExpirationMonth);
        order.setExpirationYear(inputExpirationYear);
        order.setCvv(inputCvv);

        order.setPhoneNumber(inputPhone);
        order.setLocation(inputLocation);
        order.setLocationDescription(inputLocationDescription);

        refOrder.child(order.getID()).setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                objUser.setOrderID(order.getID());
                refUsers.child(objUser.getuID()).setValue(objUser);
                Toasty.success(this, "Done", Toast.LENGTH_SHORT).show();
                loading(false);
                onBackPressed();
            }
        });
    }

    void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonOrder.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonOrder.setVisibility(View.VISIBLE);
        }
    }

    void getUserInfo() {
        refUsers.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    objUser = user;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    public String getDate() {
        Date currentDate = new Date(Long.parseLong(String.valueOf(System.currentTimeMillis())));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(currentDate);
    }
}