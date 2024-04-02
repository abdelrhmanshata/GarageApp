package com.example.garageapp.Admin.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garageapp.Model.Parking;
import com.example.garageapp.Model.Zone;
import com.example.garageapp.databinding.ItemParkingLayoutBinding;
import com.example.garageapp.databinding.ItemZoneLayoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdapterZone extends RecyclerView.Adapter<AdapterZone.ViewHolder> {

    Context context;
    List<Zone> mZone;
    private OnItemClickListener mListener;

    public AdapterZone(Context context, List<Zone> zone, OnItemClickListener mListener) {
        this.context = context;
        this.mZone = zone;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemZoneLayoutBinding binding = ItemZoneLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Zone zone = mZone.get(position);

        holder.binding.zoneName.setText(zone.getZoneName());
        holder.binding.zoneFloor.setText("Floor : " + zone.getZoneFloor());

    }

    @Override
    public int getItemCount() {
        return mZone.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItem_Click(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemZoneLayoutBinding binding;

        public ViewHolder(ItemZoneLayoutBinding binding) {
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
