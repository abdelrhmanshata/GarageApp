package com.example.garageapp.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.garageapp.Model.Parking;
import com.example.garageapp.Model.Reserve;
import com.example.garageapp.Model.User;
import com.example.garageapp.R;
import com.example.garageapp.databinding.FragmentNormalReservationBinding;
import com.example.garageapp.databinding.QrDialogBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class NormalReservationFragment extends Fragment {

    FragmentNormalReservationBinding binding;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refParking = database.getReference("Parking");
    DatabaseReference refReservation = database.getReference("Reservation");
    DatabaseReference refUsers = database.getReference("Users");

    User objUser;
    Reserve reserve;
    FusedLocationProviderClient locationProviderClient;

    public NormalReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNormalReservationBinding.inflate(inflater, container, false);
        getUserInfo();

        locationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        binding.reservationLayout.setOnClickListener(v -> {
            showQrCodeLayout();
        });

        binding.btnParkingRoute.setOnClickListener(v -> {
            getData();
        });

        return binding.getRoot();
    }

    void getReservation(String reserveID) {
        refReservation.child(reserveID).addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SimpleDateFormat", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Reserve reserve = dataSnapshot.getValue(Reserve.class);
                if (reserve != null) {
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
                    binding.year.setText(yearFormat.format(date));
                    binding.day.setText(dayFormat.format(date));
                    binding.month.setText(monthFormat.format(date));

                    //
                    binding.userName.setText(reserve.getUserName());
                    binding.userEmail.setText(reserve.getUserEmail());
                    //
                    binding.startTime.setText("Start : " + reserve.getTime(reserve.getStartTime()));
                    binding.endTime.setText("End   : " + (reserve.getEndTime().isEmpty() ? " --:-- " : reserve.getTime(reserve.getEndTime())));
                    //
                    binding.parking.setText(reserve.getParking());
                    binding.zone.setText(reserve.getZone());
                    binding.slot.setText(reserve.getSlot());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void getUserInfo() {
        refUsers.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    objUser = user;
                    if (!objUser.getReservationID().isEmpty()) {
                        binding.reservationLayout.setVisibility(View.VISIBLE);
                        binding.btnParkingRoute.setVisibility(View.VISIBLE);
                        getReservation(objUser.getReservationID());
                    } else {
                        binding.reservationLayout.setVisibility(View.GONE);
                        binding.btnParkingRoute.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public Bitmap generateQRCode(String text, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
        int matrixWidth = bitMatrix.getWidth();
        int matrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[matrixWidth * matrixHeight];
        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                pixels[y * matrixWidth + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight);
        return bitmap;
    }

    public void showQrCodeLayout() {
        androidx.appcompat.app.AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.qr_dialog, null);
        dialogBuilder.setView(dialogView);
        androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        // Initialize ViewBinding for the layout
        QrDialogBinding qrDialogBinding = QrDialogBinding.bind(dialogView);

        Bitmap qrCodeBitmap = generateQRCode(objUser.getReservationID(), 512, 512);
        if (qrCodeBitmap != null) {
            qrDialogBinding.qrCodeImageView.setImageBitmap(qrCodeBitmap);
        } else {
            Toasty.error(getContext(), "Error QR-Code", Toast.LENGTH_SHORT).show();
        }
    }

    void getData() {
        refUsers.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    refReservation.child(user.getReservationID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Reserve reserve = dataSnapshot.getValue(Reserve.class);
                            if (reserve != null) {
                                refParking.child(reserve.getParkingID()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Parking parking = dataSnapshot.getValue(Parking.class);
                                        if (parking != null) {
                                            drawRoute(parking);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void drawRoute(Parking parking) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        locationProviderClient
                .getLastLocation()
                .addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location != null) {
                        try {
                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            String uri = "http://maps.google.com/maps?f=d&hl=en&saddr=" + addresses.get(0).getLatitude() + "," + addresses.get(0).getLongitude() + "&daddr=" + parking.getLatitude() + "," + parking.getLongitude();
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                            startActivity(intent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}