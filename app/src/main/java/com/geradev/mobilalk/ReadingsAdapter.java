package com.geradev.mobilalk;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReadingsAdapter extends RecyclerView.Adapter<ReadingsAdapter.ViewHolder> {

    private static final String TAG = "ReadingsAdapter";
    private List<MeterReading> mReadings;
    private SimpleDateFormat dateFormat;
    private int lastPosition = -1;

    public ReadingsAdapter() {
        Log.d(TAG, "ReadingsAdapter: Új adapter létrehozva");
        mReadings = new ArrayList<>();
        dateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ViewHolder létrehozása");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reading, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MeterReading reading = mReadings.get(position);
        Log.d(TAG, "onBindViewHolder: Elem feltöltése a " + position + " pozíción, ID: " + reading.getId());
        
        holder.dateTextView.setText(dateFormat.format(reading.getDate()));
        holder.readingValueTextView.setText(String.format(Locale.getDefault(), "%.1f kWh", reading.getReading()));
        
        // Elemre kattintás kezelése
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Log.d(TAG, "onClick: Leolvasási elem kiválasztva, ID: " + reading.getId() + ", érték: " + reading.getReading());
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("READING_ID", reading.getId());
            context.startActivity(intent);
        });
        
        // Animáció az elemekhez
        setAnimation(holder.itemView, position);
    }
    
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Log.d(TAG, "setAnimation: Animáció alkalmazása a " + position + " pozíción");
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.slide_in_animation);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mReadings.size();
    }

    public void setReadings(List<MeterReading> readings) {
        Log.d(TAG, "setReadings: Lista frissítése " + readings.size() + " elemmel");
        mReadings.clear();
        mReadings.addAll(readings);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView readingValueTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            readingValueTextView = itemView.findViewById(R.id.readingValueTextView);
        }
    }
}