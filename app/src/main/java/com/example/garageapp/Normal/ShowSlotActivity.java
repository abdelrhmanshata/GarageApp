package com.example.garageapp.Normal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.garageapp.Admin.Adapter.AdapterSlot;
import com.example.garageapp.Model.Parking;
import com.example.garageapp.Model.Reserve;
import com.example.garageapp.Model.Slot;
import com.example.garageapp.Model.User;
import com.example.garageapp.Model.Zone;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivityShowSlotBinding;
import com.example.garageapp.databinding.AddSlotDialogBinding;
import com.example.garageapp.databinding.ReservationSlotDialogBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ShowSlotActivity extends AppCompatActivity {
    ActivityShowSlotBinding binding;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refParking = database.getReference("Parking");
    DatabaseReference refReservation = database.getReference("Reservation");
    DatabaseReference refUsers = database.getReference("Users");
    List<Slot> slotList;
    AdapterSlot adapterSlot;
    Parking parking;
    Zone zone;

    User objUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowSlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parking = (Parking) getIntent().getSerializableExtra("Parking");
        zone = (Zone) getIntent().getSerializableExtra("Zone");

        init();
        getUserInfo();
        setOnClickListener();
        getSlot();
    }

    void init() {
        slotList = new ArrayList<>();
        adapterSlot = new AdapterSlot(this, slotList);
        binding.slotGridView.setAdapter(adapterSlot);
    }

    void setOnClickListener() {
        binding.slotGridView.setOnItemClickListener((parent, view, position, id) -> {
            Slot slot = slotList.get(position);
            if (slot.isPrivate()) {
                Toasty.warning(this, "This Slot Is Private", Toast.LENGTH_SHORT).show();
                return;
            }

            if (slot.isReserved()) {
                if (slot.getUserID().equals(firebaseUser.getUid())) {
                    showDialogReservationSlotLayout(slot, position);
                } else {
                    Toasty.info(this, "This Slot Is Reserved", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (objUser.getReservationID().isEmpty()) {
                showDialogReservationSlotLayout(slot, position);
            } else {
                Toasty.normal(this, "You have already reserved a slot", Toast.LENGTH_SHORT).show();
            }
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

    public void showDialogReservationSlotLayout(Slot currentSlot, int slotIndex) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.reservation_slot_dialog, null);
        dialogBuilder.setView(dialogView);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        ReservationSlotDialogBinding reservationSlotDialogBinding = ReservationSlotDialogBinding.bind(dialogView);
        reservationSlotDialogBinding.slotPrice.setText("Slot Price " + currentSlot.getPrice() + " SAR/Hour");


        if (currentSlot.getUserID().equals(firebaseUser.getUid())) {
            reservationSlotDialogBinding.btnReservation.setVisibility(View.GONE);
            reservationSlotDialogBinding.btnCancel.setVisibility(View.VISIBLE);
        } else {
            reservationSlotDialogBinding.btnReservation.setVisibility(View.VISIBLE);
            reservationSlotDialogBinding.btnCancel.setVisibility(View.GONE);
        }


        reservationSlotDialogBinding.btnReservation.setOnClickListener(v -> {
            currentSlot.setUserID(firebaseUser.getUid());
            currentSlot.setReserved(true);

            refParking.child(currentSlot.getParkingID()).child("Zone").child(currentSlot.getZoneID()).child("Slot").child(currentSlot.getID()).setValue(currentSlot).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Reserve reserve = new Reserve();
                    reserve.setID(refReservation.push().getKey());
                    reserve.setAdminID(currentSlot.getOwnerID());
                    reserve.setUserID(firebaseUser.getUid());
                    reserve.setUserName(objUser.getuFirstName());
                    reserve.setUserEmail(objUser.getuEmail());
                    reserve.setParkingID(currentSlot.getParkingID());
                    reserve.setZoneID(currentSlot.getZoneID());
                    reserve.setSlotID(currentSlot.getID());

                    reserve.setParking(parking.getParkingName());
                    reserve.setZone(zone.getZoneName());
                    reserve.setSlot("Slot - " + (slotIndex + 1));

                    reserve.setStartTime(String.valueOf(System.currentTimeMillis()));
                    reserve.setEndTime("");
                    reserve.setSlotPrice(currentSlot.getPrice());
                    refReservation.child(reserve.getID()).setValue(reserve).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            objUser.setReservationID(reserve.getID());
                            refUsers.child(objUser.getuID()).setValue(objUser);
                            Toasty.success(this, "Reservation Slot Successfully", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    });
                }
            });
        });

        reservationSlotDialogBinding.btnCancel.setOnClickListener(v -> {
            currentSlot.setUserID("");
            currentSlot.setReserved(false);
            refParking.child(currentSlot.getParkingID()).child("Zone").child(currentSlot.getZoneID()).child("Slot").child(currentSlot.getID())
                    .setValue(currentSlot).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            refReservation.child(objUser.getReservationID()).removeValue();
                            objUser.setReservationID("");
                            refUsers.child(objUser.getuID()).setValue(objUser);
                            Toasty.success(this, "Cancel Reservation Successfully", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    });
        });
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
}