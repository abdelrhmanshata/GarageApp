package com.example.garageapp.Admin.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garageapp.Model.Parking;
import com.example.garageapp.databinding.ItemParkingLayoutBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdapterParking extends RecyclerView.Adapter<AdapterParking.ViewHolder> {

    Context context;
    List<Parking> mParking;
    private OnItemClickListener mListener;

    public AdapterParking(Context context, List<Parking> parking, OnItemClickListener mListener) {
        this.context = context;
        this.mParking = parking;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemParkingLayoutBinding binding = ItemParkingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Parking parking = mParking.get(position);

        holder.binding.parkingName.setText(parking.getParkingName());
        holder.binding.parkingDescription.setText(parking.getParkingLocationDescription());
    }

    @Override
    public int getItemCount() {
        return mParking.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItem_Click(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemParkingLayoutBinding binding;

        public ViewHolder(ItemParkingLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItem_Click(position);
                }
            }
        }
    }
}
