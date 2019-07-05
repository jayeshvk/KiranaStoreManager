package com.appdev.jayesh.kiranastoremanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.RecyclerTouchListener;
import com.appdev.jayesh.kiranastoremanager.Adapters.SalesOrderReportRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
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
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

public class SalesOrdersReport extends AppCompatActivity {

    private static final String TAG = "DateReport";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    List<SalesOrder> salesOrderList = new ArrayList<>();

    private ProgressDialog pDialog;
    SalesOrderReportRecyclerViewAdapter recyclerViewAdapter;

    RecyclerView recyclerView;

    DocumentReference documentReference;

    EditText fromdate, todate;

    Spinner accountSpinner, statusSpinner;
    List<Accounts> accountsList = new ArrayList<>();
    ArrayAdapter<Accounts> accountsAdapter;
    List<String> statusList = new ArrayList<>();
    ArrayAdapter<String> statusAdapter;

    Double in, out, balance;

    TextView tvin;
    TextView tvout;
    TextView tvbalance;

    Accounts selectedAccount = new Accounts();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order_report);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection(Constants.USERS).document(user.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        recyclerViewAdapter = new SalesOrderReportRecyclerViewAdapter(salesOrderList);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(SalesOrdersReport.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(recyclerViewAdapter);

        fromdate = findViewById(R.id.fromdate);
        todate = findViewById(R.id.todate);
        tvin = findViewById(R.id.in);
        tvout = findViewById(R.id.out);
        tvbalance = findViewById(R.id.balance);

        setDate();
        initiateAccountData();
        initiateLoadTransactionTypeData();

        setRecyclerTouchListner();
    }

    private void setRecyclerTouchListner() {
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //on touch of the item, set data on the scree
                final SalesOrder salesOrder = salesOrderList.get(position);

                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View popupView = inflater.inflate(R.layout.popup_window_salesorder_modify, null);

                TextView tvItemName = popupView.findViewById(R.id.itemName);
                final EditText tvquantity = popupView.findViewById(R.id.quantity);
                final EditText tvprice = popupView.findViewById(R.id.price);
                final EditText tvamount = popupView.findViewById(R.id.amount);
                final EditText tvnote = popupView.findViewById(R.id.note);
                final EditText tvUom = popupView.findViewById(R.id.uom);
                final TextView rdd = popupView.findViewById(R.id.deliveryDate);

                final RadioButton open = popupView.findViewById(R.id.open);
                final RadioButton delivered = popupView.findViewById(R.id.delivered);
                final RadioButton pending = popupView.findViewById(R.id.pending);
                final RadioButton closed = popupView.findViewById(R.id.closed);
                final RadioButton cancelled = popupView.findViewById(R.id.cancelled);

                tvItemName.setText(salesOrder.getItemName());
                tvquantity.setText(salesOrder.getQuantity() + "");
                tvprice.setText(salesOrder.getPrice() + "");
                tvamount.setText(salesOrder.getAmount() + "");
                tvnote.setText(salesOrder.getNotes());
                tvUom.setText(salesOrder.getUom());
                rdd.setText(UHelper.militoddmmyyyy(salesOrder.getRdd()));

                if (salesOrder.getStatus().equals(open.getText().toString()))
                    open.setChecked(true);
                if (salesOrder.getStatus().equals(closed.getText().toString()))
                    closed.setChecked(true);
                if (salesOrder.getStatus().equals(pending.getText().toString()))
                    pending.setChecked(true);
                if (salesOrder.getStatus().equals(delivered.getText().toString()))
                    delivered.setChecked(true);
                if (salesOrder.getStatus().equals(cancelled.getText().toString()))
                    cancelled.setChecked(true);


                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setElevation(20);
                popupWindow.setOutsideTouchable(false);
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                tvquantity.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Double qty = UHelper.parseDouble(tvquantity.getText().toString());
                        Double prc = UHelper.parseDouble(tvprice.getText().toString());
                        tvamount.setText(String.format("%.2f", (qty * prc)));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                tvprice.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Double qty = UHelper.parseDouble(tvquantity.getText().toString());
                        Double prc = UHelper.parseDouble(tvprice.getText().toString());
                        tvamount.setText(String.format("%.2f", (qty * prc)));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                final Button delete = popupView.findViewById(R.id.delete);
                Button update = popupView.findViewById(R.id.update);
                final Button cancel = popupView.findViewById(R.id.cancel);
                Button edit = popupView.findViewById(R.id.edit);

                final RadioGroup statusGroup = popupView.findViewById(R.id.statusGroup);


                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showProgressBar(true, "Deleting Document");
                        documentReference.collection(Constants.SALESORDERS).document(salesOrder.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        showProgressBar(false);
                                        toast("Successfully deleted!");
                                        popupWindow.dismiss();
                                        loadTransactions();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        showProgressBar(false);
                                        toast("Error deleting document" + e);
                                    }
                                });
                    }
                });
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        salesOrder.setQuantity(UHelper.parseDouble(tvquantity.getText().toString()));
                        salesOrder.setPrice(UHelper.parseDouble(tvprice.getText().toString()));
                        salesOrder.setAmount(UHelper.parseDouble(tvamount.getText().toString()));
                        salesOrder.setNotes(tvnote.getText().toString());
                        salesOrder.setUom(tvUom.getText().toString());
                        if (open.isChecked())
                            salesOrder.setStatus(open.getText().toString());
                        if (closed.isChecked())
                            salesOrder.setStatus(closed.getText().toString());
                        if (pending.isChecked())
                            salesOrder.setStatus(pending.getText().toString());
                        if (delivered.isChecked()) {
                            salesOrder.setStatus(delivered.getText().toString());
                            salesOrder.setAdd(UHelper.getTimeInMili());
                        }
                        if (cancelled.isChecked())
                            salesOrder.setStatus(cancelled.getText().toString());

                        showProgressBar(true, "Updating Document");
                        documentReference.collection(Constants.SALESORDERS).document(salesOrder.getId())
                                .set(salesOrder, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        showProgressBar(false);
                                        toast("Successfully Updated!");
                                        popupWindow.dismiss();
                                        loadTransactions();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        showProgressBar(false);
                                        toast("Error updating document" + e);
                                    }
                                });
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvquantity.setEnabled(true);
                        tvprice.setEnabled(true);
                        tvamount.setEnabled(true);
                        tvnote.setEnabled(true);
                        statusGroup.setBackgroundColor(Color.parseColor("#ffffff"));
                        for (int i = 0; i < statusGroup.getChildCount(); i++) {
                            statusGroup.getChildAt(i).setEnabled(true);
                        }
                    }
                });

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void updateFooter() {

        if (statusSpinner.getSelectedItem().toString().equals(Constants.CREDITSALES)) {
            tvin.setText("Credit SalesΣ\n" + out);
            tvout.setText("Receipt Σ\n" + in);
        } else if (statusSpinner.getSelectedItem().toString().equals(Constants.CREDITPURCHASE)) {
            tvin.setText("Credit PurchaseΣ\n" + in);
            tvout.setText("Payment Σ\n" + out);
        } else if (statusSpinner.getSelectedItem().toString().equals(Constants.LOAN)) {
            tvin.setText("Loan Σ\n" + in);
            tvout.setText("Payment Σ\n" + out);
        } else if (statusSpinner.getSelectedItem().toString().equals(Constants.BANKING)) {
            tvin.setText("Deposit Σ\n" + in);
            tvout.setText("Withdrawl Σ\n" + out);
        } else {
            tvin.setText("+\n" + in);
            tvout.setText("-\n" + out);
        }

        tvbalance.setText("Balance Σ\n" + balance);

    }

    private void initiateAccountData() {
        accountSpinner = findViewById(R.id.accountSpinner);
        accountsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, accountsList);
        accountsAdapter.setDropDownViewResource(R.layout.spinner_item);
        accountSpinner.setAdapter(accountsAdapter);
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshRecyclerView();
                loadTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        showProgressBar(true, "Loading Account Names");
        Accounts all = new Accounts();
        all.setName(Constants.ALL);
        accountsList.add(all);
        documentReference.collection(Constants.ACCOUNTS).whereEqualTo(Constants.customer, true).get()
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

    private void initiateLoadTransactionTypeData() {
        statusList.add(Constants.STATUS_OPEN);
        statusList.add(Constants.STATUS_PENDING);
        statusList.add(Constants.STATUS_DELIVERED);
        statusList.add(Constants.STATUS_CLOSED);
        statusList.add(Constants.STATUS_CANCELLED);


        statusSpinner = findViewById(R.id.statusSpinner);
        statusAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, statusList);
        statusAdapter.setDropDownViewResource(R.layout.spinner_item);
        statusSpinner.setAdapter(statusAdapter);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = findViewById(R.id.tvDate);
                if (parent.getItemAtPosition(position).toString().equals(Constants.STATUS_DELIVERED)
                        || parent.getItemAtPosition(position).toString().equals(Constants.STATUS_CLOSED)) {
                    tv.setText("Delivered");
                } else {
                    tv.setText("Created");
                }
                refreshRecyclerView();
                if (statusSpinner.getSelectedItem() != null)
                    loadTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void loadTransaction(View view) {
        loadTransactions();
    }

    private void loadTransactions() {
        refreshRecyclerView();
        String status = statusSpinner.getSelectedItem().toString();
        String accountName = accountSpinner.getSelectedItem() != null ? accountSpinner.getSelectedItem().toString() : "";
        Query query = null;
        if (accountsList.size() <= 0 || accountName.length() <= 0)
            return;

        if (accountName.equals(Constants.ALL)) {
            query = documentReference.collection(Constants.SALESORDERS).whereGreaterThanOrEqualTo("created", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("created", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo("status", status);
        } else
            query = documentReference.collection(Constants.SALESORDERS).whereGreaterThanOrEqualTo("created", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                    .whereLessThanOrEqualTo("created", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                    .whereEqualTo("status", status)
                    .whereEqualTo("accountId", accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
        showProgressBar(true, "Loading Sales Orders");
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        SalesOrder salesOrder = q.toObject(SalesOrder.class);
                        if (salesOrder.getAmount() > 0)
                            in = in + salesOrder.getAmount();
                        if (salesOrder.getAmount() < 0)
                            out = out + salesOrder.getAmount();

                        balance = balance + salesOrder.getAmount();

                        salesOrderList.add(salesOrder);
                    }
                }
                showProgressBar(false);
                recyclerViewAdapter.notifyDataSetChanged();
                updateFooter();
            }
        });
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
        salesOrderList.clear();
        recyclerViewAdapter.notifyDataSetChanged();
        in = 0.0;
        out = 0.0;
        balance = 0.0;
        updateFooter();
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
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
