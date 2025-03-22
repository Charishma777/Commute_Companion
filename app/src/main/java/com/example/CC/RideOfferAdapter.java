package com.example.CC;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RideOfferAdapter extends RecyclerView.Adapter<RideOfferAdapter.RideViewHolder>{

    private List<RideModel> rideList;
    public RideOfferAdapter(List<RideModel> rideList) {
        this.rideList = rideList;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        RideModel ride = rideList.get(position);
        holder.tvSource.setText("Source: " + ride.getSource());
        holder.tvDestination.setText("Destination: " + ride.getDestination());
        holder.tvDate.setText("Date: " + ride.getDate());
        holder.tvTime.setText("Time: " + ride.getTime());
        holder.tvCarNo.setText("Car No: " + ride.getCarNo());
        holder.tvPhone.setText("Phone: " + ride.getPhone());
        holder.tvSeats.setText("Number of Seats: " + ride.getNoOfSeats());
        holder.tvbasePrice.setText("Base Price: " + ride.getBasePrice());
        holder.tvCarType.setText("Car Model: " + ride.getCarType());
    }


    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView tvSource, tvDestination, tvDate, tvTime, tvCarNo, tvPhone, tvSeats, tvbasePrice, tvCarType;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCarNo = itemView.findViewById(R.id.tvCarNo);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvbasePrice = itemView.findViewById(R.id.tvbasePrice);
            tvCarType = itemView.findViewById(R.id.tvCarType);
        }
    }
}
