package com.example.proje.fragment;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proje.R;
import com.example.proje.adapter.TransactionAdapter;
import com.example.proje.db.DatabaseHelper;
import com.example.proje.model.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    private TextView tvIncome, tvExpense, tvBalance, tvNoData;
    private RecyclerView rv;
    private DatabaseHelper db;
    private PieChart pieChart;

    // Aktif filtre durumu
    private String filterCategory = null;
    private String filterType = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvIncome = v.findViewById(R.id.tv_total_income);
        tvExpense = v.findViewById(R.id.tv_total_expense);
        tvBalance = v.findViewById(R.id.tv_balance);
        tvNoData = v.findViewById(R.id.tv_no_data);
        rv = v.findViewById(R.id.rv_transactions);
        pieChart = v.findViewById(R.id.pieChart);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        db = new DatabaseHelper(getContext());

        v.findViewById(R.id.fab_add).setOnClickListener(view -> {
            if (getActivity() instanceof com.example.proje.MainActivity) {
                ((com.example.proje.MainActivity) getActivity()).setSelectedNav(R.id.nav_add);
            }
        });

        v.findViewById(R.id.iv_notification).setOnClickListener(view -> {
            if (getActivity() instanceof com.example.proje.MainActivity) {
                ((com.example.proje.MainActivity) getActivity()).navigateToNotificationSettings();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    // Menü inflate
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            showFilterSheet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        double inc = db.getTotalIncome();
        double exp = db.getTotalExpense();
        animateValue(tvIncome, 0, inc, "↑ ₺%.2f");
        animateValue(tvExpense, 0, exp, "↓ ₺%.2f");
        animateValue(tvBalance, 0, inc - exp, "₺%.2f");

        List<Transaction> list;
        if (filterCategory != null || filterType != null) {
            list = db.getFilteredTransactions(filterCategory, filterType, null, null);
        } else {
            list = db.getAllTransactions();
        }
        updateUI(list);
        setupCharts();
    }

    private void animateValue(TextView tv, double start, double end, String format) {
        ValueAnimator animator = ValueAnimator.ofFloat((float) start, (float) end);
        animator.setDuration(800);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> tv.setText(
                String.format(Locale.getDefault(), format, (float) a.getAnimatedValue())));
        animator.start();
    }

    private void updateUI(List<Transaction> list) {
        if (list.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
        rv.setAdapter(new TransactionAdapter(list, t -> {}));
    }

    private void setupCharts() {
        Map<String, Double> categoryExpenses = db.getCategoryExpenses();
        if (categoryExpenses.isEmpty()) return;

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet())
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#7F7CFD"),
                Color.parseColor("#00E5A0"),
                Color.parseColor("#FF6B8A"),
                Color.parseColor("#FFB347"),
                Color.parseColor("#00BCD4")
        });
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(3f);

        pieChart.setData(new PieData(dataSet));
        pieChart.setHoleRadius(75f);
        pieChart.setTransparentCircleRadius(80f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("₺" + String.format(Locale.getDefault(), "%.0f", db.getTotalExpense()));
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(20f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void showFilterSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_filter_bottom_sheet, null);

        Spinner spinnerCat = sheetView.findViewById(R.id.spinner_filter_category);
        Spinner spinnerType = sheetView.findViewById(R.id.spinner_filter_type);

        String[] categories = {"Tümü", "Maaş", "Kira", "Fatura", "Market", "Ulaşım", "Eğlence", "Sağlık", "Diğer"};
        String[] types = {"Tümü", "Gelir", "Gider"};

        spinnerCat.setAdapter(styledAdapter(categories));
        spinnerType.setAdapter(styledAdapter(types));

        // Mevcut filtreyi yansıt
        if (filterCategory != null) {
            for (int i = 0; i < categories.length; i++)
                if (categories[i].equals(filterCategory)) { spinnerCat.setSelection(i); break; }
        }
        if (filterType != null) {
            for (int i = 0; i < types.length; i++)
                if (types[i].equalsIgnoreCase(filterType)) { spinnerType.setSelection(i); break; }
        }

        sheetView.findViewById(R.id.btn_apply_filter).setOnClickListener(v -> {
            String cat = spinnerCat.getSelectedItem().toString();
            String type = spinnerType.getSelectedItem().toString();
            filterCategory = cat.equals("Tümü") ? null : cat;
            filterType = type.equals("Tümü") ? null : type;
            loadData();
            sheet.dismiss();
        });

        sheetView.findViewById(R.id.btn_clear_filter).setOnClickListener(v -> {
            filterCategory = null;
            filterType = null;
            loadData();
            sheet.dismiss();
        });

        sheet.setContentView(sheetView);
        sheet.show();
    }

    private ArrayAdapter<String> styledAdapter(String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_item, items) {
            @Override
            public View getView(int pos, View cv, @NonNull ViewGroup parent) {
                View view = super.getView(pos, cv, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }
            @Override
            public View getDropDownView(int pos, View cv, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(pos, cv, parent);
                view.setBackgroundColor(requireContext().getResources().getColor(R.color.vault_card));
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
}
