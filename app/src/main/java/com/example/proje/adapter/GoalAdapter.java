package com.example.proje.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proje.R;
import com.example.proje.model.Goal;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {
    private List<Goal> list;
    private OnGoalClickListener listener;

    public interface OnGoalClickListener {
        void onClick(Goal goal);
        void onLongClick(Goal goal);
    }

    public GoalAdapter(List<Goal> list, OnGoalClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Goal g = list.get(position);
        holder.tvTitle.setText(g.getTitle());
        holder.tvNote.setText(g.getNote());
        holder.tvCurrent.setText(String.format("%.2f ₺", g.getCurrentAmount()));
        holder.tvTarget.setText(String.format("/ %.2f ₺", g.getTargetAmount()));
        
        int progress = g.getTargetAmount() > 0 ? (int) ((g.getCurrentAmount() / g.getTargetAmount()) * 100) : 0;
        holder.pb.setProgress(Math.min(progress, 100));
        holder.tvPercentage.setText("%" + Math.min(progress, 100));
        
        holder.itemView.setOnClickListener(v -> listener.onClick(g));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(g);
            return true;
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvNote, tvCurrent, tvTarget, tvPercentage;
        ProgressBar pb;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_goal_title);
            tvNote = itemView.findViewById(R.id.tv_goal_note);
            tvCurrent = itemView.findViewById(R.id.tv_goal_current);
            tvTarget = itemView.findViewById(R.id.tv_goal_target);
            tvPercentage = itemView.findViewById(R.id.tv_goal_percentage);
            pb = itemView.findViewById(R.id.progress_goal);
        }
    }
}