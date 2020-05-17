package com.appdev.jayesh.kiranastoremanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.TransactionsRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.Model.MinTransaction;
import com.appdev.jayesh.kiranastoremanager.Model.Transaction;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CashSales extends AppCompatActivity {

    private static final String TAG = "CashSales";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    List<Transaction> transactionList = new ArrayList<>();
    TransactionsRecyclerViewAdapter adapter = new TransactionsRecyclerViewAdapter(transactionList);
    RecyclerView recyclerView;
    DocumentReference documentReference;

    private ProgressDialog pDialog;


    EditText dt;
    int mYear;
    int mMonth;
    int mDay;

    String title;
    String transactionType;

    EditText itemName, etQuantity, etPrice, etAmount, etUOM;
    ImageView etNote;
    Double quantity, price;
    int sign;

    WriteBatch batch;

    boolean save;

    List<Accounts> accountList = new ArrayList<Accounts>();
    List<Items> itemsList = new ArrayList<Items>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_sales);
        Intent intent = getIntent();
        title = intent.getStringExtra(Constants.TITLE);
        transactionType = intent.getStringExtra(Constants.TRANSACTIONTYPE);
        sign = intent.getIntExtra(Constants.SIGN, 1);
        itemsList = (List<Items>) intent.getSerializableExtra("itemlist");
        accountList = (List<Accounts>) intent.getSerializableExtra("accountlist");

        this.setTitle(title);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection(Constants.USERS).document(mAuth.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(CashSales.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        dt = findViewById(R.id.date);
        itemName = findViewById(R.id.itemName);
        etQuantity = findViewById(R.id.quantity);
        etPrice = findViewById(R.id.price);
        etAmount = findViewById(R.id.amount);
        etNote = findViewById(R.id.note);
        etUOM = findViewById(R.id.uom);

        writeLogCat();
        setDate();
        loadItemData();
        setListeners();

    }

    private void loadItemData() {
        for (Items item : itemsList) {
            if (item.getUsedFor().get(transactionType)) {
                Transaction transaction = new Transaction();
                transaction.setItemName(item.getName());
                transaction.setItemId(item.getId());
                transaction.setPrice(item.getPrice());
                transaction.setInventory(item.getIsInventory());
                transaction.setBatchItem(item.getIsBatchItem());
                transaction.setProcessed(item.getIsProcessed());
                transaction.setRawMaterial(item.getRawMaterial());
                transactionList.add(transaction);
            }
        }

        /*firebaseFirestore = FirebaseFirestore.getInstance();
        showProgressBar(true);
        Query query = firebaseFirestore.collection(Constants.USERS).document(user.getUid())
                .collection(Constants.ITEMS).whereEqualTo("usedFor." + transactionType, true)
                .orderBy("name", Query.Direction.ASCENDING);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                    Items item = q.toObject(Items.class);

                    Transaction transaction = new Transaction();
                    transaction.setItemName(item.getName());
                    transaction.setItemId(q.getId());
                    transaction.setPrice(item.getPrice());
                    transaction.setInventory(item.getIsInventory());
                    transaction.setBatchItem(item.getIsBatchItem());
                    transaction.setProcessed(item.getIsProcessed());
                    transaction.setRawMaterial(item.getRawMaterial());
                    transactionList.add(transaction);

                }
                showProgressBar(false);
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showProgressBar(false);
            }
        });
*/
    }

    private void setListeners() {
        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double qty = UHelper.parseDouble(etQuantity.getText().toString());
                Double prc = UHelper.parseDouble(etPrice.getText().toString());
                etAmount.setText(String.format("%.2f", (qty * prc)));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double qty = UHelper.parseDouble(etQuantity.getText().toString());
                Double prc = UHelper.parseDouble(etPrice.getText().toString());
                etAmount.setText(String.format("%.2f", (qty * prc)));
            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println("Text after changed");

            }
        });
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("Notes");
                alert.setIcon(R.drawable.ic_event_note_black_24dp);
                final EditText input = new EditText(v.getContext());
                if (etNote.getTag() != null)
                    input.setText(etNote.getTag().toString());
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = input.getText().toString();
                        etNote.setTag(text);
                        if (text.trim().length() > 0) {
                            etNote.setColorFilter(Color.GREEN);
                        } else
                            etNote.setColorFilter(Color.BLACK);

                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alert.show();
            }
        });
    }

    private void resetFreeTextView() {
        itemName.setText(null);
        etQuantity.setText(null);
        etPrice.setText(null);
        etAmount.setText(null);
        etNote.setTag(null);
        etNote.setColorFilter(Color.BLACK);
        etUOM.setText(null);


        quantity = 0.0;
        price = 0.0;
    }

    public void save(View view) {
        batch = firebaseFirestore.batch();
        Log.d("jayesh", "inside Save");
        saveListItems();
        saveFreeItems();
        Log.d("jayesh", "after save list items" + save);

        /* if (save)*/
        batchWrite();
    }

    private void saveListItems() {
        if (adapter.total <= 0) {
            return;
        }

        for (int i = 0; i < adapter.transactionList.size(); i++) {
            Transaction t = adapter.transactionList.get(i);
            MinTransaction mt = new MinTransaction();

            if (t.getAmount() > 0) {
                DocumentReference newDocument = documentReference.collection(Constants.TRANSACTIONS).document();
                double temp = t.getAmount() * sign;
                t.setAmount(temp);
                t.setTransactionType(transactionType);
                t.setAccountName(transactionType);
                String datetime = dt.getText().toString() + " " + UHelper.getTime("time");
                t.setTimeInMilli(UHelper.ddmmyyyyhmsTomili(datetime));
                t.setTimestamp(System.currentTimeMillis());
                t.setId(newDocument.getId());
                t.setAccountId(transactionType);
                t.setTransaction(transactionType);

/*                if ((t.getTransactionType().equals(Constants.CASHSALES))
                        && (t.getInventory() || t.getRawMaterial() != null)) {
                    if (t.getQuantity() <= 0) {
                        toast("Item " + t.getItemName() + " must have quantity");
                        save = false;
                        return;
                    }
                }
                if ((t.getTransactionType().equals(Constants.CASHPURCHASE))
                        && (t.getInventory())) {
                    if (t.getQuantity() <= 0) {
                        toast("Item " + t.getItemName() + " must have quantity");
                        save = false;
                        return;
                    }
                }*/

                mt.setAccountId(t.getAccountId());
                mt.setAccountName(t.getAccountName());
                mt.setAmount(t.getAmount());
                mt.setId(t.getId());
                mt.setItemId(t.getItemId());
                mt.setItemName(t.getItemName());
                mt.setNotes(t.getNotes());
                mt.setPrice(t.getPrice());
                mt.setQuantity(t.getQuantity());
                mt.setTimeInMilli(t.getTimeInMilli());
                mt.setTimestamp(t.getTimestamp());
                mt.setTransaction(t.getTransaction());
                mt.setTransactionType(t.getTransactionType());
                mt.setUom(t.getUom());

                save = true;
                //Update Postings for Days Sales
                Map<String, Object> data = new HashMap<>();
                data.put(transactionType, FieldValue.increment(t.getAmount()));
                data.put(Constants.TIMESTAMP, FieldValue.serverTimestamp());
                data.put("timeInMilli", UHelper.ddmmyyyyhmsTomili(datetime));
                DocumentReference daySales = documentReference.collection(Constants.POSTINGS).document(dt.getText().toString());

                batch.set(daySales, data, SetOptions.merge());
                batch.set(newDocument, mt);

                updateInventory(t, i, sign);
                adapter.transactionList.get(i).setQuantity(0);
            }
        }
    }

    private void updateInventory(Transaction t, int i, int sig) {
        //update Inventory
        DocumentReference updateInventory = null;
        Map<String, Object> inv = new HashMap<>();
        //thi si because in cash purchase amount is negative
        sig = sig * -1;
        inv.put(Constants.RAWSTOCK, FieldValue.increment(t.getQuantity() * sig));

        if (t.getTransactionType().equals(Constants.CASHSALES) && (t.getInventory() || t.getRawMaterial() != null)) {
            if (t.getRawMaterial() != null) {
                updateInventory = documentReference.collection(Constants.ITEMS).document(t.getRawMaterial());
            } else {
                updateInventory = documentReference.collection(Constants.ITEMS).document(t.getItemId());
            }
        } else if (t.getTransactionType().equals(Constants.CASHPURCHASE) && (t.getInventory())) {
            updateInventory = documentReference.collection(Constants.ITEMS).document(t.getItemId());
/*            if (t.getRawMaterial() != null) {
                updateInventory = documentReference.collection(Constants.ITEMS).document(t.getRawMaterial());
            } else {
                updateInventory = documentReference.collection(Constants.ITEMS).document(t.getItemId());
            }*/
        }
        if (updateInventory != null) batch.set(updateInventory, inv, SetOptions.merge());

    }

    private void saveFreeItems() {

        if (isFreeItemAvailable()) {
            Transaction t = new Transaction();
            MinTransaction mt = new MinTransaction();
            DocumentReference newDocument = documentReference.collection(Constants.TRANSACTIONS).document();
            String datetime = dt.getText().toString() + " " + UHelper.getTime("time");
            t.setItemName(itemName.getText().toString());
            t.setTransactionType(transactionType);
            t.setAccountName(transactionType);
            t.setTimeInMilli(UHelper.ddmmyyyyhmsTomili(datetime));
            t.setId(newDocument.getId());
            if (etNote.getTag() != null)
                t.setNotes(etNote.getTag().toString());
            t.setQuantity(UHelper.parseDouble(etQuantity.getText().toString()));
            t.setPrice(UHelper.parseDouble(etPrice.getText().toString()));
            t.setAmount(UHelper.parseDouble(etAmount.getText().toString()) * sign);
            t.setTimestamp(System.currentTimeMillis());
            t.setAccountId(transactionType);
            t.setTransaction(transactionType);
            t.setUom(etUOM.getText().toString());

            mt.setAccountId(t.getAccountId());
            mt.setAccountName(t.getAccountName());
            mt.setAmount(t.getAmount());
            mt.setId(t.getId());
            mt.setItemId(t.getItemId());
            mt.setItemName(t.getItemName());
            mt.setNotes(t.getNotes());
            mt.setPrice(t.getPrice());
            mt.setQuantity(t.getQuantity());
            mt.setTimeInMilli(t.getTimeInMilli());
            mt.setTimestamp(t.getTimestamp());
            mt.setTransaction(t.getTransaction());
            mt.setTransactionType(t.getTransactionType());
            mt.setUom(t.getUom());

            final DocumentReference accountEntry = documentReference.collection(Constants.POSTINGS).document(dt.getText().toString());
            final Map<String, Object> data = new HashMap<>();
            data.put(transactionType, FieldValue.increment(t.getAmount()));
            data.put(Constants.TIMESTAMP, FieldValue.serverTimestamp());
            data.put("timeInMilli", UHelper.ddmmyyyyhmsTomili(datetime));
            batch.set(newDocument, mt);
            batch.set(accountEntry, data, SetOptions.merge());

            save = true;
        }
    }

    private void setDate() {
        dt.setText(UHelper.setPresentDateddMMyyyy());
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
    }

    public void datePicker(final View viewq) {
        {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            String month = UHelper.intLeadingZero(2, (monthOfYear + 1));
                            String date = UHelper.intLeadingZero(2, dayOfMonth);
                            dt.setText(date + "-" + month + "-" + year);
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add) {
        }
        return super.onOptionsItemSelected(item);
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void showProgressBar(final boolean visibility) {

        runOnUiThread(new Runnable() {
            public void run() {
                if (visibility)
                    showpDialog();
                else hidepDialog();
            }
        });
    }

    private void showProgressBar(final boolean visibility, final String message) {

        runOnUiThread(new Runnable() {
            public void run() {
                pDialog.setMessage(message);
                if (visibility)
                    showpDialog();
                else hidepDialog();
            }
        });
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void batchWrite() {

        if (!isFreeItemAvailable() && !isItemListAvailable())
            return;

        showProgressBar(true, "Please wait, Saving Data");
        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showProgressBar(false);
                toast("Data Saved");
                adapter.notifyDataSetChanged();
                resetFreeTextView();
                setDate();
                save = false;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showProgressBar(false);
                toast("Failed to update, please enter again!");
            }
        });

    }

    public boolean isItemListAvailable() {
        return adapter.total > 0;
    }

    public boolean isFreeItemAvailable() {
        return UHelper.parseDouble(etAmount.getText().toString()) > 0 && itemName.getText().toString().length() > 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    protected void writeLogCat() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            ;
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append("\n");
            }

            //Convert log to string
            final String logString = new String(log.toString());

            //Create txt file in SD Card
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + File.separator + "Log File");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, "logcat.txt");

            //To write logcat in text file
            FileOutputStream fout = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fout);

            //Writing the string to file
            osw.write(logString);
            osw.flush();
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
