package com.example.garageapp.Admin.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garageapp.Model.Order;
import com.example.garageapp.Model.Parking;
import com.example.garageapp.R;
import com.example.garageapp.databinding.ItemOrderLayoutBinding;
import com.example.garageapp.databinding.ItemParkingLayoutBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterOrder extends RecyclerView.Adapter<AdapterOrder.ViewHolder> {

    Context context;
    List<Order> mOrders;
    private OnItemClickListener mListener;

    public AdapterOrder(Context context, List<Order> orders, OnItemClickListener mListener) {
        this.context = context;
        this.mOrders = orders;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemOrderLayoutBinding binding = ItemOrderLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint({"SimpleDateFormat", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = mOrders.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = dateFormat.parse(order.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());

        holder.binding.year.setText(yearFormat.format(date));
        holder.binding.day.setText(dayFormat.format(date));
        holder.binding.month.setText(monthFormat.format(date));
        holder.binding.userName.setText(order.getUserName());
        holder.binding.userEmail.setText(order.getUserEmail());
        holder.binding.userPhone.setText(order.getPhoneNumber());
        holder.binding.userLocation.setText(order.getLocation());
        holder.binding.orderStatus.setText(order.getStatus());

        int color;
        if (order.getStatus().equals("Received")) {
            color = Color.parseColor("#008000"); // Example color
        } else if (order.getStatus().equals("Pending")) {
            color = Color.parseColor("#B00020");
        } else {
            color = Color.parseColor("#757575");
        }
        ColorStateList colorStateList = ColorStateList.valueOf(color);
        holder.binding.orderStatus.setBackgroundTintList(colorStateList);
    }

    @Override
    public int getItemCount() {
        return mOrders.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItem_Click(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemOrderLayoutBinding binding;

        public ViewHolder(ItemOrderLayoutBinding binding) {
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
