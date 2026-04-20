package com.example.proje.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proje.R;
import com.example.proje.db.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetLimitFragment extends Fragment {
    private RecyclerView rv;
    private DatabaseHelper db;
    private List<BudgetLimitItem> items;
    private BudgetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_budget_limit, container, false);
        rv = v.findViewById(R.id.rv_budget_limits);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        db = new DatabaseHelper(getContext());
        loadData();
        return v;
    }

    private void loadData() {
        items = new ArrayList<>();
        String[] categories = {"Maaş", "Kira", "Fatura", "Market", "Ulaşım", "Eğlence", "Sağlık", "Diğer"};
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        
        for (String cat : categories) {
            double expense = db.getMonthlyExpenseByCategory(cat, currentMonth);
            double limit = db.getBudgetLimit(cat);
            items.add(new BudgetLimitItem(cat, expense, limit));
        }
        
        adapter = new BudgetAdapter();
        rv.setAdapter(adapter);
    }

    private class BudgetLimitItem {
        String category;
        double expense, limit;
        BudgetLimitItem(String c, double e, double l) { category = c; expense = e; limit = l; }
    }

    private class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_limit, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BudgetLimitItem item = items.get(position);
            holder.tvCategory.setText(item.category);
            
            if (item.limit <= 0) {
                holder.tvStatus.setText("Limit belirlenmedi");
                holder.pb.setProgress(0);
                holder.tvInfo.setText(String.format(Locale.getDefault(), "%.2f ₺ / Limit yok", item.expense));
            } else {
                int percent = (int) ((item.expense / item.limit) * 100);
                holder.pb.setProgress(Math.min(percent, 100));
                holder.tvStatus.setText("%" + percent);
                holder.tvInfo.setText(String.format(Locale.getDefault(), "%.2f ₺ / %.2f ₺", item.expense, item.limit));
                
                int color;
                if (percent < 60) color = getResources().getColor(R.color.income_mint);
                else if (percent < 90) color = getResources().getColor(R.color.warning_orange);
                else color = getResources().getColor(R.color.expense_pink);
                
                holder.pb.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            }

            holder.itemView.setOnClickListener(v -> showSetLimitDialog(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategory, tvStatus, tvInfo;
            ProgressBar pb;
            ViewHolder(View v) {
                super(v);
                tvCategory = v.findViewById(R.id.tv_budget_category);
                tvStatus = v.findViewById(R.id.tv_budget_limit_status);
                tvInfo = v.findViewById(R.id.tv_budget_amount_info);
                pb = v.findViewById(R.id.pb_budget_limit);
            }
        }
    }

    private void showSetLimitDialog(BudgetLimitItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(item.category + " Bütçe Limiti");
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Limit tutarı girin (₺)");
        if (item.limit > 0) input.setText(String.valueOf(item.limit));
        builder.setView(input);

        builder.setPositiveButton("Kaydet", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                db.setBudgetLimit(item.category, Double.parseDouble(val));
                loadData();
            }
        });
        builder.setNegativeButton("İptal", null);
        builder.show();
    }
}