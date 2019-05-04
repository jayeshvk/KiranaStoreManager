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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.TransactionsRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.Model.Transaction;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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

public class Expenses extends AppCompatActivity {

    private static final String TAG = Constants.EXPENSES;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;
    List<Transaction> transactionList = new ArrayList<>();
    TransactionsRecyclerViewAdapter adapter = new TransactionsRecyclerViewAdapter(transactionList);
    RecyclerView recyclerView;
    DocumentReference accountEntry, documentReference;

    private ProgressDialog pDialog;

    EditText dt;
    int mYear;
    int mMonth;
    int mDay;

    EditText itemName, etQuantity, etPrice, etAmount;
    ImageView etNote;
    Double quantity, price;

    boolean isAmountModified;

    String title;
    String transactionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);
        Intent intent = getIntent();
        title = intent.getStringExtra(Constants.TITLE);
        transactionType = intent.getStringExtra(Constants.TRANSACTIONTYPES);

        this.setTitle(title);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection(Constants.USERS).document(mAuth.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        dt = findViewById(R.id.date);


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Expenses.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        setDate();
        loadItemData();
        setListeners();

    }

    private void loadItemData() {

        firebaseFirestore = FirebaseFirestore.getInstance();
        showProgressBar(true);
        Query query = firebaseFirestore.collection(Constants.USERS).document(user.getUid()).collection(Constants.ITEMS).whereEqualTo("usedFor." + transactionType, true);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                    Items item = q.toObject(Items.class);

                    Transaction transaction = new Transaction();
                    transaction.setItemName(item.getName());
                    transaction.setItemId(q.getId());
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
        itemName.setText(null);
        etQuantity.setText(null);
        etPrice.setText(null);
        etAmount.setText(null);
        etNote.setTag(null);
        etNote.setColorFilter(Color.BLACK);


        quantity = 0.0;
        price = 0.0;
    }

    private void initiateAccountingEntries() {
        accountEntry = documentReference.collection(Constants.POSTINGS).document(dt.getText().toString());
        Map<String, Object> fsDate = new HashMap<>();
        fsDate.put(Constants.TIMESTAMP, FieldValue.serverTimestamp());
        accountEntry.set(fsDate, SetOptions.merge());

    }

    public void save(View view) {
        initiateAccountingEntries();
        saveFreeItems();
        saveListItems();

    }

    private void saveListItems() {
        if (adapter.total <= 0)
            return;

        WriteBatch batch = firebaseFirestore.batch();

        for (final Transaction t : adapter.transactionList) {
            if (t.getAmount() > 0) {
                showProgressBar(true, "Please wait, Saving Data");
                DocumentReference newDocument = documentReference.collection(Constants.TRANSACTIONS).document();

                t.setTransactionType(transactionType);
                t.setAccountName(transactionType);
                String datetime = dt.getText().toString() + " " + UHelper.getTime("time");
                t.setTimeInMilli(UHelper.ddmmyyyyhmsTomili(datetime));
                t.setTimestamp(FieldValue.serverTimestamp());
                t.setId(newDocument.getId());
                //Update Postings for Days Sales
                Map<String, Object> data = new HashMap<>();
                data.put(transactionType, FieldValue.increment(t.getAmount()));

                batch.set(newDocument, t);
                batch.set(accountEntry, data, SetOptions.merge());
            }
        }
        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showProgressBar(false);
                toast("Data Saved");
                adapter.notifyDataSetChanged();
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

    private void saveFreeItems() {
        if (UHelper.parseDouble(etAmount.getText().toString()) > 0 && itemName.getText().toString().length() > 0) {
            Transaction t = new Transaction();
            showProgressBar(true, "Please wait, Saving Data");
            DocumentReference newDocument = documentReference.collection(Constants.TRANSACTIONS).document();
            String datetime = dt.getText().toString() + " " + UHelper.getTime("time");

            t.setItemName(itemName.getText().toString());
            t.setTransactionType(transactionType);
            t.setAccountName(transactionType);
            t.setTimeInMilli(UHelper.ddmmyyyyhmsTomili(datetime));
            t.setTimestamp(FieldValue.serverTimestamp());
            t.setId(newDocument.getId());
            if (etNote.getTag() != null)
                t.setNotes(etNote.getTag().toString());
            t.setQuantity(UHelper.parseDouble(etQuantity.getText().toString()));
            t.setPrice(UHelper.parseDouble(etPrice.getText().toString()));
            t.setAmount(UHelper.parseDouble(etAmount.getText().toString()));
            t.setTimestamp(FieldValue.serverTimestamp());

            //Update Postings for Days Sales
            final Map<String, Object> data = new HashMap<>();
            data.put(transactionType, FieldValue.increment(t.getAmount()));

            newDocument.set(t).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    showProgressBar(false);
                    toast(Constants.SUCCESS_MESSAGE);
                    accountEntry.set(data, SetOptions.merge());
                    resetFreeTextView();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showProgressBar(false);
                    toast(Constants.FAIL_MESSAGE);
                    Log.d(TAG, e.toString());
                }
            });
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

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
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

}
