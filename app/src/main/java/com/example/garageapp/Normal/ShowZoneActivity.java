package com.example.garageapp.Normal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.garageapp.Admin.Adapter.AdapterZone;
import com.example.garageapp.Admin.Parking.SlotActivity;
import com.example.garageapp.Model.Parking;
import com.example.garageapp.Model.Zone;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivityShowZoneBinding;
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

public class ShowZoneActivity extends AppCompatActivity implements AdapterZone.OnItemClickListener {

    ActivityShowZoneBinding binding;
    Parking parking;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refParking = database.getReference("Parking");
    List<Zone> zoneList;
    AdapterZone adapterZone;


    TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
            getZone(s.toString().toLowerCase());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowZoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parking = (Parking) getIntent().getSerializableExtra("Parking");

        init();
        getZone("");
    }

    void init() {
        zoneList = new ArrayList<>();
        adapterZone = new AdapterZone(this, zoneList, this);
        binding.zoneRecyclerView.setAdapter(adapterZone);
        binding.inputZoneName.addTextChangedListener(searchTextWatcher);
    }

    void getZone(String zoneName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        refParking.child(parking.getID()).child("Zone").orderByChild("zoneFloor").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                zoneList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Zone zone = snapshot.getValue(Zone.class);
                    if (zone != null) {
                        if (zone.getZoneName().toLowerCase().contains(zoneName))
                            zoneList.add(zone);
                    }
                }
                Collections.sort(zoneList, Comparator.comparingInt(Zone::getZoneFloor));
                binding.progressBar.setVisibility(View.GONE);
                adapterZone.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onItem_Click(int position) {
        Zone zone = zoneList.get(position);
        Intent intent = new Intent(this, ShowSlotActivity.class);
        intent.putExtra("Parking", parking);
        intent.putExtra("Zone", zone);
        startActivity(intent);
    }
}