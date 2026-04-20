package com.example.proje.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.proje.MainActivity;
import com.example.proje.R;
import com.example.proje.db.DatabaseHelper;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTransactionFragment extends Fragment {
    private EditText etAmount, etTitle, etNote;
    private MaterialButtonToggleGroup toggleGroup;
    private Spinner spinner;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        etAmount = v.findViewById(R.id.et_amount);
        etTitle = v.findViewById(R.id.et_title);
        etNote = v.findViewById(R.id.et_note);
        toggleGroup = v.findViewById(R.id.toggle_group);
        spinner = v.findViewById(R.id.spinner_category);
        db = new DatabaseHelper(getContext());

        String[] categories = {"Maaş", "Kira", "Fatura", "Market", "Ulaşım", "Eğlence", "Sağlık", "Diğer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categories) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((android.widget.TextView) view).setTextColor(android.graphics.Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(getContext().getResources().getColor(R.color.vault_card));
                ((android.widget.TextView) view).setTextColor(android.graphics.Color.WHITE);
                return view;
            }
        };
        spinner.setAdapter(adapter);

        v.findViewById(R.id.btn_save).setOnClickListener(view -> {
            String amountStr = etAmount.getText().toString().replace("₺", "").trim();
            String title = etTitle.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Lütfen bir tutar girin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (title.isEmpty()) {
                etTitle.setError("Başlık boş olamaz");
                etTitle.requestFocus();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Geçersiz tutar", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = toggleGroup.getCheckedButtonId() == R.id.btn_income ? "gelir" : "gider";
            String cat = spinner.getSelectedItem().toString();
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            db.addTransaction(title, amount, cat, type, note, date);

            etAmount.setText("");
            etTitle.setText("");
            etNote.setText("");

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToHome();
            }
        });
        return v;
    }
}