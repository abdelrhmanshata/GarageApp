package com.example.garageapp.Admin.Parking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.garageapp.Admin.Adapter.AdapterSlot;
import com.example.garageapp.Admin.Adapter.AdapterZone;
import com.example.garageapp.Model.Parking;
import com.example.garageapp.Model.Slot;
import com.example.garageapp.Model.Zone;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivitySlotBinding;
import com.example.garageapp.databinding.ActivityZoneBinding;
import com.example.garageapp.databinding.AddSlotDialogBinding;
import com.example.garageapp.databinding.AddZoneDialogBinding;
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
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class SlotActivity extends AppCompatActivity {

    ActivitySlotBinding binding;
    Zone zone;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refParking = database.getReference("Parking");
    List<Slot> slotList;
    AdapterSlot adapterSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        zone = (Zone) getIntent().getSerializableExtra("Zone");

        init();
        setOnClickListener();
        getSlot();
    }

    void init() {
        slotList = new ArrayList<>();
        adapterSlot = new AdapterSlot(this, slotList);
        binding.slotGridView.setAdapter(adapterSlot);
    }

    void setOnClickListener() {
        binding.addNewSlot.setOnClickListener(v -> {
            showDialogAddSlotLayout(null);
        });

        binding.slotGridView.setOnItemClickListener((parent, view, position, id) -> {
            Slot slot = slotList.get(position);
            showDialogAddSlotLayout(slot);
        });


    }

    void getSlot() {
        binding.progressBar.setVisibility(View.VISIBLE);
        refParking.child(zone.getParkingID()).child("Zone").child(zone.getID()).child("Slot").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                slotList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Slot slot = snapshot.getValue(Slot.class);
                    if (slot != null) {
                        slotList.add(slot);
                    }
                }
                binding.progressBar.setVisibility(View.GONE);
                adapterSlot.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void showDialogAddSlotLayout(Slot currentSlot) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_slot_dialog, null);
        dialogBuilder.setView(dialogView);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        AddSlotDialogBinding addSlotDialogBinding = AddSlotDialogBinding.bind(dialogView);
        addSlotDialogBinding.title.setText(currentSlot == null ? "Add New Slot" : "Update Slot");
        Slot slot;
        if (currentSlot == null) {
            slot = new Slot();
            slot.setOwnerID(firebaseUser.getUid());
            slot.setParkingID(zone.getParkingID());
            slot.setZoneID(zone.getID());
            slot.setID(refParking.push().getKey());
            slot.setUserID("");
        } else {
            slot = currentSlot;
            addSlotDialogBinding.slotIsPrivate.setChecked(slot.isPrivate());
            addSlotDialogBinding.slotIsReserved.setChecked(slot.isReserved());
        }

        addSlotDialogBinding.btnSave.setOnClickListener(v -> {
            addSlotDialogBinding.btnSave.setVisibility(View.GONE);
            addSlotDialogBinding.progressBar.setVisibility(View.VISIBLE);
            slot.setPrivate(addSlotDialogBinding.slotIsPrivate.isChecked());
            slot.setReserved(addSlotDialogBinding.slotIsReserved.isChecked());
            slot.setPrice(slot.isPrivate() ? 30.0 : 20.0);
            refParking.child(zone.getParkingID()).child("Zone").child(zone.getID()).child("Slot").child(slot.getID()).setValue(slot).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toasty.success(this, "Save Slot Successfully", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            });
        });
    }

}