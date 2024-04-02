package com.example.garageapp.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.garageapp.Admin.Adapter.AdapterParking;
import com.example.garageapp.Admin.Parking.ZoneActivity;
import com.example.garageapp.Model.Parking;
import com.example.garageapp.R;
import com.example.garageapp.databinding.AddParkingDialogBinding;
import com.example.garageapp.databinding.FragmentHomeBinding;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class HomeFragment extends Fragment implements EasyPermissions.PermissionCallbacks, AdapterParking.OnItemClickListener {
    FragmentHomeBinding binding;
    AddParkingDialogBinding addParkingDialogBinding;
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


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        init();
        onClickListener();
        getParking();
        return binding.getRoot();
    }

    void init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        parkingList = new ArrayList<>();
        adapterParking = new AdapterParking(getContext(), parkingList, this);
        binding.parkingRecyclerView.setAdapter(adapterParking);

    }

    void onClickListener() {
        binding.addNewParking.setOnClickListener(v -> {
            showDialogAddParkingLayout(null);
        });
    }

    void getParking() {
        binding.progressBar.setVisibility(View.VISIBLE);

        refParking.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parkingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Parking parking = snapshot.getValue(Parking.class);
                    if (parking != null) {
                        if (parking.getOwnerID().equals(firebaseUser.getUid()))
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

    public void showDialogAddParkingLayout(Parking currentParking) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_parking_dialog, null);
        dialogBuilder.setView(dialogView);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        addParkingDialogBinding = AddParkingDialogBinding.bind(dialogView);
        addParkingDialogBinding.title.setText(currentParking == null ? "Add New Parking" : "Update Parking");

        Parking parking;
        if (currentParking == null) {
            parking = new Parking();
            parking.setOwnerID(firebaseUser.getUid());
            parking.setID(refParking.push().getKey());
        } else {
            parking = currentParking;
            addParkingDialogBinding.inputParkingName.setText(parking.getParkingName());
            addParkingDialogBinding.inputLocation.setText("Lat:" + parking.getLatitude() + " ,Long:" + parking.getLongitude());
            addParkingDialogBinding.inputLocationDescription.setText(parking.getParkingLocationDescription());
        }
        addParkingDialogBinding.selectParkingLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                showDialogMapLayout();
            } else {
                EasyPermissions.requestPermissions(this, "Please grant location permission", REQUEST_LOCATION_PERMISSION, Manifest.permission.ACCESS_FINE_LOCATION);
            }

        });
        addParkingDialogBinding.btnSave.setOnClickListener(v -> {
            addParkingDialogBinding.btnSave.setVisibility(View.GONE);
            addParkingDialogBinding.progressBar.setVisibility(View.VISIBLE);

            String inputParkingName = Objects.requireNonNull(addParkingDialogBinding.inputParkingName.getText()).toString().trim();
            if (inputParkingName.isEmpty()) {
                addParkingDialogBinding.inputParkingName.setError("Parking Name Is Required");
                addParkingDialogBinding.inputParkingName.requestFocus();
                addParkingDialogBinding.btnSave.setVisibility(View.VISIBLE);
                addParkingDialogBinding.progressBar.setVisibility(View.GONE);
                return;
            }

            String inputLocation = Objects.requireNonNull(addParkingDialogBinding.inputLocation.getText()).toString().trim();
            if (inputLocation.isEmpty()) {
                addParkingDialogBinding.inputLocation.setError("Location Is Required");
                addParkingDialogBinding.inputLocation.requestFocus();
                addParkingDialogBinding.btnSave.setVisibility(View.VISIBLE);
                addParkingDialogBinding.progressBar.setVisibility(View.GONE);
                return;
            }

            String inputLocationDescription = Objects.requireNonNull(addParkingDialogBinding.inputLocationDescription.getText()).toString().trim();
            if (inputLocationDescription.isEmpty()) {
                addParkingDialogBinding.inputLocationDescription.setError("Location Description Is Required");
                addParkingDialogBinding.inputLocationDescription.requestFocus();
                addParkingDialogBinding.btnSave.setVisibility(View.VISIBLE);
                addParkingDialogBinding.progressBar.setVisibility(View.GONE);
                return;
            }

            parking.setParkingName(inputParkingName);
            parking.setParkingLocation(inputLocation);
            parking.setParkingLocationDescription(inputLocationDescription);
            parking.setLatitude(String.valueOf(getLatitude()));
            parking.setLongitude(String.valueOf(getLongitude()));

            refParking.child(parking.getID()).setValue(parking).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toasty.success(getActivity(), "Save Parking Successfully", Toast.LENGTH_SHORT).show();
                    addParkingDialogBinding.btnSave.setVisibility(View.VISIBLE);
                    addParkingDialogBinding.progressBar.setVisibility(View.GONE);
                    alertDialog.dismiss();
                }
            });
        });
    }

    public void showDialogMapLayout() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.map_dialog, null);
        dialogBuilder.setView(dialogView);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        MapDialogBinding mapDialogBinding = MapDialogBinding.bind(dialogView);
        MapView mapView = mapDialogBinding.mapView;
        mapView.onCreate(new Bundle());
        mapView.getMapAsync(googleMap -> {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                EasyPermissions.requestPermissions(getActivity(), "Please grant location permission", REQUEST_LOCATION_PERMISSION, Manifest.permission.ACCESS_FINE_LOCATION);
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    setLatitude(location.getLatitude());
                    setLongitude(location.getLongitude());
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    // Add a marker at the current location
                    googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                    // Move the camera to the current location
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                }
            }).addOnFailureListener(requireActivity(), e -> {
                // Handle failure to get location
            });

//            googleMap.setMyLocationEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setOnMapClickListener(latLng -> {
                // Handle map click event
                googleMap.clear();
                setLatitude(latLng.latitude);
                setLongitude(latLng.longitude);
                LatLng currentLocation = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Selected Location"));
            });
        });

        mapDialogBinding.closeMabLayout.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        mapDialogBinding.saveLocation.setOnClickListener(v -> {
            addParkingDialogBinding.inputLocation.setText("Lat:" + getLatitude() + " ,Long:" + getLongitude());
            alertDialog.dismiss();
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
            showDialogMapLayout();
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
        Intent intent = new Intent(getContext(), ZoneActivity.class);
        intent.putExtra("Parking", parking);
        startActivity(intent);
    }
}