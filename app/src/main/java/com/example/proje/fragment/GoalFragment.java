package com.example.proje.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proje.R;
import com.example.proje.adapter.GoalAdapter;
import com.example.proje.db.DatabaseHelper;
import com.example.proje.model.Goal;
import java.util.List;

public class GoalFragment extends Fragment {
    private RecyclerView rv;
    private DatabaseHelper db;
    private GoalAdapter adapter;
    private TextView tvNoGoals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_goal, container, false);
        rv = v.findViewById(R.id.rv_goals);
        tvNoGoals = v.findViewById(R.id.tv_no_goals);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        db = new DatabaseHelper(getContext());

        v.findViewById(R.id.fab_add_goal).setOnClickListener(view -> showAddGoalDialog());

        loadGoals();
        return v;
    }

    private void loadGoals() {
        List<Goal> list = db.getAllGoals();
        if (list.isEmpty()) {
            tvNoGoals.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvNoGoals.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
        
        adapter = new GoalAdapter(list, new GoalAdapter.OnGoalClickListener() {
            @Override
            public void onClick(Goal goal) {
                showAddMoneyDialog(goal);
            }

            @Override
            public void onLongClick(Goal goal) {
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.delete_confirm)
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            db.deleteGoal(goal.getId());
                            loadGoals();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        rv.setAdapter(adapter);
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle(R.string.add_goal);
        
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etTitle = new EditText(getContext());
        etTitle.setHint(R.string.title);
        layout.addView(etTitle);

        final EditText etTarget = new EditText(getContext());
        etTarget.setHint(R.string.target_amount);
        etTarget.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etTarget);

        final EditText etNote = new EditText(getContext());
        etNote.setHint(R.string.note);
        layout.addView(etNote);

        b.setView(layout);
        b.setPositiveButton(R.string.save, (d, w) -> {
            String title = etTitle.getText().toString();
            String targetStr = etTarget.getText().toString();
            String note = etNote.getText().toString();

            if (title.isEmpty() || targetStr.isEmpty()) {
                Toast.makeText(getContext(), "Lütfen başlık ve tutar girin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double target = Double.parseDouble(targetStr);
                db.addGoal(title, target, 0, note);
                loadGoals();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Geçersiz tutar", Toast.LENGTH_SHORT).show();
            }
        });
        b.setNegativeButton(R.string.cancel, null);
        b.show();
    }

    private void showAddMoneyDialog(Goal goal) {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle(R.string.add_money);
        
        final EditText etAmount = new EditText(getContext());
        etAmount.setHint(R.string.amount);
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        LinearLayout layout = new LinearLayout(getContext());
        layout.setPadding(50, 40, 50, 10);
        layout.addView(etAmount);
        b.setView(layout);

        b.setPositiveButton(R.string.save, (d, w) -> {
            String amountStr = etAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    db.updateGoalAmount(goal.getId(), amount);
                    loadGoals();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Geçersiz tutar", Toast.LENGTH_SHORT).show();
                }
            }
        });
        b.setNegativeButton(R.string.cancel, null);
        b.show();
    }
}