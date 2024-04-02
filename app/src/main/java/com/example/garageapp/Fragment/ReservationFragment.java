package com.example.garageapp.Fragment;


import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.garageapp.Admin.Adapter.AdapterReservation;

import com.example.garageapp.Model.Order;
import com.example.garageapp.Model.Reserve;
import com.example.garageapp.R;

import com.example.garageapp.databinding.CheckoutDialogBinding;

import com.example.garageapp.databinding.FragmentReservationBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import es.dmoral.toasty.Toasty;

public class ReservationFragment extends Fragment implements AdapterReservation.OnItemClickListener {

    FragmentReservationBinding binding;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refReservation = database.getReference("Reservation");
    DatabaseReference refParking = database.getReference("Parking");
    DatabaseReference refUsers = database.getReference("Users");
    List<Reserve> reserveList;
    AdapterReservation adapterReservation;


    TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
            getReservations(s.toString().toLowerCase());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public ReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReservationBinding.inflate(inflater, container, false);

        init();
        getReservations("");
        setOnClickListener();
        return binding.getRoot();
    }

    void init() {
        reserveList = new ArrayList<>();
        adapterReservation = new AdapterReservation(getContext(), reserveList, this);
        binding.reservationRecyclerView.setAdapter(adapterReservation);
        binding.inputUserName.addTextChangedListener(searchTextWatcher);
    }

    void setOnClickListener() {
        binding.scannerQRCode.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Scanner QR", Toast.LENGTH_SHORT).show();
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setOrientationLocked(true);
            integrator.setPrompt("Scan a QR code");
            integrator.initiateScan();
        });

    }

    void getReservations(String userName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        refReservation.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reserveList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Reserve reserve = snapshot.getValue(Reserve.class);
                    if (reserve != null) {
                        if (reserve.getAdminID().equals(currentUser.getUid())) {
                            if (userName.isEmpty()) {
                                reserveList.add(reserve);
                            } else {
                                if ((reserve.getUserName().toLowerCase().contains(userName)) || reserve.getID().toLowerCase().contains(userName)) {
                                    reserveList.add(reserve);
                                }
                            }
                        }
                    }
                }
                reserveList.sort(Comparator.comparing(Reserve::getStartTime).reversed());
                binding.progressBar.setVisibility(View.GONE);
                adapterReservation.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onItem_Click(int position) {
        Reserve reserve = reserveList.get(position);
        showDialogCheckOutLayout(reserve);
    }

    @SuppressLint("SetTextI18n")
    public void showDialogCheckOutLayout(Reserve reserve) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.checkout_dialog, null);
        dialogBuilder.setView(dialogView);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        CheckoutDialogBinding checkoutDialogBinding = CheckoutDialogBinding.bind(dialogView);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = dateFormat.parse(reserve.getDate(reserve.getStartTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        checkoutDialogBinding.year.setText(yearFormat.format(date));
        checkoutDialogBinding.day.setText(dayFormat.format(date));
        checkoutDialogBinding.month.setText(monthFormat.format(date));
        //
        checkoutDialogBinding.userName.setText(reserve.getUserName());
        checkoutDialogBinding.userEmail.setText(reserve.getUserEmail());
        //
        checkoutDialogBinding.startTime.setText("Start : " + reserve.getTime(reserve.getStartTime()));
        checkoutDialogBinding.endTime.setText("End   : " + (reserve.getEndTime().isEmpty() ? " --:-- " : reserve.getTime(reserve.getEndTime())));
        //
        checkoutDialogBinding.parking.setText(reserve.getParking());
        checkoutDialogBinding.zone.setText(reserve.getZone());
        checkoutDialogBinding.slot.setText(reserve.getSlot());
        //

        if (!reserve.getEndTime().isEmpty()) {
            checkoutDialogBinding.btnCheckOutLayout.setVisibility(View.GONE);
            checkoutDialogBinding.deleteCheckOut.setVisibility(View.VISIBLE);
            checkoutDialogBinding.checkOutLayout.setVisibility(View.VISIBLE);

            checkoutDialogBinding.timeTaken.setText(reserve.getBetweenTime(reserve.getEndTime(), reserve.getStartTime()) + " h");
            checkoutDialogBinding.hourPrice.setText(reserve.getSlotPrice() + " SAR");
            double totalPrice = reserve.getBetweenTime(reserve.getEndTime(), reserve.getStartTime()) * reserve.getSlotPrice();
            checkoutDialogBinding.totalPrice.setText(totalPrice + " SAR");
        } else {
            checkoutDialogBinding.btnCheckOutLayout.setVisibility(View.VISIBLE);
            checkoutDialogBinding.deleteCheckOut.setVisibility(View.GONE);
        }

        checkoutDialogBinding.btnCheckOut.setOnClickListener(v -> {
            reserve.setEndTime(String.valueOf(System.currentTimeMillis()));
            refReservation.child(reserve.getID()).setValue(reserve);

            checkoutDialogBinding.btnCheckOut.setVisibility(View.GONE);
            checkoutDialogBinding.progressBar.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> {
                checkoutDialogBinding.checkOutLayout.setVisibility(View.VISIBLE);
                checkoutDialogBinding.btnCheckOut.setVisibility(View.VISIBLE);
                checkoutDialogBinding.progressBar.setVisibility(View.GONE);
                checkoutDialogBinding.btnCheckOutLayout.setVisibility(View.GONE);
                checkoutDialogBinding.deleteCheckOut.setVisibility(View.VISIBLE);

                checkoutDialogBinding.endTime.setText("End   : " + reserve.getTime(reserve.getEndTime()));

                checkoutDialogBinding.timeTaken.setText(reserve.getBetweenTime(reserve.getEndTime(), reserve.getStartTime()) + " h");
                checkoutDialogBinding.hourPrice.setText(reserve.getSlotPrice() + " SAR");
                double totalPrice = reserve.getBetweenTime(reserve.getEndTime(), reserve.getStartTime()) * reserve.getSlotPrice();
                checkoutDialogBinding.totalPrice.setText(totalPrice + " SAR");

                // reset
                refUsers.child(reserve.getUserID()).child("reservationID").setValue("");
                refParking.child(reserve.getParkingID()).child("Zone").child(reserve.getZoneID()).child("Slot").child(reserve.getSlotID()).child("reserved").setValue(false);
                refParking.child(reserve.getParkingID()).child("Zone").child(reserve.getZoneID()).child("Slot").child(reserve.getSlotID()).child("userID").setValue("");
            }, 500);
        });

        checkoutDialogBinding.deleteCheckOut.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Delete", Toast.LENGTH_SHORT).show();
            refReservation.child(reserve.getID()).removeValue();
            alertDialog.dismiss();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                String scannedData = result.getContents();
                binding.inputUserName.setText(scannedData);
            } else {
                Toasty.error(getContext(), "Cancel", Toast.LENGTH_SHORT).show();
            }
        }
    }

}