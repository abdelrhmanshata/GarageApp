package com.example.garageapp.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.garageapp.Model.Order;
import com.example.garageapp.Model.Reserve;
import com.example.garageapp.Model.User;
import com.example.garageapp.Normal.MakeOrderActivity;
import com.example.garageapp.R;
import com.example.garageapp.databinding.FragmentNormalHomeBinding;
import com.example.garageapp.databinding.FragmentNormalOrderBinding;
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
import java.util.Locale;

public class NormalOrderFragment extends Fragment {

    FragmentNormalOrderBinding binding;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refUsers = database.getReference("Users");
    DatabaseReference refOrder = database.getReference("Orders");
    User objUser;

    public NormalOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNormalOrderBinding.inflate(inflater, container, false);

        getUserInfo();

        binding.btnOrder.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), MakeOrderActivity.class));
        });
        return binding.getRoot();
    }

    void getUserInfo() {
        refUsers.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    objUser = user;
                    if (!objUser.getOrderID().isEmpty()) {
                        binding.orderLayout.setVisibility(View.VISIBLE);
                        binding.btnOrder.setVisibility(View.GONE);
                        getOrder(user.getOrderID());
                    } else {
                        binding.orderLayout.setVisibility(View.GONE);
                        binding.btnOrder.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void getOrder(String orderID) {
        refOrder.child(orderID).addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SimpleDateFormat", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Order order = dataSnapshot.getValue(Order.class);
                if (order != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = null;
                    try {
                        date = dateFormat.parse(order.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                    binding.year.setText(yearFormat.format(date));
                    binding.day.setText(dayFormat.format(date));
                    binding.month.setText(monthFormat.format(date));
                    binding.userName.setText(order.getUserName());
                    binding.userEmail.setText(order.getUserEmail());
                    binding.userPhone.setText(order.getPhoneNumber());
                    binding.userLocation.setText(order.getLocation());
                    binding.orderStatus.setText(order.getStatus());
                    int color;
                    if (order.getStatus().equals("Received")) {
                        color = Color.parseColor("#008000"); // Example color
                    } else if (order.getStatus().equals("Pending")) {
                        color = Color.parseColor("#B00020");
                    } else {
                        color = Color.parseColor("#757575");
                    }
                    ColorStateList colorStateList = ColorStateList.valueOf(color);
                    binding.orderStatus.setBackgroundTintList(colorStateList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}