package com.berkaysenkoylu.travellog.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.berkaysenkoylu.travellog.databinding.LocationRowBinding;
import com.berkaysenkoylu.travellog.model.Place;
import com.berkaysenkoylu.travellog.view.LocationDetailActivity;

import java.util.List;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationHolder> {

    private List<Place> placeList;

    public LocationListAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public LocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LocationRowBinding locationRowBinding = LocationRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LocationHolder(locationRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationHolder holder, int position) {
        holder.locationRowBinding.locationNameView.setText(this.placeList.get(holder.getAdapterPosition()).name);

        holder.locationRowBinding.locationNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), LocationDetailActivity.class);
                intent.putExtra("editMode", true);
                intent.putExtra("selectedLocation", placeList.get(holder.getAdapterPosition()));
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.placeList.size();
    }

    public class LocationHolder extends RecyclerView.ViewHolder {
        LocationRowBinding locationRowBinding;

        public LocationHolder(LocationRowBinding binding) {
            super(binding.getRoot());
            locationRowBinding = binding;
        }
    }
}
