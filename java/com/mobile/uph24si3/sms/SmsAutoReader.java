package com.mobile.uph24si3.sms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SmsAutoReader - Auto membaca SMS dari bank Indonesia dan mengekstrak data transaksi.
 *
 * Bank yang didukung:
 * - BCA (Bank Central Asia)
 * - Mandiri
 * - BRI (Bank Rakyat Indonesia)
 * - BNI (Bank Negara Indonesia)
 * - GoPay / Gojek
 * - OVO
 * - DANA
 * - ShopeePay
 */
public class SmsAutoReader {

    // ================== SMS SENDER NUMBERS / NAMES ==================
    private static final String[] BCA_SENDERS = {"BCA", "MYBCA", "KLIKBCA", "1500888", "69888"};
    private static final String[] MANDIRI_SENDERS = {"MANDIRI", "BANKMANDIRI", "14000"};
    private static final String[] BRI_SENDERS = {"BRI", "BANKBRI", "1500017", "BRI-NOTIF"};
    private static final String[] BNI_SENDERS = {"BNI", "BANKBNI", "1500046", "BNI46"};
    private static final String[] GOPAY_SENDERS = {"GOPAY", "GOJEK", "GoTo"};
    private static final String[] OVO_SENDERS = {"OVO", "OVO-NOTIF"};
    private static final String[] DANA_SENDERS = {"DANA", "DANA-INFO"};
    private static final String[] SHOPEEPAY_SENDERS = {"ShopeePay", "Shopee", "SHOPEE"};

    // ================== SMS PATTERNS ==================

    /**
     * BCA Debit: "tgl DD/MM/YY digunakan pada MERCHANT sejumlah Rp X.XXX,XX"
     * BCA Kredit: "tgl DD/MM/YY diterima dari SENDER sejumlah Rp X.XXX,XX"
     */
    private static final Pattern BCA_DEBIT_PATTERN = Pattern.compile(
        "(?i)(?:digunakan|debit|pembelian|pembayaran).*?(?:sejumlah|sebesar|Rp\\.?)\\s*([\\d.,]+)",
        Pattern.DOTALL);
    private static final Pattern BCA_CREDIT_PATTERN = Pattern.compile(
        "(?i)(?:diterima|kredit|transfer masuk|top.?up).*?(?:sejumlah|sebesar|Rp\\.?)\\s*([\\d.,]+)",
        Pattern.DOTALL);

    /**
     * Mandiri: "Debit Rp. X.XXX.XXX,- Tgl ... MERCHANT"
     *          "Credit/Kredit Rp. X.XXX.XXX,- Tgl ..."
     */
    private static final Pattern MANDIRI_DEBIT_PATTERN = Pattern.compile(
        "(?i)(?:debit|db|pembelian|pembayaran)\\s*Rp\\.?\\s*([\\d.,]+)",
        Pattern.DOTALL);
    private static final Pattern MANDIRI_CREDIT_PATTERN = Pattern.compile(
        "(?i)(?:credit|cr|kredit|transfer masuk)\\s*Rp\\.?\\s*([\\d.,]+)",
        Pattern.DOTALL);

    /**
     * BRI: "Debit Rp X.XXX,XX YYYY-MM-DD MERCHANT"
     *      "Kredit Rp X.XXX,XX YYYY-MM-DD"
     */
    private static final Pattern BRI_DEBIT_PATTERN = Pattern.compile(
        "(?i)(?:debit|db|pembelian|pembayaran)\\s*Rp\\s*([\\d.,]+)",
        Pattern.DOTALL);
    private static final Pattern BRI_CREDIT_PATTERN = Pattern.compile(
        "(?i)(?:kredit|cr|transfer masuk|terima)\\s*Rp\\s*([\\d.,]+)",
        Pattern.DOTALL);

    /**
     * GoPay: "Pembayaran Rp X.XXX ke MERCHANT berhasil"
     *        "Transfer Rp X.XXX ke ... berhasil"
     *        "Top Up Rp X.XXX berhasil"
     */
    private static final Pattern GOPAY_PAYMENT_PATTERN = Pattern.compile(
        "(?i)(?:pembayaran|bayar|transfer keluar)\\s*Rp\\s*([\\d.,]+)",
        Pattern.DOTALL);
    private static final Pattern GOPAY_TOPUP_PATTERN = Pattern.compile(
        "(?i)(?:top.?up|isi ulang|terima|masuk|transfer masuk)\\s*Rp\\s*([\\d.,]+)",
        Pattern.DOTALL);

    /**
     * OVO: "Transaksi OVO Pay Rp X.XXX ke MERCHANT"
     *      "OVO Cash Rp X.XXX berhasil diterima"
     */
    private static final Pattern OVO_PAYMENT_PATTERN = Pattern.compile(
        "(?i)(?:ovo\\s*pay|bayar|pembayaran|transfer)\\s*Rp\\s*([\\d.,]+)",
        Pattern.DOTALL);
    private static final Pattern OVO_CREDIT_PATTERN = Pattern.compile(
        "(?i)(?:diterima|top.?up|masuk|kredit)\\s*Rp\\s*([\\d.,]+)",
        Pattern.DOTALL);

    /**
     * General fallback: any amount pattern
     */
    private static final Pattern GENERAL_AMOUNT_PATTERN = Pattern.compile(
        "(?i)Rp\\.?\\s*([\\d.,]+)");

    // ================== MERCHANT EXTRACTION ==================
    private static final Pattern MERCHANT_PATTERN = Pattern.compile(
        "(?i)(?:di|pada|ke|for|merchant|at)\\s+([A-Z][A-Z0-9\\s&'-]{2,40}?)(?:\\s*(?:tgl|pada|saldo|ref|info|\\d|berhasil|$))",
        Pattern.DOTALL);

    // ================== MAIN METHODS ==================

    /**
     * Membaca SMS dari inbox dan mengekstrak transaksi bank.
     * @return jumlah transaksi yang berhasil diimpor
     */
    public static int readInboxSms(Context context, DatabaseHelper db) {
        List<SmsTransaction> parsed = new ArrayList<>();

        try {
            Uri smsUri = Uri.parse("content://sms/inbox");
            String[] projection = {"_id", "address", "body", "date"};
            // Ambil 500 SMS terakhir
            Cursor cursor = context.getContentResolver().query(
                smsUri, projection, null, null, "date DESC LIMIT 500");

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long dateMs = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

                    if (sender == null || body == null) continue;

                    SmsTransaction tx = parseSms(sender.toUpperCase(), body, dateMs);
                    if (tx != null) parsed.add(tx);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Simpan ke database (hindari duplikat dengan cek tanggal+jumlah)
        int saved = 0;
        for (SmsTransaction smsTx : parsed) {
            try {
                // Cek duplikat: transaksi dengan jumlah dan tanggal sama
                boolean isDuplicate = db.isTransactionExists(smsTx.amount, smsTx.date);
                if (!isDuplicate) {
                    Transaction t = new Transaction(
                        smsTx.type, smsTx.amount, smsTx.category,
                        smsTx.description, smsTx.date, 0);
                    db.addTransaction(t);
                    saved++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return saved;
    }

    /**
     * Parse satu SMS dan kembalikan SmsTransaction jika valid, null jika bukan SMS bank.
     */
    public static SmsTransaction parseSms(String sender, String body, long dateMs) {
        if (body == null || body.length() < 10) return null;

        String bankName = detectBank(sender, body);
        if (bankName == null) return null;

        // Tentukan tipe (debit/credit)
        boolean isDebit = isDebitTransaction(body, bankName);
        boolean isCredit = isCreditTransaction(body, bankName);

        if (!isDebit && !isCredit) return null;

        // Ekstrak jumlah
        double amount = extractAmount(body, bankName, isDebit);
        if (amount <= 0) return null;

        // Ekstrak merchant/deskripsi
        String description = extractDescription(body, bankName);

        // Tentukan kategori berdasarkan merchant
        String category = guessCategoryFromMerchant(description, bankName);

        // Format tanggal
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(dateMs));

        String type = isCredit ? Transaction.TYPE_INCOME : Transaction.TYPE_EXPENSE;
        String desc = bankName + (description.isEmpty() ? "" : " - " + description);

        return new SmsTransaction(type, amount, category, desc, date, bankName);
    }

    // ================== BANK DETECTION ==================
    private static String detectBank(String sender, String body) {
        String combined = sender + " " + body.toUpperCase();

        for (String s : BCA_SENDERS) {
            if (combined.contains(s.toUpperCase())) return "BCA";
        }
        for (String s : MANDIRI_SENDERS) {
            if (combined.contains(s.toUpperCase())) return "Mandiri";
        }
        for (String s : BRI_SENDERS) {
            if (combined.contains(s.toUpperCase())) return "BRI";
        }
        for (String s : BNI_SENDERS) {
            if (combined.contains(s.toUpperCase())) return "BNI";
        }
        for (String s : GOPAY_SENDERS) {
            if (combined.contains(s.toUpperCase())) return "GoPay";
        }
        for (String s : OVO_SENDERS) {
            if (combined.contains(s.toUpperCase())) return "OVO";
        }
        for (String s : DANA_SENDERS) {
            if (combined.contains(s.toUpperCase())) return "DANA";
        }
        for (String s : SHOPEEPAY_SENDERS) {
            if (combined.contains(s.toUpperCase())) return "ShopeePay";
        }
        return null;
    }

    // ================== DEBIT/CREDIT DETECTION ==================
    private static boolean isDebitTransaction(String body, String bankName) {
        String lower = body.toLowerCase();
        return lower.contains("debit") || lower.contains("db ") ||
               lower.contains("pembelian") || lower.contains("pembayaran") ||
               lower.contains("digunakan") || lower.contains("belanja") ||
               lower.contains(" bayar ") || lower.contains("pengeluaran") ||
               lower.contains("transfer keluar") || lower.contains("withdrawal") ||
               (lower.contains("ovo pay") && !lower.contains("masuk")) ||
               (lower.contains("gopay") && lower.contains("ke ") && !lower.contains("top up"));
    }

    private static boolean isCreditTransaction(String body, String bankName) {
        String lower = body.toLowerCase();
        return lower.contains("kredit") || lower.contains("cr ") ||
               lower.contains("diterima") || lower.contains("transfer masuk") ||
               lower.contains("top up") || lower.contains("top-up") ||
               lower.contains("isi ulang") || lower.contains("masuk") ||
               lower.contains("terima") || lower.contains("deposit") ||
               lower.contains("gaji") || lower.contains("refund");
    }

    // ================== AMOUNT EXTRACTION ==================
    private static double extractAmount(String body, String bank, boolean isDebit) {
        Pattern[] patterns;

        switch (bank) {
            case "BCA":
                patterns = isDebit
                    ? new Pattern[]{BCA_DEBIT_PATTERN, GENERAL_AMOUNT_PATTERN}
                    : new Pattern[]{BCA_CREDIT_PATTERN, GENERAL_AMOUNT_PATTERN};
                break;
            case "Mandiri":
                patterns = isDebit
                    ? new Pattern[]{MANDIRI_DEBIT_PATTERN, GENERAL_AMOUNT_PATTERN}
                    : new Pattern[]{MANDIRI_CREDIT_PATTERN, GENERAL_AMOUNT_PATTERN};
                break;
            case "BRI":
                patterns = isDebit
                    ? new Pattern[]{BRI_DEBIT_PATTERN, GENERAL_AMOUNT_PATTERN}
                    : new Pattern[]{BRI_CREDIT_PATTERN, GENERAL_AMOUNT_PATTERN};
                break;
            case "GoPay":
                patterns = isDebit
                    ? new Pattern[]{GOPAY_PAYMENT_PATTERN, GENERAL_AMOUNT_PATTERN}
                    : new Pattern[]{GOPAY_TOPUP_PATTERN, GENERAL_AMOUNT_PATTERN};
                break;
            case "OVO":
                patterns = isDebit
                    ? new Pattern[]{OVO_PAYMENT_PATTERN, GENERAL_AMOUNT_PATTERN}
                    : new Pattern[]{OVO_CREDIT_PATTERN, GENERAL_AMOUNT_PATTERN};
                break;
            default:
                patterns = new Pattern[]{GENERAL_AMOUNT_PATTERN};
        }

        for (Pattern p : patterns) {
            Matcher m = p.matcher(body);
            if (m.find()) {
                try {
                    String raw = m.group(1).replaceAll("[.,](?=\\d{3})", "")
                                           .replaceAll(",", ".");
                    return Double.parseDouble(raw);
                } catch (Exception ignored) {}
            }
        }
        return 0;
    }

    // ================== DESCRIPTION EXTRACTION ==================
    private static String extractDescription(String body, String bank) {
        try {
            Matcher m = MERCHANT_PATTERN.matcher(body);
            if (m.find()) return m.group(1).trim();

            // Fallback: ambil kata setelah "di"/"pada"/"ke"
            String lower = body.toLowerCase();
            String[] keywords = {" di ", " pada ", " ke ", " for ", " at "};
            for (String kw : keywords) {
                int idx = lower.indexOf(kw);
                if (idx >= 0) {
                    String after = body.substring(idx + kw.length()).trim();
                    String[] words = after.split("\\s+");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < Math.min(words.length, 4); i++) {
                        if (words[i].matches(".*\\d.*") || words[i].length() < 2) break;
                        sb.append(words[i]).append(" ");
                    }
                    if (sb.length() > 2) return sb.toString().trim();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    // ================== CATEGORY GUESSING ==================
    private static String guessCategoryFromMerchant(String merchant, String bank) {
        String lower = merchant.toLowerCase();

        if (lower.contains("alfamart") || lower.contains("indomaret") ||
            lower.contains("supermarket") || lower.contains("market") ||
            lower.contains("hypermart") || lower.contains("carefour")) return "Belanja";

        if (lower.contains("grab") || lower.contains("gojek") || lower.contains("ojek") ||
            lower.contains("taxi") || lower.contains("transjakarta") ||
            lower.contains("commuter") || lower.contains("mrt") || lower.contains("lrt")) return "Transportasi";

        if (lower.contains("kfc") || lower.contains("mcd") || lower.contains("starbucks") ||
            lower.contains("resto") || lower.contains("warung") || lower.contains("makan") ||
            lower.contains("cafe") || lower.contains("pizza") || lower.contains("burger")) return "Makanan";

        if (lower.contains("pln") || lower.contains("telkom") || lower.contains("listrik") ||
            lower.contains("air") || lower.contains("indihome") || lower.contains("tagihan")) return "Tagihan";

        if (lower.contains("apotik") || lower.contains("apotek") || lower.contains("klinik") ||
            lower.contains("rumah sakit") || lower.contains("rs ") || lower.contains("dokter")) return "Kesehatan";

        if (lower.contains("netflix") || lower.contains("spotify") || lower.contains("youtube") ||
            lower.contains("game") || lower.contains("bioskop") || lower.contains("cinema")) return "Hiburan";

        if (lower.contains("top up") || lower.contains("isi ulang") || lower.contains("transfer")) {
            return "TOP UP".equals(bank) ? "Pemasukan" : "Transfer";
        }

        if (bank.equals("GoPay") || bank.equals("OVO") || bank.equals("DANA") ||
            bank.equals("ShopeePay")) return "Belanja";

        return "Lainnya";
    }

    // ================== DATA CLASS ==================
    public static class SmsTransaction {
        public final String type;
        public final double amount;
        public final String category;
        public final String description;
        public final String date;
        public final String bank;

        public SmsTransaction(String type, double amount, String category,
                              String description, String date, String bank) {
            this.type = type;
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
            this.bank = bank;
        }
    }
}
