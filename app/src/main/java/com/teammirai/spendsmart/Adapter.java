package com.teammirai.spendsmart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teammirai.spendsmart.model.Goal;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<Goal> data;

    public Adapter(Context context, List<Goal> data) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.goal_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Goal goal = data.get(position);
        holder.goalLabel.setText(goal.getGoalName());
        holder.dayLabel.setText(goal.getDate().split("/")[0]); // Assuming date format is "dd/MM/yyyy"
        holder.monthAndYearLabel.setText(goal.getDate().substring(goal.getDate().indexOf('/') + 1)); // Assuming date format is "dd/MM/yyyy"
        holder.timeLabel.setText(goal.getTime());
        holder.amountLabel.setText(goal.getAmount());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView goalLabel, dayLabel, monthAndYearLabel, amountLabel, timeLabel;

        public ViewHolder(View itemView) {
            super(itemView);
            goalLabel = itemView.findViewById(R.id.goalLabel);
            dayLabel = itemView.findViewById(R.id.dayLabel);
            monthAndYearLabel = itemView.findViewById(R.id.monthAndYearLabel);
            amountLabel = itemView.findViewById(R.id.amountLabel);
            timeLabel = itemView.findViewById(R.id.timeLabel);
        }
    }
}
