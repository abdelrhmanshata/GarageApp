package com.example.garageapp.Admin.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garageapp.Model.Order;
import com.example.garageapp.Model.Reserve;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ItemOrderLayoutBinding;
import com.example.garageapp.databinding.ItemReserveParkingBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterReservation extends RecyclerView.Adapter<AdapterReservation.ViewHolder> {

    Context context;
    List<Reserve> mReserves;
    private OnItemClickListener mListener;

    public AdapterReservation(Context context, List<Reserve> reserves, OnItemClickListener mListener) {
        this.context = context;
        this.mReserves = reserves;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemReserveParkingBinding binding = ItemReserveParkingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint({"SimpleDateFormat", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reserve reserve = mReserves.get(position);

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
        holder.binding.year.setText(yearFormat.format(date));
        holder.binding.day.setText(dayFormat.format(date));
        holder.binding.month.setText(monthFormat.format(date));

        //
        holder.binding.userName.setText(reserve.getUserName());
        holder.binding.userEmail.setText(reserve.getUserEmail());
        //
        holder.binding.startTime.setText("Start : " + reserve.getTime(reserve.getStartTime()));
        holder.binding.endTime.setText("End   : " + (reserve.getEndTime().isEmpty() ? " --:-- " : reserve.getTime(reserve.getEndTime())));
        //
        holder.binding.parking.setText(reserve.getParking());
        holder.binding.zone.setText(reserve.getZone());
        holder.binding.slot.setText(reserve.getSlot());

    }

    @Override
    public int getItemCount() {
        return mReserves.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItem_Click(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemReserveParkingBinding binding;

        public ViewHolder(ItemReserveParkingBinding binding) {
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
