package com.example.proje.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proje.R;
import com.example.proje.TransactionDetailActivity;
import com.example.proje.model.Transaction;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> list;
    private OnItemLongClickListener listener;

    public interface OnItemLongClickListener { void onLongClick(Transaction transaction); }

    public TransactionAdapter(List<Transaction> list, OnItemLongClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = list.get(position);
        holder.itemView.startAnimation(
                AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_animation));

        holder.tvTitle.setText(t.getTitle());
        holder.tvCategory.setText(t.getCategory() + " • " + t.getDate());

        boolean isIncome = t.getType().equalsIgnoreCase("gelir");
        holder.tvAmount.setText(String.format("%s%.2f ₺", isIncome ? "+" : "-", t.getAmount()));
        holder.tvAmount.setTextColor(Color.parseColor(isIncome ? "#00E5A0" : "#FF6B8A"));

        // Kategori ikonu ve arka plan rengi
        holder.tvIcon.setText(categoryEmoji(t.getCategory()));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(14));
        bg.setColor(categoryColor(t.getCategory()));
        holder.tvIcon.setBackground(bg);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TransactionDetailActivity.class);
            intent.putExtra("transaction_id", t.getId());
            v.getContext().startActivity(intent);
        });
        holder.itemView.setOnLongClickListener(v -> { listener.onLongClick(t); return true; });
    }

    @Override
    public int getItemCount() { return list.size(); }

    private static String categoryEmoji(String category) {
        if (category == null) return "💰";
        switch (category) {
            case "Maaş":     return "💼";
            case "Kira":     return "🏠";
            case "Fatura":   return "📄";
            case "Market":   return "🛒";
            case "Ulaşım":   return "🚌";
            case "Eğlence":  return "🎮";
            case "Sağlık":   return "💊";
            default:         return "💰";
        }
    }

    private static int categoryColor(String category) {
        if (category == null) return Color.parseColor("#267F7CFD");
        switch (category) {
            case "Maaş":     return Color.parseColor("#2600E5A0"); // mint
            case "Kira":     return Color.parseColor("#263B82F6"); // mavi
            case "Fatura":   return Color.parseColor("#26FFB347"); // turuncu
            case "Market":   return Color.parseColor("#2600BCD4"); // cyan
            case "Ulaşım":   return Color.parseColor("#267F7CFD"); // mor
            case "Eğlence":  return Color.parseColor("#26FF6B8A"); // pembe
            case "Sağlık":   return Color.parseColor("#2600E5A0"); // mint
            default:         return Color.parseColor("#267F7CFD"); // mor
        }
    }

    private static int dpToPx(int dp) { return Math.round(dp * android.content.res.Resources.getSystem().getDisplayMetrics().density); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount, tvIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tv_item_title);
            tvCategory = itemView.findViewById(R.id.tv_item_category);
            tvAmount   = itemView.findViewById(R.id.tv_item_amount);
            tvIcon     = itemView.findViewById(R.id.view_type_indicator);
        }
    }
}
