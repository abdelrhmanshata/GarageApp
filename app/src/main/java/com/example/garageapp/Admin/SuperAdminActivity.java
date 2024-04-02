package com.example.garageapp.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.garageapp.Admin.Adapter.AdapterOrder;
import com.example.garageapp.Auth.LoginActivity;
import com.example.garageapp.Model.Order;
import com.example.garageapp.Model.Zone;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivitySuperAdminBinding;
import com.example.garageapp.databinding.ChangeOrderStatusDialogBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SuperAdminActivity extends AppCompatActivity implements AdapterOrder.OnItemClickListener {

    ActivitySuperAdminBinding binding;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refOrder = database.getReference("Orders");
    List<Order> orderList;
    AdapterOrder adapterOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuperAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
//        setOrder();
        getOrders();
        setOnClickListener();

    }

    void init() {
        orderList = new ArrayList<>();
        adapterOrder = new AdapterOrder(this, orderList, this);
        binding.ordersRecyclerView.setAdapter(adapterOrder);
    }

    void setOnClickListener() {
        binding.logout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    void getOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        refOrder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null) {
                        orderList.add(order);
                    }
                }

                Collections.sort(orderList, Comparator.comparing(Order::getDate).reversed());

                binding.progressBar.setVisibility(View.GONE);
                adapterOrder.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }





    @Override
    public void onItem_Click(int position) {
        Order order = orderList.get(position);
        showChangeOrderStatusLayout(order);
    }


    public void showChangeOrderStatusLayout(Order order) {
        androidx.appcompat.app.AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.change_order_status_dialog, null);
        dialogBuilder.setView(dialogView);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        ChangeOrderStatusDialogBinding statusDialogBinding = ChangeOrderStatusDialogBinding.bind(dialogView);

        statusDialogBinding.cancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        if (order.getStatus().equals("Received")) {
            statusDialogBinding.received.setChecked(true);
        } else if (order.getStatus().equals("Pending")) {
            statusDialogBinding.pending.setChecked(true);
        } else {
            statusDialogBinding.waiting.setChecked(true);
        }

        statusDialogBinding.radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.waiting) {
                order.setStatus("Waiting");
            } else if (checkedId == R.id.pending) {
                order.setStatus("Pending");
            } else if (checkedId == R.id.received) {
                order.setStatus("Received");
            }
            refOrder.child(order.getID()).setValue(order).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Update Status Successfully", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}