package com.geradev.mobilalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReadingsAdapter extends RecyclerView.Adapter<ReadingsAdapter.ViewHolder> {

    private List<MeterReading> mReadings;
    private SimpleDateFormat dateFormat;

    public ReadingsAdapter() {
        mReadings = new ArrayList<>();
        dateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reading, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MeterReading reading = mReadings.get(position);
        holder.dateTextView.setText(dateFormat.format(reading.getDate()));
        holder.readingValueTextView.setText(String.format(Locale.getDefault(), "%.1f kWh", reading.getReading()));
    }

    @Override
    public int getItemCount() {
        return mReadings.size();
    }

    public void setReadings(List<MeterReading> readings) {
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