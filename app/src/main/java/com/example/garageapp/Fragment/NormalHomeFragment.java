package com.example.garageapp.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.garageapp.Admin.Adapter.AdapterParking;
import com.example.garageapp.Admin.Parking.ZoneActivity;
import com.example.garageapp.Model.Parking;
import com.example.garageapp.Normal.ShowZoneActivity;
import com.example.garageapp.R;
import com.example.garageapp.databinding.AddParkingDialogBinding;
import com.example.garageapp.databinding.FragmentHomeBinding;
import com.example.garageapp.databinding.FragmentNormalHomeBinding;
import com.example.garageapp.databinding.MapDialogBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class NormalHomeFragment extends Fragment implements EasyPermissions.PermissionCallbacks, AdapterParking.OnItemClickListener {

    FragmentNormalHomeBinding binding;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refParking = database.getReference("Parking");
    private static final int REQUEST_LOCATION_PERMISSION = 123;
    private FusedLocationProviderClient fusedLocationClient;
    double latitude = 0.0;
    double longitude = 0.0;

    //
    List<Parking> parkingList;
    AdapterParking adapterParking;

    public NormalHomeFragment() {
        // Required empty public constructor
    }

    TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
            getParking(s.toString().toLowerCase());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNormalHomeBinding.inflate(inflater, container, false);
        init();
        getParking("");
        return binding.getRoot();
    }

    void init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        parkingList = new ArrayList<>();
        adapterParking = new AdapterParking(getContext(), parkingList, this);
        binding.parkingRecyclerView.setAdapter(adapterParking);
        binding.inputParkingName.addTextChangedListener(searchTextWatcher);
    }

    @SuppressLint("NotifyDataSetChanged")
    void getParking(String parkingName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        refParking.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parkingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Parking parking = snapshot.getValue(Parking.class);
                    if (parking != null) {
                        if (parking.getParkingName().toLowerCase().contains(parkingName) || parking.getParkingLocationDescription().toLowerCase().contains(parkingName))
                            parkingList.add(parking);
                    }
                }
                binding.progressBar.setVisibility(View.GONE);
                adapterParking.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // Permission granted, you can now perform your tasks that require this permission
        if (requestCode == REQUEST_LOCATION_PERMISSION) {

        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Permission denied
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // Some permissions are permanently denied, show a dialog to the user
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onItem_Click(int position) {
        Parking parking = parkingList.get(position);
        Intent intent = new Intent(getContext(), ShowZoneActivity.class);
        intent.putExtra("Parking", parking);
        startActivity(intent);
    }
}