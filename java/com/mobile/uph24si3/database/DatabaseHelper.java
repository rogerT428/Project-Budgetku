package com.mobile.uph24si3.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mobile.uph24si3.model.BankAccount;
import com.mobile.uph24si3.model.Budget;
import com.mobile.uph24si3.model.Category;
import com.mobile.uph24si3.model.RecurringBill;
import com.mobile.uph24si3.model.SavingsGoal;
import com.mobile.uph24si3.model.SplitBill;
import com.mobile.uph24si3.model.SplitBillMember;
import com.mobile.uph24si3.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "budgetku.db";
    private static final int DB_VERSION = 2; // Dinaikkan ke versi 2

    // Table names
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String TABLE_BANK_ACCOUNTS = "bank_accounts";
    private static final String TABLE_BUDGETS = "budgets";
    private static final String TABLE_SAVINGS = "savings_goals";
    private static final String TABLE_SPLIT_BILLS = "split_bills";
    private static final String TABLE_SPLIT_MEMBERS = "split_bill_members";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_RECURRING = "recurring_bills";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Transactions table - Added 'time' column for real-time tracking
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "type TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "category TEXT," +
                "description TEXT," +
                "date TEXT," +
                "time TEXT," +
                "bank_account_id INTEGER DEFAULT 0" +
                ")");

        // Bank Accounts table
        db.execSQL("CREATE TABLE " + TABLE_BANK_ACCOUNTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "bank_name TEXT NOT NULL," +
                "account_number TEXT," +
                "account_holder TEXT," +
                "balance REAL DEFAULT 0," +
                "bank_type TEXT," +
                "color TEXT" +
                ")");

        // Budgets table
        db.execSQL("CREATE TABLE " + TABLE_BUDGETS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category TEXT NOT NULL," +
                "monthly_limit REAL NOT NULL," +
                "used_amount REAL DEFAULT 0," +
                "month TEXT," +
                "color TEXT" +
                ")");

        // Savings Goals table
        db.execSQL("CREATE TABLE " + TABLE_SAVINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "target_amount REAL NOT NULL," +
                "current_amount REAL DEFAULT 0," +
                "target_date TEXT," +
                "icon TEXT," +
                "is_completed INTEGER DEFAULT 0" +
                ")");

        // Split Bills table
        db.execSQL("CREATE TABLE " + TABLE_SPLIT_BILLS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "total_amount REAL NOT NULL," +
                "date TEXT," +
                "notes TEXT," +
                "is_settled INTEGER DEFAULT 0" +
                ")");

        // Split Bill Members table
        db.execSQL("CREATE TABLE " + TABLE_SPLIT_MEMBERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "split_bill_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "amount_owed REAL NOT NULL," +
                "is_paid INTEGER DEFAULT 0," +
                "paid_date TEXT," +
                "FOREIGN KEY(split_bill_id) REFERENCES split_bills(id)" +
                ")");

        // Categories table
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "icon TEXT," +
                "color TEXT," +
                "type TEXT DEFAULT 'EXPENSE'," +
                "is_default INTEGER DEFAULT 0" +
                ")");

        // Recurring Bills table
        db.execSQL("CREATE TABLE " + TABLE_RECURRING + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "category TEXT," +
                "due_day INTEGER," +
                "frequency TEXT DEFAULT 'MONTHLY'," +
                "is_active INTEGER DEFAULT 1," +
                "next_due_date TEXT," +
                "is_paid INTEGER DEFAULT 0" +
                ")");

        // Insert default categories
        insertDefaultCategories(db);
    }

    /**
     * Mengisi data simulasi dari tanggal 1 bulan ini agar riwayat tidak kosong.
     */
    public void seedInitialData() {
        SQLiteDatabase db = getWritableDatabase();
        // Cek jika sudah ada data, jangan isi lagi agar tidak duplikat
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TRANSACTIONS, null);
        cursor.moveToFirst();
        if (cursor.getInt(0) > 0) {
            cursor.close();
            return; 
        }
        cursor.close();

        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String monthPrefix = sdfDate.format(new Date());

        String[] incomeCats = {"Gaji", "Freelance", "Investasi"};
        String[] expenseCats = {"Makanan & Minum", "Transportasi", "Belanja", "Tagihan"};
        
        // Loop dari tanggal 1 sampai hari ini
        for (int i = 1; i <= today; i++) {
            String dateStr = String.format(Locale.getDefault(), "%s-%02d", monthPrefix, i);
            
            // Tambahkan Pemasukan di tanggal 1
            if (i == 1) {
                addTransaction(new Transaction("INCOME", 5000000, "Gaji", "Gaji Bulanan", dateStr, "08:00:00", 0));
            }

            // Tambahkan variasi pengeluaran harian
            if (i % 2 == 0) {
                addTransaction(new Transaction("EXPENSE", 25000, "Transportasi", "Ojek Online", dateStr, "09:15:00", 0));
                addTransaction(new Transaction("EXPENSE", 45000, "Makanan & Minum", "Makan Siang", dateStr, "12:30:00", 0));
            } else {
                addTransaction(new Transaction("EXPENSE", 15000, "Makanan & Minum", "Kopi Pagi", dateStr, "10:00:00", 0));
                addTransaction(new Transaction("EXPENSE", 120000, "Belanja", "Kebutuhan Dapur", dateStr, "19:45:00", 0));
            }
            
            if (i == 10 || i == 20) {
                addTransaction(new Transaction("INCOME", 250000, "Freelance", "Project Sampingan", dateStr, "14:00:00", 0));
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Migrasi dari v1 ke v2: Tambahkan kolom 'time' jika belum ada
            try {
                db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN time TEXT");
            } catch (Exception ignored) {}
        }
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[][] categories = {
            // Expense categories
            {"Makanan & Minum", "🍔", "#FF6B35", "EXPENSE"},
            {"Transportasi", "🚗", "#4ECDC4", "EXPENSE"},
            {"Belanja", "🛍️", "#A8E6CF", "EXPENSE"},
            {"Kesehatan", "💊", "#FF8B94", "EXPENSE"},
            {"Hiburan", "🎮", "#C7B3FF", "EXPENSE"},
            {"Pendidikan", "📚", "#FFD93D", "EXPENSE"},
            {"Tagihan", "💡", "#6BCB77", "EXPENSE"},
            {"Rumah", "🏠", "#4D96FF", "EXPENSE"},
            // Income categories
            {"Gaji", "💼", "#52C41A", "INCOME"},
            {"Freelance", "💻", "#1890FF", "INCOME"},
            {"Investasi", "📈", "#00D4AA", "INCOME"},
            {"Lainnya", "📦", "#8B949E", "BOTH"}
        };

        for (String[] cat : categories) {
            ContentValues cv = new ContentValues();
            cv.put("name", cat[0]);
            cv.put("icon", cat[1]);
            cv.put("color", cat[2]);
            cv.put("type", cat[3]);
            cv.put("is_default", 1);
            db.insert(TABLE_CATEGORIES, null, cv);
        }
    }

    // ==================== TRANSACTION CRUD ====================

    public long addTransaction(Transaction t) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("type", t.getType());
        cv.put("amount", t.getAmount());
        cv.put("category", t.getCategory());
        cv.put("description", t.getDescription());
        cv.put("date", t.getDate());
        cv.put("time", t.getTime() != null ? t.getTime() : new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
        cv.put("bank_account_id", t.getBankAccountId());
        long id = db.insert(TABLE_TRANSACTIONS, null, cv);

        // Update budget used amount
        if (t.isExpense()) {
            updateBudgetUsedAmount(t.getCategory(), t.getAmount(), getCurrentMonth());
        }
        return id;
    }

    /**
     * Cek apakah transaksi dengan jumlah dan tanggal yang sama sudah ada.
     * Digunakan untuk mencegah duplikat saat import SMS.
     */
    public boolean isTransactionExists(double amount, String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
            "SELECT COUNT(*) FROM " + TABLE_TRANSACTIONS +
            " WHERE amount = ? AND date = ?",
            new String[]{String.valueOf(amount), date});
        boolean exists = false;
        if (c.moveToFirst()) {
            exists = c.getInt(0) > 0;
        }
        c.close();
        return exists;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY date DESC, id DESC", null);
        while (c.moveToNext()) {
            list.add(cursorToTransaction(c));
        }
        c.close();
        return list;
    }

    public List<Transaction> getTransactionsByMonth(String month) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS +
                " WHERE date LIKE ? ORDER BY date DESC, id DESC",
                new String[]{month + "%"});
        while (c.moveToNext()) {
            list.add(cursorToTransaction(c));
        }
        c.close();
        return list;
    }

    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS +
                " ORDER BY date DESC, id DESC LIMIT " + limit, null);
        while (c.moveToNext()) {
            list.add(cursorToTransaction(c));
        }
        c.close();
        return list;
    }

    public List<Transaction> getTransactionsByType(String type) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS +
                " WHERE type = ? ORDER BY date DESC", new String[]{type});
        while (c.moveToNext()) {
            list.add(cursorToTransaction(c));
        }
        c.close();
        return list;
    }

    public boolean deleteTransaction(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_TRANSACTIONS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public double getTotalIncome(String month) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_TRANSACTIONS +
                " WHERE type='INCOME' AND date LIKE ?", new String[]{month + "%"});
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        return total;
    }

    public double getTotalExpense(String month) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_TRANSACTIONS +
                " WHERE type='EXPENSE' AND date LIKE ?", new String[]{month + "%"});
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        return total;
    }

    public List<String[]> getExpenseByCategory(String month) {
        List<String[]> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT category, SUM(amount) FROM " + TABLE_TRANSACTIONS +
                " WHERE type='EXPENSE' AND date LIKE ? GROUP BY category ORDER BY SUM(amount) DESC",
                new String[]{month + "%"});
        while (c.moveToNext()) {
            result.add(new String[]{c.getString(0), String.valueOf(c.getDouble(1))});
        }
        c.close();
        return result;
    }

    private Transaction cursorToTransaction(Cursor c) {
        Transaction t = new Transaction();
        t.setId(c.getLong(c.getColumnIndexOrThrow("id")));
        t.setType(c.getString(c.getColumnIndexOrThrow("type")));
        t.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));
        t.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
        t.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
        t.setDate(c.getString(c.getColumnIndexOrThrow("date")));
        int timeIdx = c.getColumnIndex("time");
        if (timeIdx != -1) t.setTime(c.getString(timeIdx));
        t.setBankAccountId(c.getLong(c.getColumnIndexOrThrow("bank_account_id")));
        return t;
    }

    // ==================== BANK ACCOUNT CRUD ====================

    public long addBankAccount(BankAccount ba) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("bank_name", ba.getBankName());
        cv.put("account_number", ba.getAccountNumber());
        cv.put("account_holder", ba.getAccountHolder());
        cv.put("balance", ba.getBalance());
        cv.put("bank_type", ba.getBankType());
        cv.put("color", ba.getColor());
        return db.insert(TABLE_BANK_ACCOUNTS, null, cv);
    }

    public List<BankAccount> getAllBankAccounts() {
        List<BankAccount> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_BANK_ACCOUNTS, null);
        while (c.moveToNext()) {
            BankAccount ba = new BankAccount();
            ba.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            ba.setBankName(c.getString(c.getColumnIndexOrThrow("bank_name")));
            ba.setAccountNumber(c.getString(c.getColumnIndexOrThrow("account_number")));
            ba.setAccountHolder(c.getString(c.getColumnIndexOrThrow("account_holder")));
            ba.setBalance(c.getDouble(c.getColumnIndexOrThrow("balance")));
            ba.setBankType(c.getString(c.getColumnIndexOrThrow("bank_type")));
            ba.setColor(c.getString(c.getColumnIndexOrThrow("color")));
            list.add(ba);
        }
        c.close();
        return list;
    }

    /**
     * Total saldo = Saldo awal semua rekening + semua pemasukan - semua pengeluaran.
     * Otomatis berubah setiap ada transaksi baru - tanpa perlu update manual.
     */
    public double getTotalBalance() {
        SQLiteDatabase db = getReadableDatabase();

        // Saldo awal dari semua rekening yang terhubung
        Cursor c1 = db.rawQuery("SELECT COALESCE(SUM(balance), 0) FROM " + TABLE_BANK_ACCOUNTS, null);
        double initialTotal = 0;
        if (c1.moveToFirst()) initialTotal = c1.getDouble(0);
        c1.close();

        // Total semua pemasukan (semua waktu)
        Cursor c2 = db.rawQuery("SELECT COALESCE(SUM(amount), 0) FROM " + TABLE_TRANSACTIONS + " WHERE type='INCOME'", null);
        double totalIncome = 0;
        if (c2.moveToFirst()) totalIncome = c2.getDouble(0);
        c2.close();

        // Total semua pengeluaran (semua waktu)
        Cursor c3 = db.rawQuery("SELECT COALESCE(SUM(amount), 0) FROM " + TABLE_TRANSACTIONS + " WHERE type='EXPENSE'", null);
        double totalExpense = 0;
        if (c3.moveToFirst()) totalExpense = c3.getDouble(0);
        c3.close();

        return initialTotal + totalIncome - totalExpense;
    }

    public boolean deleteBankAccount(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_BANK_ACCOUNTS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ==================== BUDGET CRUD ====================

    public long addBudget(Budget b) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category", b.getCategory());
        cv.put("monthly_limit", b.getMonthlyLimit());
        cv.put("used_amount", b.getUsedAmount());
        cv.put("month", b.getMonth());
        cv.put("color", b.getColor());
        return db.insert(TABLE_BUDGETS, null, cv);
    }

    public List<Budget> getBudgetsByMonth(String month) {
        List<Budget> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_BUDGETS + " WHERE month = ?", new String[]{month});
        while (c.moveToNext()) {
            list.add(cursorToBudget(c));
        }
        c.close();
        return list;
    }

    private void updateBudgetUsedAmount(String category, double amount, String month) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_BUDGETS +
                " SET used_amount = used_amount + ? WHERE category = ? AND month = ?",
                new Object[]{amount, category, month});
    }

    public boolean deleteBudget(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_BUDGETS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    private Budget cursorToBudget(Cursor c) {
        Budget b = new Budget();
        b.setId(c.getLong(c.getColumnIndexOrThrow("id")));
        b.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
        b.setMonthlyLimit(c.getDouble(c.getColumnIndexOrThrow("monthly_limit")));
        b.setUsedAmount(c.getDouble(c.getColumnIndexOrThrow("used_amount")));
        b.setMonth(c.getString(c.getColumnIndexOrThrow("month")));
        b.setColor(c.getString(c.getColumnIndexOrThrow("color")));
        return b;
    }

    // ==================== SAVINGS CRUD ====================

    public long addSavingsGoal(SavingsGoal sg) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", sg.getName());
        cv.put("target_amount", sg.getTargetAmount());
        cv.put("current_amount", sg.getCurrentAmount());
        cv.put("target_date", sg.getTargetDate());
        cv.put("icon", sg.getIcon());
        cv.put("is_completed", sg.isCompleted() ? 1 : 0);
        return db.insert(TABLE_SAVINGS, null, cv);
    }

    public List<SavingsGoal> getAllSavingsGoals() {
        List<SavingsGoal> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SAVINGS + " ORDER BY id DESC", null);
        while (c.moveToNext()) {
            SavingsGoal sg = new SavingsGoal();
            sg.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            sg.setName(c.getString(c.getColumnIndexOrThrow("name")));
            sg.setTargetAmount(c.getDouble(c.getColumnIndexOrThrow("target_amount")));
            sg.setCurrentAmount(c.getDouble(c.getColumnIndexOrThrow("current_amount")));
            sg.setTargetDate(c.getString(c.getColumnIndexOrThrow("target_date")));
            sg.setIcon(c.getString(c.getColumnIndexOrThrow("icon")));
            sg.setCompleted(c.getInt(c.getColumnIndexOrThrow("is_completed")) == 1);
            list.add(sg);
        }
        c.close();
        return list;
    }

    public boolean updateSavingsAmount(long id, double newAmount) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("current_amount", newAmount);
        return db.update(TABLE_SAVINGS, cv, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteSavingsGoal(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_SAVINGS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ==================== SPLIT BILL CRUD ====================

    public long addSplitBill(SplitBill sb) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", sb.getTitle());
        cv.put("total_amount", sb.getTotalAmount());
        cv.put("date", sb.getDate());
        cv.put("notes", sb.getNotes());
        cv.put("is_settled", sb.isSettled() ? 1 : 0);
        long splitId = db.insert(TABLE_SPLIT_BILLS, null, cv);

        // Insert members
        if (sb.getMembers() != null) {
            for (SplitBillMember member : sb.getMembers()) {
                addSplitBillMember(member, splitId);
            }
        }
        return splitId;
    }

    private void addSplitBillMember(SplitBillMember member, long splitBillId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("split_bill_id", splitBillId);
        cv.put("name", member.getName());
        cv.put("amount_owed", member.getAmountOwed());
        cv.put("is_paid", member.isPaid() ? 1 : 0);
        cv.put("paid_date", member.getPaidDate());
        db.insert(TABLE_SPLIT_MEMBERS, null, cv);
    }

    public List<SplitBill> getAllSplitBills() {
        List<SplitBill> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SPLIT_BILLS + " ORDER BY id DESC", null);
        while (c.moveToNext()) {
            SplitBill sb = new SplitBill();
            sb.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            sb.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
            sb.setTotalAmount(c.getDouble(c.getColumnIndexOrThrow("total_amount")));
            sb.setDate(c.getString(c.getColumnIndexOrThrow("date")));
            sb.setNotes(c.getString(c.getColumnIndexOrThrow("notes")));
            sb.setSettled(c.getInt(c.getColumnIndexOrThrow("is_settled")) == 1);
            sb.setMembers(getMembersBySplitId(sb.getId()));
            list.add(sb);
        }
        c.close();
        return list;
    }

    public List<SplitBillMember> getMembersBySplitId(long splitId) {
        List<SplitBillMember> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SPLIT_MEMBERS +
                " WHERE split_bill_id = ?", new String[]{String.valueOf(splitId)});
        while (c.moveToNext()) {
            SplitBillMember m = new SplitBillMember();
            m.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            m.setSplitBillId(c.getLong(c.getColumnIndexOrThrow("split_bill_id")));
            m.setName(c.getString(c.getColumnIndexOrThrow("name")));
            m.setAmountOwed(c.getDouble(c.getColumnIndexOrThrow("amount_owed")));
            m.setPaid(c.getInt(c.getColumnIndexOrThrow("is_paid")) == 1);
            m.setPaidDate(c.getString(c.getColumnIndexOrThrow("paid_date")));
            list.add(m);
        }
        c.close();
        return list;
    }

    public boolean updateMemberPaidStatus(long memberId, boolean isPaid) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_paid", isPaid ? 1 : 0);
        if (isPaid) {
            cv.put("paid_date", getCurrentDate());
        }
        return db.update(TABLE_SPLIT_MEMBERS, cv, "id = ?", new String[]{String.valueOf(memberId)}) > 0;
    }

    public boolean deleteSplitBill(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SPLIT_MEMBERS, "split_bill_id = ?", new String[]{String.valueOf(id)});
        return db.delete(TABLE_SPLIT_BILLS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ==================== CATEGORY CRUD ====================

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES + " ORDER BY is_default DESC, name ASC", null);
        while (c.moveToNext()) {
            Category cat = new Category();
            cat.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            cat.setName(c.getString(c.getColumnIndexOrThrow("name")));
            cat.setIcon(c.getString(c.getColumnIndexOrThrow("icon")));
            cat.setColor(c.getString(c.getColumnIndexOrThrow("color")));
            cat.setType(c.getString(c.getColumnIndexOrThrow("type")));
            cat.setDefault(c.getInt(c.getColumnIndexOrThrow("is_default")) == 1);
            list.add(cat);
        }
        c.close();
        return list;
    }

    public List<Category> getCategoriesByType(String type) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES +
                " WHERE type = ? OR type = 'BOTH' ORDER BY is_default DESC, name ASC",
                new String[]{type});
        while (c.moveToNext()) {
            Category cat = new Category();
            cat.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            cat.setName(c.getString(c.getColumnIndexOrThrow("name")));
            cat.setIcon(c.getString(c.getColumnIndexOrThrow("icon")));
            cat.setColor(c.getString(c.getColumnIndexOrThrow("color")));
            cat.setType(c.getString(c.getColumnIndexOrThrow("type")));
            cat.setDefault(c.getInt(c.getColumnIndexOrThrow("is_default")) == 1);
            list.add(cat);
        }
        c.close();
        return list;
    }

    public long addCategory(Category cat) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", cat.getName());
        cv.put("icon", cat.getIcon());
        cv.put("color", cat.getColor());
        cv.put("type", cat.getType());
        cv.put("is_default", 0);
        return db.insert(TABLE_CATEGORIES, null, cv);
    }

    public boolean deleteCategory(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_CATEGORIES, "id = ? AND is_default = 0", new String[]{String.valueOf(id)}) > 0;
    }

    // ==================== RECURRING BILL CRUD ====================

    public long addRecurringBill(RecurringBill rb) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", rb.getName());
        cv.put("amount", rb.getAmount());
        cv.put("category", rb.getCategory());
        cv.put("due_day", rb.getDueDay());
        cv.put("frequency", rb.getFrequency());
        cv.put("is_active", rb.isActive() ? 1 : 0);
        cv.put("next_due_date", rb.getNextDueDate());
        cv.put("is_paid", rb.isPaid() ? 1 : 0);
        return db.insert(TABLE_RECURRING, null, cv);
    }

    public List<RecurringBill> getAllRecurringBills() {
        List<RecurringBill> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_RECURRING + " ORDER BY due_day ASC", null);
        while (c.moveToNext()) {
            RecurringBill rb = new RecurringBill();
            rb.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            rb.setName(c.getString(c.getColumnIndexOrThrow("name")));
            rb.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));
            rb.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
            rb.setDueDay(c.getInt(c.getColumnIndexOrThrow("due_day")));
            rb.setFrequency(c.getString(c.getColumnIndexOrThrow("frequency")));
            rb.setActive(c.getInt(c.getColumnIndexOrThrow("is_active")) == 1);
            rb.setNextDueDate(c.getString(c.getColumnIndexOrThrow("next_due_date")));
            rb.setPaid(c.getInt(c.getColumnIndexOrThrow("is_paid")) == 1);
            list.add(rb);
        }
        c.close();
        return list;
    }

    public boolean deleteRecurringBill(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_RECURRING, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ==================== HELPERS ====================

    public String getCurrentMonth() {
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
    }

    public String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public String generateFullReport(String month) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LAPORAN KEUANGAN BUDGETKU ===\n");
        sb.append("Bulan: ").append(month).append("\n");
        sb.append("Generated: ").append(getCurrentDate()).append("\n\n");

        double income = getTotalIncome(month);
        double expense = getTotalExpense(month);
        sb.append("--- RINGKASAN ---\n");
        sb.append(String.format("Total Pemasukan  : Rp %,.0f\n", income));
        sb.append(String.format("Total Pengeluaran: Rp %,.0f\n", expense));
        sb.append(String.format("Selisih          : Rp %,.0f\n\n", income - expense));

        sb.append("--- PENGELUARAN PER KATEGORI ---\n");
        List<String[]> byCategory = getExpenseByCategory(month);
        for (String[] cat : byCategory) {
            sb.append(String.format("%-20s: Rp %,.0f\n", cat[0], Double.parseDouble(cat[1])));
        }

        sb.append("\n--- TRANSAKSI ---\n");
        List<Transaction> transactions = getTransactionsByMonth(month);
        for (Transaction t : transactions) {
            sb.append(String.format("[%s] %s %s - Rp %,.0f (%s)\n",
                    t.getDate(),
                    t.isIncome() ? "+" : "-",
                    t.getCategory(),
                    t.getAmount(),
                    t.getDescription() != null ? t.getDescription() : ""));
        }

        sb.append("\n--- TOTAL SALDO BANK ---\n");
        sb.append(String.format("Total: Rp %,.0f\n", getTotalBalance()));

        return sb.toString();
    }
}
