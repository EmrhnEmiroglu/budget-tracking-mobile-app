package com.example.proje;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.proje.db.DatabaseHelper;
import com.example.proje.model.Transaction;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

public class TransactionDetailActivity extends AppCompatActivity {
    private TextInputEditText etTitle, etAmount, etNote;
    private Spinner spinner;
    private MaterialButtonToggleGroup toggleGroup;
    private TextView tvDate;
    private DatabaseHelper db;
    private int transactionId;
    private Transaction currentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        transactionId = getIntent().getIntExtra("transaction_id", -1);
        db = new DatabaseHelper(this);
        currentTransaction = db.getTransaction(transactionId);

        etTitle = findViewById(R.id.et_detail_title);
        etAmount = findViewById(R.id.et_detail_amount);
        etNote = findViewById(R.id.et_detail_note);
        spinner = findViewById(R.id.spinner_detail_category);
        toggleGroup = findViewById(R.id.toggle_detail_type);
        tvDate = findViewById(R.id.tv_detail_date);

        String[] categories = {"Maaş", "Kira", "Fatura", "Market", "Ulaşım", "Eğlence", "Sağlık", "Diğer"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        if (currentTransaction != null) {
            etTitle.setText(currentTransaction.getTitle());
            etAmount.setText(String.valueOf(currentTransaction.getAmount()));
            etNote.setText(currentTransaction.getNote());
            tvDate.setText("Tarih: " + currentTransaction.getDate());
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(currentTransaction.getCategory())) spinner.setSelection(i);
            }
            toggleGroup.check(currentTransaction.getType().equalsIgnoreCase("gelir") ? R.id.btn_detail_income : R.id.btn_detail_expense);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Kaydet").setIcon(android.R.drawable.ic_menu_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, 2, 0, "Sil").setIcon(android.R.drawable.ic_menu_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        if (item.getItemId() == 1) { saveChanges(); return true; }
        if (item.getItemId() == 2) { deleteTransaction(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges() {
        String title = etTitle.getText().toString();
        String amountStr = etAmount.getText().toString();
        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }
        db.updateTransaction(transactionId, title, Double.parseDouble(amountStr), spinner.getSelectedItem().toString(),
                toggleGroup.getCheckedButtonId() == R.id.btn_detail_income ? "gelir" : "gider",
                etNote.getText().toString(), currentTransaction.getDate());
        Toast.makeText(this, "Güncellendi", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteTransaction() {
        new AlertDialog.Builder(this)
                .setTitle("Sil")
                .setMessage("Bu işlemi silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet", (d, w) -> {
                    db.deleteTransaction(transactionId);
                    finish();
                })
                .setNegativeButton("Hayır", null)
                .show();
    }
}