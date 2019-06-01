package com.appdev.jayesh.kiranastoremanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.SalesOrderRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.Model.SalesOrder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class SalesOrderActivity extends AppCompatActivity {

    private static final String TAG = "SalesOrder";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;

    List<SalesOrder> salesOrderList = new ArrayList<>();

    private ProgressDialog pDialog;


    EditText created, rdd;
    int mYear;
    int mMonth;
    int mDay;


    List<Accounts> accountsList = new ArrayList<>();
    ArrayAdapter<Accounts> accountsArrayAdapter;
    Spinner accountSpinner;

    WriteBatch batch;

    SalesOrderRecyclerViewAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order);
        this.setTitle("Sales Order Create");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection(Constants.USERS).document(user.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        created = findViewById(R.id.created);
        rdd = findViewById(R.id.rdd);

        setDate();
        loadAccountData();
        loadItemData();

    }

    private void loadAccountData() {
        accountSpinner = findViewById(R.id.spinner);
        accountsArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, accountsList);
        accountsArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        accountSpinner.setAdapter(accountsArrayAdapter);

        showProgressBar(true);
        documentReference.collection(Constants.ACCOUNTS).whereEqualTo(Constants.customer, true).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot q : task.getResult()) {
                                Accounts accounts = q.toObject(Accounts.class);
                                System.out.println("***" + accounts.getName());
                                accountsList.add(accounts);
                                accountsArrayAdapter.notifyDataSetChanged();
                            }
                        } else {
                            toast("Error getting documents: " + task.getException());
                        }
                        showProgressBar(false);
                    }
                });

    }

    private void loadItemData() {
        adapter = new SalesOrderRecyclerViewAdapter(salesOrderList);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(SalesOrderActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        showProgressBar(true);
        Query query = documentReference.collection(Constants.ITEMS).whereEqualTo("usedFor." + Constants.CREDITSALES, true).orderBy("name", Query.Direction.ASCENDING);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                    Items item = q.toObject(Items.class);
                    Log.d(TAG, item.getName());
                    SalesOrder salesOrder = new SalesOrder();
                    salesOrder.setItemName(item.getName());
                    salesOrder.setItemId(item.getId());
                    salesOrderList.add(salesOrder);
                }
                showProgressBar(false);
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showProgressBar(false);
                toast("Error fetching data");
            }
        });

    }

    public void saveListItems() {
        batch = firebaseFirestore.batch();
        if (!isAccountAvailable() || !isItemListAvailable()) {
            toast("Enter Data");
            return;
        }

        for (final SalesOrder t : adapter.salesOrderList) {
            if (t.getAmount() > 0) {
                String datetime = created.getText().toString() + " " + UHelper.getTime("time");
                String deliveryDate = rdd.getText().toString() + " " + UHelper.getTime("time");

                DocumentReference newDocument = documentReference.collection(Constants.SALESORDERS).document();
                t.setId(newDocument.getId());
                t.setAccountName(accountSpinner.getSelectedItem().toString());
                t.setAccountId(accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
                t.setCreated(UHelper.ddmmyyyyhmsTomili(datetime));
                t.setTimestamp(System.currentTimeMillis());
                t.setRdd(UHelper.ddmmyyyyhmsTomili(deliveryDate));
                t.setStatus(Constants.STATUS_OPEN);

                batch.set(newDocument, t);
            }
        }

        showProgressBar(true, "Please wait, Saving Data");
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

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public boolean isAccountAvailable() {
        return accountSpinner.getSelectedItem() != null;
    }

    public boolean isItemListAvailable() {
        return adapter.total > 0;
    }

    public void datePicker(View view) {
        final int id = view.getId();
        String dateSplit;
        int mYear = 0, mMonth = 0, mDay = 0;

        if (id == R.id.created) {
            dateSplit = created.getText().toString();
        } else {
            dateSplit = rdd.getText().toString();
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
                            case R.id.created:
                                created.setText(date + "-" + month + "-" + year);
                                break;
                            case R.id.rdd:
                                rdd.setText(date + "-" + month + "-" + year);
                                break;
                        }

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();

    }

    private void setDate() {
        created.setText(UHelper.setPresentDateddMMyyyy());
        rdd.setText(UHelper.setPresentDateddMMyyyy());
    }

    public void saveButton(View view) {
        saveListItems();
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

}
