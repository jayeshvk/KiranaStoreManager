package com.appdev.jayesh.kiranastoremanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.DateReportRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Adapters.RecyclerTouchListener;
import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
import com.appdev.jayesh.kiranastoremanager.Model.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.SetOptions;

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

    Double in, out, balance;

    TextView tvin;
    TextView tvout;
    TextView tvbalance;

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
                final Transaction transaction = transactionList.get(position);

                System.out.println("P : " + position + " sixe" + transactionList.size());

                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_window_transaction_modify, null);

                final TextView tvItemName = popupView.findViewById(R.id.itemName);
                final EditText tvquantity = popupView.findViewById(R.id.quantity);
                final EditText tvprice = popupView.findViewById(R.id.price);
                final EditText tvamount = popupView.findViewById(R.id.amount);
                final EditText tvnote = popupView.findViewById(R.id.note);
                final EditText uom = popupView.findViewById(R.id.uom);

                tvItemName.setText(transaction.getItemName());
                tvquantity.setText(transaction.getQuantity() + "");
                tvprice.setText(transaction.getPrice() + "");
                tvamount.setText(transaction.getAmount() + "");
                tvnote.setText(transaction.getNotes());
                uom.setText(transaction.getUom());


                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setElevation(20);
                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

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

                Button delete = popupView.findViewById(R.id.delete);
                Button update = popupView.findViewById(R.id.update);
                Button cancel = popupView.findViewById(R.id.cancel);
                Button edit = popupView.findViewById(R.id.edit);


                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showProgressBar(true, "Deleting Document");
                        documentReference.collection(Constants.TRANSACTIONS).document(transaction.getId())
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
                        transaction.setQuantity(UHelper.parseDouble(tvquantity.getText().toString()));
                        transaction.setPrice(UHelper.parseDouble(tvprice.getText().toString()));
                        transaction.setAmount(UHelper.parseDouble(tvamount.getText().toString()));
                        transaction.setNotes(tvnote.getText().toString());
                        transaction.setUom(uom.getText().toString());

                        showProgressBar(true, "Updating Document");
                        documentReference.collection(Constants.TRANSACTIONS).document(transaction.getId())
                                .set(transaction, SetOptions.merge())
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
                        uom.setEnabled(true);
                    }
                });

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void updateFooter() {

        if (transactionTypeSpinner.getSelectedItem().toString().contains(Constants.CREDITSALES)) {
            tvin.setText("Credit SalesΣ\n" + out);
            tvout.setText("Receipt Σ\n" + in);
        } else if (transactionTypeSpinner.getSelectedItem().toString().contains(Constants.CREDITPURCHASE)) {
            tvin.setText("Credit PurchaseΣ\n" + in);
            tvout.setText("Payment Σ\n" + out);
        } else if (transactionTypeSpinner.getSelectedItem().toString().contains(Constants.LOAN)) {
            tvin.setText("Loan Σ\n" + in);
            tvout.setText("Payment Σ\n" + out);
        } else if (transactionTypeSpinner.getSelectedItem().toString().contains(Constants.BANKING)) {
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                refreshRecyclerView();
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
        loadTransactions();
    }

    private void loadTransactions() {
        refreshRecyclerView();
        String tranType = transactionTypeSpinner.getSelectedItem().toString();

        Query query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"))
                .whereEqualTo(Constants.TRANSACTION, tranType)
                .whereEqualTo("accountId", accountsList.get(accountSpinner.getSelectedItemPosition()).getId());
        showProgressBar(true, "Loading Transaction List");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        Transaction transaction = q.toObject(Transaction.class);
                        if (transaction.getAmount() > 0)
                            in = in + transaction.getAmount();
                        if (transaction.getAmount() < 0)
                            out = out + transaction.getAmount();

                        balance = balance + transaction.getAmount();

                        transactionList.add(transaction);
                        System.out.println(transaction.getItemName());
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
        transactionList.clear();
        recyclerViewAdapter.notifyDataSetChanged();
        in = 0.0;
        out = 0.0;
        balance = 0.0;
        updateFooter();
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
