package com.example.hobbyzooapp;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hobbyzooapp.CalendarEvolutionAdapter;
import com.example.hobbyzooapp.R;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarEvolutionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    private final ArrayList<LocalDate> days;
    public final View parentView;
    public final TextView dayOfMonth;
    private final CalendarEvolutionAdapter.OnItemListener onItemListener;
    public CalendarEvolutionViewHolder(@NonNull View itemView, CalendarEvolutionAdapter.OnItemListener onItemListener, ArrayList<LocalDate> days)
    {
        super(itemView);
        parentView = itemView.findViewById(R.id.parentView);
        dayOfMonth = itemView.findViewById(R.id.cellDayText);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
        this.days = days;
    }

    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(), days.get(getAdapterPosition()));
    }
}