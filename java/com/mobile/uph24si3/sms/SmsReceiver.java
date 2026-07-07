package com.mobile.uph24si3.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.mobile.uph24si3.database.DatabaseHelper;
import com.mobile.uph24si3.model.Transaction;

/**
 * SmsReceiver - Menerima SMS baru secara real-time dan langsung mencatat
 * transaksi bank ke BudgetKu tanpa perlu input manual.
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SMS_RECEIVED.equals(intent.getAction())) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        try {
            Object[] pdus = (Object[]) bundle.get("pdus");
            String format = bundle.getString("format");
            if (pdus == null) return;

            StringBuilder fullBody = new StringBuilder();
            String sender = "";

            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                if (sms != null) {
                    sender = sms.getOriginatingAddress();
                    fullBody.append(sms.getMessageBody());
                }
            }

            if (sender == null || fullBody.length() == 0) return;

            // Parse SMS
            long now = System.currentTimeMillis();
            SmsAutoReader.SmsTransaction smsTx = SmsAutoReader.parseSms(
                sender.toUpperCase(), fullBody.toString(), now);

            if (smsTx != null) {
                DatabaseHelper db = DatabaseHelper.getInstance(context);
                boolean isDuplicate = db.isTransactionExists(smsTx.amount, smsTx.date);

                if (!isDuplicate) {
                    Transaction t = new Transaction(
                        smsTx.type, smsTx.amount, smsTx.category,
                        smsTx.description, smsTx.date, 0);
                    db.addTransaction(t);

                    // Notifikasi ke user
                    String typeLabel = Transaction.TYPE_INCOME.equals(smsTx.type) ? "💚 Pemasukan" : "❤️ Pengeluaran";
                    Toast.makeText(context,
                        "📱 " + typeLabel + " otomatis dari " + smsTx.bank + " dicatat!",
                        Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
