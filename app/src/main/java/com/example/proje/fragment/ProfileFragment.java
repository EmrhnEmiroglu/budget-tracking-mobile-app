package com.example.proje.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.example.proje.LoginActivity;
import com.example.proje.R;
import com.example.proje.auth.UserManager;
import com.example.proje.db.DatabaseHelper;
import com.example.proje.helper.ExportHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private static final String PREFS_UI = "vault_ui_prefs";
    private static final String KEY_NOTIF = "pref_notifications";
    private static final String KEY_AVATAR_URI = "avatar_uri";

    private DatabaseHelper db;
    private UserManager userManager;

    private TextView tvUserName, tvUserEmail;
    private TextView tvStatTransactions, tvStatCategories, tvStatGoals;
    private ImageView ivAvatar;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) saveAndShowAvatar(uri);
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());
        userManager = new UserManager(requireContext());

        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvStatTransactions = view.findViewById(R.id.tv_stat_transactions);
        tvStatCategories = view.findViewById(R.id.tv_stat_categories);
        tvStatGoals = view.findViewById(R.id.tv_stat_goals);
        ivAvatar = view.findViewById(R.id.iv_avatar);

        loadUserInfo();
        loadStats();
        setupRows(view);

        ivAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        ivAvatar.setOnLongClickListener(v -> { showAvatarOptions(); return true; });

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> confirmLogout());

        // Zil ikonundan gelindiyse bildirimler satırına scroll yap
        Bundle args = getArguments();
        if (args != null && args.getBoolean("scroll_to_notifications", false)) {
            View rowNotif = view.findViewById(R.id.row_notifications);
            if (rowNotif != null) {
                rowNotif.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        rowNotif.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        ScrollView scrollView = (ScrollView) view;
                        scrollView.smoothScrollTo(0, rowNotif.getTop() - 80);
                    }
                });
            }
        }
    }

    private void loadUserInfo() {
        tvUserName.setText(userManager.getName());
        tvUserEmail.setText(userManager.getEmail());

        // Kaydedilmiş avatar varsa göster
        String uriStr = requireContext()
                .getSharedPreferences(PREFS_UI, android.content.Context.MODE_PRIVATE)
                .getString(KEY_AVATAR_URI, null);
        if (uriStr != null) {
            try {
                ivAvatar.setImageURI(Uri.parse(uriStr));
                ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception ignored) {}
        }
    }

    private void showAvatarOptions() {
        boolean hasAvatar = requireContext()
                .getSharedPreferences(PREFS_UI, android.content.Context.MODE_PRIVATE)
                .getString(KEY_AVATAR_URI, null) != null;

        if (!hasAvatar) {
            pickImageLauncher.launch("image/*");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Profil Fotoğrafı")
                .setItems(new String[]{"Fotoğrafı Değiştir", "Fotoğrafı Kaldır"}, (d, which) -> {
                    if (which == 0) {
                        pickImageLauncher.launch("image/*");
                    } else {
                        requireContext().getSharedPreferences(PREFS_UI, android.content.Context.MODE_PRIVATE)
                                .edit().remove(KEY_AVATAR_URI).apply();
                        ivAvatar.setImageResource(android.R.drawable.ic_menu_camera);
                        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        Toast.makeText(requireContext(), "Profil fotoğrafı kaldırıldı", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void saveAndShowAvatar(Uri uri) {
        // Kalıcı okuma izni al (content URI için)
        try {
            requireContext().getContentResolver()
                    .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {}

        requireContext().getSharedPreferences(PREFS_UI, android.content.Context.MODE_PRIVATE)
                .edit().putString(KEY_AVATAR_URI, uri.toString()).apply();

        ivAvatar.setImageURI(uri);
        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Toast.makeText(requireContext(), "Profil fotoğrafı güncellendi", Toast.LENGTH_SHORT).show();
    }

    private void loadStats() {
        tvStatTransactions.setText(String.valueOf(db.getAllTransactions().size()));
        tvStatCategories.setText(String.valueOf(db.getDistinctCategoryCount()));
        tvStatGoals.setText(String.valueOf(db.getAllGoals().size()));
    }

    private void setupRows(View root) {
        // --- HESAP ---
        View rowEditProfile = root.findViewById(R.id.row_edit_profile);
        setupRow(rowEditProfile, "✏️", "Profili Düzenle", null, false);
        rowEditProfile.setOnClickListener(v -> showEditProfileDialog());

        View rowChangePassword = root.findViewById(R.id.row_change_password);
        setupRow(rowChangePassword, "🔒", "Şifre Değiştir", null, false);
        rowChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // --- TERCİHLER ---
        boolean notifOn = requireContext()
                .getSharedPreferences(PREFS_UI, android.content.Context.MODE_PRIVATE)
                .getBoolean(KEY_NOTIF, true);

        View rowNotifications = root.findViewById(R.id.row_notifications);
        setupRow(rowNotifications, "🔔", "Bildirimler", null, true);
        SwitchCompat swNotif = rowNotifications.findViewById(R.id.sw_row_toggle);
        swNotif.setChecked(notifOn);
        swNotif.setOnCheckedChangeListener((btn, checked) ->
                requireContext().getSharedPreferences(PREFS_UI, android.content.Context.MODE_PRIVATE)
                        .edit().putBoolean(KEY_NOTIF, checked).apply());

        View rowCurrency = root.findViewById(R.id.row_currency);
        setupRow(rowCurrency, "💱", "Para Birimi", "Türk Lirası (₺)", false);
        rowCurrency.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Şu an yalnızca ₺ desteklenmektedir", Toast.LENGTH_SHORT).show());

        View rowLanguage = root.findViewById(R.id.row_language);
        setupRow(rowLanguage, "🌍", "Dil", "Türkçe", false);
        rowLanguage.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Şu an yalnızca Türkçe desteklenmektedir", Toast.LENGTH_SHORT).show());

        // --- VERİ ---
        View rowExport = root.findViewById(R.id.row_export);
        setupRow(rowExport, "📤", "Dışa Aktar", "CSV veya PDF formatında", false);
        rowExport.setOnClickListener(v -> showExportBottomSheet());

        View rowDeleteData = root.findViewById(R.id.row_delete_data);
        setupRow(rowDeleteData, "🗑️", "Tüm Verileri Sil", null, false);
        rowDeleteData.setOnClickListener(v -> confirmDeleteAll());
    }

    private void setupRow(View row, String icon, String label, String value, boolean isToggle) {
        ((TextView) row.findViewById(R.id.tv_row_icon)).setText(icon);
        ((TextView) row.findViewById(R.id.tv_row_label)).setText(label);

        TextView tvValue = row.findViewById(R.id.tv_row_value);
        if (value != null) {
            tvValue.setText(value);
            tvValue.setVisibility(View.VISIBLE);
        }
        if (isToggle) {
            row.findViewById(R.id.tv_row_arrow).setVisibility(View.GONE);
            row.findViewById(R.id.sw_row_toggle).setVisibility(View.VISIBLE);
            row.setClickable(false);
            row.setFocusable(false);
        }
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_profile_edit_dialog, null);

        TextInputEditText etName = dialogView.findViewById(R.id.et_display_name);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);
        etName.setText(userManager.getName());
        etEmail.setText(userManager.getEmail());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String name = text(etName);
            String email = text(etEmail);
            if (name.isEmpty()) { etName.setError("İsim boş olamaz"); return; }
            userManager.updateProfile(name, email);
            tvUserName.setText(name);
            tvUserEmail.setText(email);
            dialog.dismiss();
            Toast.makeText(requireContext(), "Profil güncellendi", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_change_password_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            TextInputEditText etCurrent = dialogView.findViewById(R.id.et_current_password);
            TextInputEditText etNew = dialogView.findViewById(R.id.et_new_password);
            TextInputEditText etConfirm = dialogView.findViewById(R.id.et_confirm_password);

            String current = text(etCurrent);
            String newPass = text(etNew);
            String confirm = text(etConfirm);

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirm)) { etConfirm.setError("Şifreler eşleşmiyor"); return; }
            if (newPass.length() < 6) { etNew.setError("En az 6 karakter olmalı"); return; }
            if (userManager.changePassword(current, newPass)) {
                dialog.dismiss();
                Toast.makeText(requireContext(), "Şifre değiştirildi", Toast.LENGTH_SHORT).show();
            } else {
                etCurrent.setError("Mevcut şifre hatalı");
            }
        });
        dialog.show();
    }

    private void showExportBottomSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_export_bottom_sheet, null);
        sheetView.findViewById(R.id.btn_export_csv).setOnClickListener(v -> {
            ExportHelper.exportCSV(requireContext());
            sheet.dismiss();
        });
        sheetView.findViewById(R.id.btn_export_pdf).setOnClickListener(v -> {
            ExportHelper.exportPDF(requireContext());
            sheet.dismiss();
        });
        sheet.setContentView(sheetView);
        sheet.show();
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tüm Verileri Sil")
                .setMessage("Tüm işlemler, hedefler ve bütçe limitleri kalıcı olarak silinecek. Emin misiniz?")
                .setPositiveButton("Sil", (d, w) -> {
                    db.deleteAllData();
                    loadStats();
                    Toast.makeText(requireContext(), "Tüm veriler silindi", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Çıkış Yap")
                .setMessage("Uygulamadan çıkmak istediğinize emin misiniz?")
                .setPositiveButton("Çıkış Yap", (d, w) -> {
                    userManager.logout();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }
}
