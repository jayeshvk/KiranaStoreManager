package com.appdev.jayesh.kiranastoremanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.TransactionsRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.Model.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreditSales extends AppCompatActivity {

    private static final String TAG = "CreditSales";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    List<Transaction> transactionList = new ArrayList<>();

    private ProgressDialog pDialog;
    TransactionsRecyclerViewAdapter adapter;

    RecyclerView recyclerView;

    EditText dt;
    int mYear;
    int mMonth;
    int mDay;

    DocumentReference accountEntry, documentReference;

    List<Accounts> accountsList = new ArrayList<>();

    ArrayAdapter<Accounts> accountsArrayAdapter;

    Spinner accountSpinner;

    String title, transactionType, account, transactionTypeReverse;

    EditText itemName, etQuantity, etPrice, etAmount;
    ImageView etNote;
    Double quantity, price;

    WriteBatch batch;

    int sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_sales);
        Intent intent = getIntent();
        title = intent.getStringExtra(Constants.TITLE);
        transactionType = intent.getStringExtra(Constants.TRANSACTIONTYPE);
        account = intent.getStringExtra(Constants.ACCOUNTS);
        transactionTypeReverse = intent.getStringExtra(Constants.TRANSACTIONTYPEREVERSE);
        sign = intent.getIntExtra(Constants.SIGN, 1);
        this.setTitle(title);

        dt = findViewById(R.id.date);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection(Constants.USERS).document(user.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        adapter = new TransactionsRecyclerViewAdapter(transactionList);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(CreditSales.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        dt = findViewById(R.id.date);

        accountSpinner = findViewById(R.id.spinner);

        setDate();
        loadAccountData();
        loadItemData();
        setListeners();
    }

    private void setDate() {
        dt.setText(UHelper.setPresentDateddMMyyyy());
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
    }

    private void loadItemData() {
        showProgressBar(true);
        Query query = firebaseFirestore.collection(Constants.USERS).document(user.getUid()).collection(Constants.ITEMS).whereEqualTo("usedFor." + transactionType, true);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                    Items item = q.toObject(Items.class);

                    Transaction transaction = new Transaction();
                    transaction.setItemName(item.getName());
                    transaction.setItemId(item.getId());
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

    }

    private void loadAccountData() {
        showProgressBar(true);
        accountsArrayAdapter = new ArrayAdapter<Accounts>(this, R.layout.spinner_item, accountsList);
        accountsArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        accountSpinner.setAdapter(accountsArrayAdapter);

        documentReference.collection(Constants.ACCOUNTS).whereEqualTo(account, true).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot q : task.getResult()) {
                                Accounts accounts = q.toObject(Accounts.class);
                                System.out.println("***" + accounts.getName());
                                accountsList.add(accounts);
                                updateSpinnerList();
                            }
                        } else {
                            toast("Error getting documents: " + task.getException());
                        }
                        showProgressBar(false);
                    }
                });

    }

    private void setListeners() {
        itemName = findViewById(R.id.itemName);
        etQuantity = findViewById(R.id.quantity);
        etPrice = findViewById(R.id.price);
        etAmount = findViewById(R.id.amount);
        etNote = findViewById(R.id.note);

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
        if (itemName.getText().toString().length() != 0 && etAmount.getText().toString().length() != 0) {
            itemName.setText(null);
            etQuantity.setText(null);
            etPrice.setText(null);
            etAmount.setText(null);
            etNote.setTag(null);
            etNote.setColorFilter(Color.BLACK);
        }


        quantity = 0.0;
        price = 0.0;
    }

    public void saveButton(View view) {
        batch = firebaseFirestore.batch();
        // initiateAccountingEntries();
        saveFreeItems(sign);
        saveListItems(sign);
        batchWrite();
    }

    public void savePaymentsButton(View view) {
        batch = firebaseFirestore.batch();
        // initiateAccountingEntries();
        String dia = null;
        if (account.contains(Constants.customer))
            dia = "Are you Sure to Receive Customer payments from ";
        else if (account.contains(Constants.vendor))
            dia = "Are you Sure to Pay Vendor ";
        else
            dia = "Are you Sure to Pay Lender ";
        new AlertDialog.Builder(view.getContext())
                .setTitle("Receive Credit")
                .setMessage(dia + accountSpinner.getSelectedItem())

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveFreeItems(sign * -1);
                        saveListItems(sign * -1);
                        batchWrite();
                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void saveFreeItems(int sig) {
        if (UHelper.parseDouble(etAmount.getText().toString()) > 0 && itemName.getText().toString().length() > 0 && accountSpinner.getSelectedItem() != null) {
            Transaction t = new Transaction();
            DocumentReference freeItemDocument = documentReference.collection(Constants.TRANSACTIONS).document();
            String datetime = dt.getText().toString() + " " + UHelper.getTime("time");

            t.setItemName(itemName.getText().toString());
            if (sig != sign)
                t.setTransactionType(transactionTypeReverse);
            else
                t.setTransactionType(transactionType);
            t.setAccountName(accountSpinner.getSelectedItem().toString());
            t.setTimeInMilli(UHelper.ddmmyyyyhmsTomili(datetime));
            t.setId(freeItemDocument.getId());
            if (etNote.getTag() != null)
                t.setNotes(etNote.getTag().toString());
            t.setQuantity(UHelper.parseDouble(etQuantity.getText().toString()));
            t.setPrice(UHelper.parseDouble(etPrice.getText().toString()));
            t.setAmount(UHelper.parseDouble(etAmount.getText().toString()) * sig);
            t.setTimestamp(System.currentTimeMillis());
            t.setAccountId(accountsList.get(accountSpinner.getSelectedItemPosition()).getId());

            //Update Postings for Days Credit Sales
            final Map<String, Object> data = new HashMap<>();
            if (sig != sign)
                data.put(transactionTypeReverse, FieldValue.increment(t.getAmount()));
            else
                data.put(transactionType, FieldValue.increment(t.getAmount()));

            DocumentReference doc = documentReference.collection(Constants.ACCOUNTS).document(accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
            DocumentReference accountEntry = documentReference.collection(Constants.POSTINGS).document(dt.getText().toString());

            batch.set(freeItemDocument, t);
            batch.set(accountEntry, data, SetOptions.merge());
            batch.set(doc, data, SetOptions.merge());
        }
    }

    public void saveListItems(int sig) {
        if (adapter.total <= 0 || accountSpinner.getSelectedItem() == null)
            return;

        for (final Transaction t : adapter.transactionList) {
            if (t.getAmount() > 0) {

                DocumentReference newDocument = documentReference.collection(Constants.TRANSACTIONS).document();
                t.setAmount(t.getAmount() * sig);
                if (sig != sign)
                    t.setTransactionType(transactionTypeReverse);
                else
                    t.setTransactionType(transactionType);
                t.setAccountName(accountSpinner.getSelectedItem().toString());
                String datetime = dt.getText().toString() + " " + UHelper.getTime("time");
                t.setTimeInMilli(UHelper.ddmmyyyyhmsTomili(datetime));
                t.setTimestamp(System.currentTimeMillis());
                t.setId(newDocument.getId());
                t.setAccountId(accountsList.get(accountSpinner.getSelectedItemPosition()).getId());


                //Update Postings for Days Credit Sales
                Map<String, Object> data = new HashMap<>();
                if (sig != sign)
                    data.put(transactionTypeReverse, FieldValue.increment(t.getAmount()));
                else
                    data.put(transactionType, FieldValue.increment(t.getAmount()));

                DocumentReference doc = documentReference.collection(Constants.ACCOUNTS).document(accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
                DocumentReference accountEntry = documentReference.collection(Constants.POSTINGS).document(dt.getText().toString());

                batch.set(newDocument, t);
                batch.set(accountEntry, data, SetOptions.merge());
                batch.set(doc, data, SetOptions.merge());
            }
        }
    }

/*    private void initiateAccountingEntries() {
        Map<String, Object> fsDate = new HashMap<>();
        fsDate.put(Constants.TIMESTAMP, FieldValue.serverTimestamp());
        accountEntry.set(fsDate, SetOptions.merge());
    }*/

    public void datePicker(final View view) {
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

    private void updateSpinnerList() {

        runOnUiThread(new Runnable() {
            public void run() {
                accountsArrayAdapter.notifyDataSetChanged();
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

    public void summary(final View view) {
        if (accountSpinner.getSelectedItem() == null)
            return;
        ;
        DocumentReference docRef = documentReference.collection(Constants.ACCOUNTS).document(accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
        final HashMap<String, Object>[] x = new HashMap[]{new HashMap<>()};
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        double in, out;
                        try {
                            in = document.get(transactionType, Double.class);
                            out = document.get(transactionTypeReverse, Double.class);
                        } catch (NullPointerException e) {
                            in = 0;
                            out = 0;
                        }

                        new AlertDialog.Builder(view.getContext())
                                .setTitle("Summary ")
                                .setMessage(transactionType + " Rs : " + document.get(transactionType) + "\n" +
                                        transactionTypeReverse + " Rs : " + document.get(transactionTypeReverse) + "\n" +
                                        "Balance Rs : " + (in + out))
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(R.drawable.ic_timeline_black_24dp)
                                .show();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void batchWrite() {
        if (!isAccountAvailable())
            return;

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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showProgressBar(false);
                toast("Failed to update, please enter again!");
            }
        });

    }

    public boolean isAccountAvailable() {
        return accountSpinner.getSelectedItem() != null;
    }

    public boolean isItemListAvailable() {
        return adapter.total > 0;
    }

    public boolean isFreeItemAvailable() {
        return UHelper.parseDouble(etAmount.getText().toString()) > 0 && itemName.getText().toString().length() > 0;
    }

}
