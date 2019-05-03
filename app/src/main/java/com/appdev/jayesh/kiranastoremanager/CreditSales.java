package com.appdev.jayesh.kiranastoremanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.TransactionsRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
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

public class CreditSales extends AppCompatActivity {

    private static final String TAG = "CreditSales";


    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    List<Transaction> transactionList = new ArrayList<>();

    private ProgressDialog pDialog;
    TransactionsRecyclerViewAdapter adapter = new TransactionsRecyclerViewAdapter(transactionList);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_sales);
        Intent intent = getIntent();
        title = intent.getStringExtra(Constants.TITLE);
        transactionType = intent.getStringExtra(Constants.TRANSACTIONTYPES);
        account = intent.getStringExtra(Constants.ACCOUNTS);
        transactionTypeReverse = intent.getStringExtra(Constants.TRANSACTIONTYPEREVERSE);

        this.setTitle(title);

        dt = findViewById(R.id.date);

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
        recyclerView.setLayoutManager(new LinearLayoutManager(CreditSales.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        dt = findViewById(R.id.date);

        accountSpinner = findViewById(R.id.spinner);

        setDate();
        loadAccountData();
        loadItemData();


    }

    private void setDate() {
        dt.setText(UHelper.setPresentDateddMMyyyy());
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
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
                    transaction.setItemId(item.getId());
                    transactionList.add(transaction);
                }
                showProgressBar(false);
                accountsArrayAdapter.notifyDataSetChanged();
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
        Query query = documentReference.collection(Constants.ACCOUNTS).whereEqualTo(account, true);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                    Accounts accounts = q.toObject(Accounts.class);
                    accountsList.add(accounts);
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

        accountsArrayAdapter = new ArrayAdapter<Accounts>(this, R.layout.spinner_item, accountsList);
        accountsArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        accountSpinner.setAdapter(accountsArrayAdapter);
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void save(View view) {
        if (adapter.total <= 0)
            return;
        switch (view.getId()) {

            case R.id.saveSales:
                save(-1);
                break;

            case R.id.saveCreditReceived:
                String dia = null;
                if (account.contains(Constants.customer))
                    dia = "Are you Sure to Receive Customer payments from ";
                else dia = "Are you Sure to Pay Vendor ";
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Receive Credit")
                        .setMessage(dia + accountSpinner.getSelectedItem())

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                save(+1);
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;

            default:
                break;
        }

    }

    public void save(int sign) {
        if (accountSpinner.getSelectedItem() == null)
            return;
        initiateAccountingEntries();
        WriteBatch batch = firebaseFirestore.batch();

        for (final Transaction t : adapter.transactionList) {
            if (t.getAmount() > 0) {
                showProgressBar(true, "Please wait, Saving Data");
                DocumentReference newDocument = documentReference.collection(Constants.TRANSACTIONS).document();
                t.setAmount(t.getAmount() * sign);
                if (sign > 0)
                    t.setTransactionType(transactionTypeReverse);
                else
                    t.setTransactionType(transactionType);


                t.setAccountName(accountSpinner.getSelectedItem().toString());
                String datetime = dt.getText().toString() + " " + UHelper.getTime("time");
                t.setTimeInMilli(UHelper.ddmmyyyyhmsTomili(datetime));
                t.setTimestamp(FieldValue.serverTimestamp());
                t.setId(newDocument.getId());

                //Update Postings for Days Credit Sales
                Map<String, Object> data = new HashMap<>();
                if (sign > 0)
                    data.put(transactionTypeReverse, FieldValue.increment(t.getAmount()));
                else
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

    private void initiateAccountingEntries() {
        accountEntry = documentReference.collection(Constants.POSTINGS).document(dt.getText().toString());
        Map<String, Object> fsDate = new HashMap<>();
        fsDate.put(Constants.TIMESTAMP, FieldValue.serverTimestamp());
        accountEntry.set(fsDate, SetOptions.merge());

    }

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
