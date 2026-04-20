package com.example.proje.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import com.example.proje.db.DatabaseHelper;
import com.example.proje.model.Transaction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportHelper {
    public static void exportCSV(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        List<Transaction> list = db.getAllTransactions();
        String filename = "butce_raporu_" + new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date()) + ".csv";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);

        try {
            FileWriter writer = new FileWriter(file);
            writer.append("ID,Başlık,Tutar,Kategori,Tür,Not,Tarih\n");
            for (Transaction t : list) {
                writer.append(t.getId() + "," + t.getTitle() + "," + t.getAmount() + "," + t.getCategory() + "," + t.getType() + "," + t.getNote() + "," + t.getDate() + "\n");
            }
            writer.flush();
            writer.close();
            shareFile(context, file, "text/csv");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void exportPDF(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        List<Transaction> list = db.getAllTransactions();
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText("Bütçe Raporu - " + new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()), 50, 50, paint);

        paint.setTextSize(14f);
        paint.setFakeBoldText(false);
        double inc = db.getTotalIncome();
        double exp = db.getTotalExpense();
        canvas.drawText("Toplam Gelir: " + String.format("%.2f ₺", inc), 50, 100, paint);
        canvas.drawText("Toplam Gider: " + String.format("%.2f ₺", exp), 50, 120, paint);
        canvas.drawText("Net Bakiye: " + String.format("%.2f ₺", inc - exp), 50, 140, paint);

        int y = 180;
        paint.setFakeBoldText(true);
        canvas.drawText("Tarih", 50, y, paint);
        canvas.drawText("Başlık", 150, y, paint);
        canvas.drawText("Kategori", 350, y, paint);
        canvas.drawText("Tutar", 500, y, paint);
        paint.setFakeBoldText(false);

        y += 20;
        for (Transaction t : list) {
            if (y > 800) break; // Simplified for one page
            canvas.drawText(t.getDate(), 50, y, paint);
            canvas.drawText(t.getTitle(), 150, y, paint);
            canvas.drawText(t.getCategory(), 350, y, paint);
            canvas.drawText(String.format("%.2f", t.getAmount()), 500, y, paint);
            y += 20;
        }

        document.finishPage(page);
        String filename = "butce_raporu_" + new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date()) + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);

        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            shareFile(context, file, "application/pdf");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void shareFile(Context context, File file, String type) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Raporu Paylaş"));
    }
}