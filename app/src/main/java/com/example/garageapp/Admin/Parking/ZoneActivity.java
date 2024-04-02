package com.example.garageapp.Admin.Parking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.garageapp.Admin.Adapter.AdapterParking;
import com.example.garageapp.Admin.Adapter.AdapterZone;
import com.example.garageapp.Model.Parking;
import com.example.garageapp.Model.Zone;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ActivityAdminMainBinding;
import com.example.garageapp.databinding.ActivityZoneBinding;
import com.example.garageapp.databinding.AddParkingDialogBinding;
import com.example.garageapp.databinding.AddZoneDialogBinding;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.EasyPermissions;

public class ZoneActivity extends AppCompatActivity implements AdapterZone.OnItemClickListener {

    ActivityZoneBinding binding;
    Parking parking;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refParking = database.getReference("Parking");

    List<Zone> zoneList;
    AdapterZone adapterZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityZoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parking = (Parking) getIntent().getSerializableExtra("Parking");

        init();
        setOnClickListener();
        getZone();
    }

    void init() {
        zoneList = new ArrayList<>();
        adapterZone = new AdapterZone(this, zoneList, this);
        binding.zoneRecyclerView.setAdapter(adapterZone);
    }

    void setOnClickListener() {
        binding.addNewZone.setOnClickListener(v -> {
            showDialogAddZoneLayout(null);
        });
    }

    void getZone() {
        binding.progressBar.setVisibility(View.VISIBLE);
        refParking.child(parking.getID()).child("Zone").orderByChild("zoneFloor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                zoneList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Zone zone = snapshot.getValue(Zone.class);
                    if (zone != null) {
                        zoneList.add(zone);
                    }
                }
                Collections.sort(zoneList, new Comparator<Zone>() {
                    @Override
                    public int compare(Zone zone1, Zone zone2) {
                        // Compare the flore field of each Zone object
                        return Integer.compare(zone1.getZoneFloor(), zone2.getZoneFloor());
                    }
                });
                binding.progressBar.setVisibility(View.GONE);
                adapterZone.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void showDialogAddZoneLayout(Zone currentZone) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_zone_dialog, null);
        dialogBuilder.setView(dialogView);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        AddZoneDialogBinding addZoneDialogBinding = AddZoneDialogBinding.bind(dialogView);
        addZoneDialogBinding.title.setText(currentZone == null ? "Add New Zone" : "Update Zone");
        Zone zone;
        if (currentZone == null) {
            zone = new Zone();
            zone.setOwnerID(firebaseUser.getUid());
            zone.setParkingID(parking.getID());
            zone.setID(refParking.push().getKey());
        } else {
            zone = currentZone;
            addZoneDialogBinding.inputZoneName.setText(zone.getZoneName());
            addZoneDialogBinding.inputZoneFloor.setText(String.valueOf(zone.getZoneFloor()));
        }

        addZoneDialogBinding.btnSave.setOnClickListener(v -> {

            addZoneDialogBinding.btnSave.setVisibility(View.GONE);
            addZoneDialogBinding.progressBar.setVisibility(View.VISIBLE);

            String inputZoneName = Objects.requireNonNull(addZoneDialogBinding.inputZoneName.getText()).toString().trim();
            if (inputZoneName.isEmpty()) {
                addZoneDialogBinding.inputZoneName.setError("Zone Name Is Required");
                addZoneDialogBinding.inputZoneName.requestFocus();
                addZoneDialogBinding.btnSave.setVisibility(View.VISIBLE);
                addZoneDialogBinding.progressBar.setVisibility(View.GONE);
                return;
            }

            String inputZoneFloor = Objects.requireNonNull(addZoneDialogBinding.inputZoneFloor.getText()).toString().trim();
            if (inputZoneFloor.isEmpty()) {
                addZoneDialogBinding.inputZoneFloor.setError("Zone Floor Is Required");
                addZoneDialogBinding.inputZoneFloor.requestFocus();
                addZoneDialogBinding.btnSave.setVisibility(View.VISIBLE);
                addZoneDialogBinding.progressBar.setVisibility(View.GONE);
                return;
            }

            zone.setZoneName(inputZoneName);
            zone.setZoneFloor(Integer.parseInt(inputZoneFloor));
            refParking.child(parking.getID()).child("Zone").child(zone.getID()).setValue(zone)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toasty.success(this, "Save Zone Successfully", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    });
        });
    }

    @Override
    public void onItem_Click(int position) {
        Zone zone = zoneList.get(position);
        Intent intent = new Intent(this, SlotActivity.class);
        intent.putExtra("Zone", zone);
        startActivity(intent);
    }
}