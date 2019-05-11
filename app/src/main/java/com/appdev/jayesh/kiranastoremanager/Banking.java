package com.appdev.jayesh.kiranastoremanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Model.Transaction;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class Banking extends AppCompatActivity {

    private static final String TAG = Constants.EXPENSES;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;
    List<Transaction> transactionList = new ArrayList<>();
    DocumentReference documentReference;

    private ProgressDialog pDialog;

    EditText dt;
    int mYear;
    int mMonth;
    int mDay;

    EditText withdrawlName, withdrawlAmount, withdrawlComment;
    EditText depositName, depositAmount, depositComment;

    TextView balance;

    double deposit = 0.0, withdrawl = 0.0, balanceAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banking);
        Intent intent = getIntent();

        this.setTitle("Banking");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection(Constants.USERS).document(mAuth.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        dt = findViewById(R.id.date);
        withdrawlName = findViewById(R.id.withdrawlName);
        withdrawlAmount = findViewById(R.id.withdrawlAmount);
        withdrawlComment = findViewById(R.id.withdrawlComment);

        depositName = findViewById(R.id.depositName);
        depositAmount = findViewById(R.id.depositAmount);
        depositComment = findViewById(R.id.depositComment);

        balance = findViewById(R.id.balance);

        setDate();
        setListner();

    }

    private void setListner() {
        documentReference.collection(Constants.BANKING).document(Constants.DEPOSIT).
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            deposit = UHelper.parseDouble(snapshot.getData().get(Constants.DEPOSIT) + "");

                            Log.d(TAG, "Current data: " + snapshot.get(Constants.DEPOSIT));
                            //deposit = (Double) snapshot.get(Constants.WITHDRAWL);

                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });
        documentReference.collection(Constants.BANKING).document(Constants.WITHDRAWL).
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            withdrawl = UHelper.parseDouble(snapshot.getData().get(Constants.WITHDRAWL) + "");
                            Log.d(TAG, "Current data: " + snapshot.get(Constants.WITHDRAWL));
                            //withdrawl = (Double) snapshot.get(Constants.WITHDRAWL);
                            balance.setText(String.format("%.2f", (deposit - withdrawl)));

                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });
    }

    private void resetFreeTextView() {
        depositName.setText(null);
        depositAmount.setText(null);
        depositComment.setText(null);
        withdrawlName.setText(null);
        withdrawlAmount.setText(null);
        withdrawlComment.setText(null);

    }

    public void save(View view) {
        saveListItems();
    }

    private void saveListItems() {
        String dName = depositName.getText().toString();
        String dAmount = depositAmount.getText().toString();
        String dComment = depositComment.getText().toString();
        String wName = withdrawlName.getText().toString();
        String wAmount = withdrawlAmount.getText().toString();
        String wComment = withdrawlComment.getText().toString();
        String datetime = dt.getText().toString() + " " + UHelper.getTime("time");

        WriteBatch batch = firebaseFirestore.batch();

        //if deposit entered
        if (dName.length() > 0 && dAmount.length() > 0) {
            Transaction dTransaction = new Transaction();

            DocumentReference depositDocument = documentReference.collection(Constants.TRANSACTIONS).document();
            dTransaction.setItemName(dName);
            dTransaction.setTransactionType(Constants.DEPOSIT);
            dTransaction.setAccountName(Constants.DEPOSIT);
            dTransaction.setAccountId(Constants.DEPOSIT);
            dTransaction.setTimeInMilli(UHelper.ddmmyyyyhmsTomili(datetime));
            dTransaction.setId(depositDocument.getId());
            if (dComment.length() > 0)
                dTransaction.setNotes(dComment);
            dTransaction.setAmount(UHelper.parseDouble(dAmount));
            dTransaction.setTimestamp(System.currentTimeMillis());
            dTransaction.setTransaction(Constants.BANKING);
            dTransaction.setAccountId(Constants.BANKING);

            //Update Postings for Days Sales
            DocumentReference depoAccountEntry = documentReference.collection(Constants.BANKING).document(Constants.DEPOSIT);
            Map<String, Object> data = new HashMap<>();
            data.put(Constants.DEPOSIT, FieldValue.increment(dTransaction.getAmount()));
            data.put(Constants.TIMESTAMP, FieldValue.serverTimestamp());

            batch.set(depositDocument, dTransaction);
            batch.set(depoAccountEntry, data, SetOptions.merge());
        }

        //if withdrawls entered
        if (wName.length() > 0 && wAmount.length() > 0) {
            Transaction wTransaction = new Transaction();

            DocumentReference withdrawlDocument = documentReference.collection(Constants.TRANSACTIONS).document();
            wTransaction.setItemName(wName);
            wTransaction.setTransactionType(Constants.WITHDRAWL);
            wTransaction.setAccountName(Constants.WITHDRAWL);
            wTransaction.setAccountId(Constants.WITHDRAWL);
            wTransaction.setTimeInMilli(UHelper.ddmmyyyyhmsTomili(datetime));
            wTransaction.setId(withdrawlDocument.getId());
            if (wComment.length() > 0)
                wTransaction.setNotes(wComment);
            wTransaction.setAmount(UHelper.parseDouble(wAmount));
            wTransaction.setTimestamp(System.currentTimeMillis());
            wTransaction.setTransaction(Constants.BANKING);
            wTransaction.setAccountId(Constants.BANKING);

            //Update Postings for withdrawl
            DocumentReference wthdrawAccountEntry = documentReference.collection(Constants.BANKING).document(Constants.WITHDRAWL);
            Map<String, Object> data = new HashMap<>();
            data.put(Constants.WITHDRAWL, FieldValue.increment(wTransaction.getAmount()));
            data.put(Constants.TIMESTAMP, FieldValue.serverTimestamp());

            batch.set(withdrawlDocument, wTransaction);
            batch.set(wthdrawAccountEntry, data, SetOptions.merge());
        }

        if (dName.length() > 0 && dAmount.length() > 0 || wName.length() > 0 && wAmount.length() > 0) {
            showProgressBar(true);
            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    showProgressBar(false);
                    toast("Data Saved");
                    setDate();
                    resetFreeTextView();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showProgressBar(false);
                    toast("Failed to update, please enter again!");
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
