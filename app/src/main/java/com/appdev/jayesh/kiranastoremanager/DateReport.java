package com.appdev.jayesh.kiranastoremanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.appdev.jayesh.kiranastoremanager.Adapters.DateReportRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
import com.appdev.jayesh.kiranastoremanager.Model.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class DateReport extends AppCompatActivity {

    private static final String TAG = "DateReport";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    List<Transaction> transactionList = new ArrayList<>();

    private ProgressDialog pDialog;
    DateReportRecyclerViewAdapter recyclerViewAdapter;

    RecyclerView recyclerView;

    DocumentReference documentReference;

    EditText fromdate, todate;

    Spinner accountSpinner, transactionTypeSpinner;
    List<Accounts> accountsList = new ArrayList<>();
    ArrayAdapter<Accounts> accountsAdapter;
    List<String> transactionTypeList = new ArrayList<>();
    ArrayAdapter<String> transactionTypeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_report);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection(Constants.USERS).document(user.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        recyclerViewAdapter = new DateReportRecyclerViewAdapter(transactionList);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(DateReport.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(recyclerViewAdapter);

        fromdate = findViewById(R.id.fromdate);
        todate = findViewById(R.id.todate);

        setDate();
        initiateAccountData();

        initiateLoadTransactionTypeData();
    }

    private void initiateAccountData() {
        accountSpinner = findViewById(R.id.accountSpinner);
        accountsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, accountsList);
        accountsAdapter.setDropDownViewResource(R.layout.spinner_item);
        accountSpinner.setAdapter(accountsAdapter);
    }

    private void initiateLoadTransactionTypeData() {
        transactionTypeList.add(Constants.CASHSALES);
        transactionTypeList.add(Constants.CREDITSALES);
        transactionTypeList.add(Constants.CASHPURCHASE);
        transactionTypeList.add(Constants.CREDITPURCHASE);
        transactionTypeList.add(Constants.EXPENSES);
        transactionTypeList.add(Constants.LOAN);
        transactionTypeList.add(Constants.BANKING);


        transactionTypeSpinner = findViewById(R.id.transactionTypeSpinner);
        transactionTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, transactionTypeList);
        transactionTypeAdapter.setDropDownViewResource(R.layout.spinner_item);
        transactionTypeSpinner.setAdapter(transactionTypeAdapter);

        transactionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setAccountTypeLoadAccountData(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setAccountTypeLoadAccountData(String tranType) {
        switch (tranType) {
            case Constants.CASHSALES:
                refreshAccountSpinnerView();
                Accounts cs = new Accounts();
                cs.setName(Constants.CASHSALES);
                cs.setId(Constants.CASHSALES);
                accountsList.add(cs);
                accountsAdapter.notifyDataSetChanged();
                break;
            case Constants.CASHPURCHASE:
                refreshAccountSpinnerView();
                Accounts cp = new Accounts();
                cp.setName(Constants.CASHPURCHASE);
                cp.setId(Constants.CASHPURCHASE);
                accountsList.add(cp);
                accountsAdapter.notifyDataSetChanged();
                break;
            case Constants.EXPENSES:
                refreshAccountSpinnerView();
                Accounts ep = new Accounts();
                ep.setName(Constants.EXPENSES);
                ep.setId(Constants.EXPENSES);
                accountsList.add(ep);
                accountsAdapter.notifyDataSetChanged();
                break;
            case Constants.BANKING:
                refreshAccountSpinnerView();
                Accounts bk = new Accounts();
                bk.setName(Constants.BANKING);
                bk.setId(Constants.BANKING);
                accountsList.add(bk);
                accountsAdapter.notifyDataSetChanged();
                break;

            case Constants.CREDITSALES:
                refreshAccountSpinnerView();
                loadAccountData(Constants.customer);
                break;
            case Constants.CREDITPURCHASE:
                refreshAccountSpinnerView();
                loadAccountData(Constants.vendor);
                break;
            case Constants.LOAN:
                refreshAccountSpinnerView();
                loadAccountData(Constants.lender);
                break;
        }

    }

    private void loadAccountData(String account) {
        showProgressBar(true);
        documentReference.collection(Constants.ACCOUNTS).whereEqualTo(account, true).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot q : task.getResult()) {
                                Accounts accounts = q.toObject(Accounts.class);
                                accountsList.add(accounts);
                                accountsAdapter.notifyDataSetChanged();
                            }
                        } else {
                            toast("Error getting documents: " + task.getException());
                        }
                        showProgressBar(false);
                    }
                });

    }

    public void loadTransaction(View view) {
        refreshRecyclerView();
        String tranType = transactionTypeSpinner.getSelectedItem().toString();
        Query query = null;
        Query query2 = null;


        if (tranType.contains(Constants.CREDITSALES)) {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.CREDITSALES)
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.CUSTOMERPAYMENTS)
                    .whereEqualTo("accountId", accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
        } else if (tranType.contains(Constants.CREDITPURCHASE)) {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.CREDITPURCHASE)
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.VENDORPAYMENTS)
                    .whereEqualTo("accountId", accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
        } else if (tranType.contains(Constants.LOAN)) {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.LOAN)
                    //.whereEqualTo(Constants.TRANSACTIONTYPE, Constants.LOANPAYMENT)
                    .whereEqualTo("accountId", accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
            query2 = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.LOANPAYMENT)
                    .whereEqualTo("accountId", accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
        } else if (tranType.contains(Constants.BANKING)) {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.DEPOSIT)
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.WITHDRAWL);
        } else if ((tranType.contains(Constants.CASHSALES))) {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.CASHSALES);
        } else if ((tranType.contains(Constants.CASHPURCHASE))) {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.CASHPURCHASE);
        } else if ((tranType.contains(Constants.EXPENSES))) {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.EXPENSES);
        } else {
            toast("Nothing selected");
        }
/*        query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                .whereEqualTo(Constants.TRANSACTIONTYPE, Constants.LOAN)
                .whereEqualTo("accountId", "5P07an5DGoVe8bjUuD8B");
        showProgressBar(true);*/

        Source source = Source.SERVER;

/*        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
                showProgressBar(false);
            }
        });*/

/*        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        Transaction transaction = q.toObject(Transaction.class);
                        transactionList.add(transaction);
                        System.out.println(transaction.getItemName());
                    }
                }
                showProgressBar(false);
            }
        });*/
        showProgressBar(true);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        Transaction transaction = q.toObject(Transaction.class);
                        transactionList.add(transaction);
                        System.out.println(transaction.getItemName());
                    }
                }
                showProgressBar(false);
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });
        if (query2 != null) {
            query2.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                            Transaction transaction = q.toObject(Transaction.class);
                            transactionList.add(transaction);
                            System.out.println(transaction.getItemName());
                        }
                    }
                    showProgressBar(false);
                    recyclerViewAdapter.notifyDataSetChanged();
                }
            });
        }

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

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    public void datePicker(View view) {
        final int id = view.getId();
        String dateSplit;
        int mYear = 0, mMonth = 0, mDay = 0;

        if (id == R.id.fromdate) {
            dateSplit = fromdate.getText().toString();
        } else {
            dateSplit = todate.getText().toString();
        }
//get date from edit text and set them in the Date picker dialig as default date
        String[] split = dateSplit.split("-", 3);
        mYear = UHelper.parseInt(split[2]);
        mMonth = UHelper.parseInt(split[1]) - 1;
        mDay = UHelper.parseInt(split[0]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String month = UHelper.intLeadingZero(2, monthOfYear + 1);
                        String date = UHelper.intLeadingZero(2, dayOfMonth);

                        switch (id) {
                            case R.id.fromdate:
                                fromdate.setText(date + "-" + month + "-" + year);
                                break;
                            case R.id.todate:
                                todate.setText(date + "-" + month + "-" + year);
                                break;
                        }

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();

    }

    private void setDate() {
        fromdate.setText(UHelper.setPresentDateddMMyyyy());
        todate.setText(UHelper.setPresentDateddMMyyyy());
    }


    private void refreshRecyclerView() {
        transactionList.clear();
        recyclerViewAdapter.notifyDataSetChanged();
    }

    private void refreshAccountSpinnerView() {
        accountsList.clear();
        accountsAdapter.notifyDataSetChanged();
    }


    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }


}
