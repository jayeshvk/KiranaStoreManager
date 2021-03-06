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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.ItemSummaryRecyclerViewAdapter;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ItemSummaryReport extends AppCompatActivity {

    private static final String TAG = "DateReport";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    List<Transaction> transactionList = new ArrayList<>();

    private ProgressDialog pDialog;
    ItemSummaryRecyclerViewAdapter recyclerViewAdapter;

    RecyclerView recyclerView;

    DocumentReference documentReference;

    EditText fromdate, todate;

    Spinner accountSpinner, transactionTypeSpinner, itemSpinner;
    List<Accounts> accountsList = new ArrayList<>();
    ArrayAdapter<Accounts> accountsAdapter;
    List<String> transactionTypeList = new ArrayList<>();
    ArrayAdapter<String> transactionTypeAdapter;
    List<Items> itemsList = new ArrayList<>();
    ArrayAdapter<Items> itemAdapter;


    Double in, out, balance;

    TextView tvin;
    TextView tvout;
    TextView tvbalance;

    final List<Transaction> tmp = new ArrayList<>();

    String viewType = "daily";

    Boolean load = true;
    HashMap<String, String> transactionMapping = new HashMap<>();

    TextView tvItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_summary_report);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection(Constants.USERS).document(user.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        recyclerViewAdapter = new ItemSummaryRecyclerViewAdapter(transactionList);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ItemSummaryReport.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(recyclerViewAdapter);

        fromdate = findViewById(R.id.fromdate);
        todate = findViewById(R.id.todate);

        tvin = findViewById(R.id.in);
        tvout = findViewById(R.id.out);
        tvbalance = findViewById(R.id.balance);

        setDate();
        initiateAccountData();
        initiateItemData();
        initiateLoadTransactionTypeData();

        transactionMapping.put(Constants.CREDITSALES, Constants.CUSTOMERPAYMENTS);
        transactionMapping.put(Constants.CREDITPURCHASE, Constants.VENDORPAYMENTS);
        transactionMapping.put(Constants.LOAN, Constants.LOANPAYMENT);
        transactionMapping.put(Constants.DEPOSIT, Constants.WITHDRAWL);

        tvItem = findViewById(R.id.tvItem);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        if (viewType.equals("daily")) {
            menu.findItem(R.id.dailyView).setChecked(true);
            menu.findItem(R.id.monthlyView).setChecked(false);
        } else {
            menu.findItem(R.id.dailyView).setChecked(false);
            menu.findItem(R.id.monthlyView).setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        // Handle item selection
        switch (item.getItemId()) {
            case R.id.dailyView:
                viewType = "daily";
                item.setChecked(true);
                return true;
            case R.id.monthlyView:
                //startSettings();
                viewType = "monthly";
                item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //invalidateOptionsMenu();
        if (viewType.equals("daily")) {
            menu.findItem(R.id.dailyView).setChecked(true);
            menu.findItem(R.id.monthlyView).setChecked(false);
        } else {
            menu.findItem(R.id.dailyView).setChecked(false);
            menu.findItem(R.id.monthlyView).setChecked(true);
        }
        return super.onPrepareOptionsMenu(menu);
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

    private void initiateItemData() {
        itemSpinner = findViewById(R.id.itemSpinner);
        itemAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, itemsList);
        itemAdapter.setDropDownViewResource(R.layout.spinner_item);
        itemSpinner.setAdapter(itemAdapter);
        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadAccountData(String account) {
        Accounts all = new Accounts();
        all.setName(Constants.ALL);

        accountsList.add(all);
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
                loadItemData();
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

    public void loadTransaction(View view) {
        if (!load)
            return;
        load = false;
        refreshRecyclerView();
        tmp.clear();
        final String item = itemSpinner.getSelectedItem().toString();
        final String tranType = transactionTypeSpinner.getSelectedItem().toString();
        if (accountSpinner.getSelectedItem().toString().equals(Constants.ALL))
            tvItem.setText("Name");
        else
            tvItem.setText("Item");
        loadTransactions(tranType, item);
    }

    private void loadTransactions(String tranType, String item) {
        long fromMilli, toMilli;
        if (viewType.equals("daily")) {
            fromMilli = UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00");
            toMilli = UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59");
        } else {
            String month = fromdate.getText().toString().split("-")[1];
            String year = fromdate.getText().toString().split("-")[2];
            String tomonth = todate.getText().toString().split("-")[1];
            String toyear = todate.getText().toString().split("-")[2];

            Calendar calendar = Calendar.getInstance();
            calendar.set(UHelper.parseInt(toyear), UHelper.parseInt(tomonth) + 1, 1);
            String lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + "";

            fromMilli = UHelper.ddmmyyyyhmsTomili("01-" + month + "-" + year + " 00:00:00");
            toMilli = UHelper.ddmmyyyyhmsTomili(lastDay + "-" + tomonth + "-" + toyear + " 23:59:59");
        }
        //handle banking transaction type seperately
        if (tranType.equals(Constants.BANKING))
            tranType = Constants.DEPOSIT;

        if (accountSpinner.getSelectedItem().toString().equals(Constants.ALL)) {
//get data for Transaction type anf their reverse transactions
            if (item.equals(Constants.ALL)) {
                getDataFromFireStoreForAll(fromMilli, toMilli, tranType);

            } else {
                getDataFromFireStoreForOne(fromMilli, toMilli, tranType);
            }

        } else {
            //get data for Transaction type anf their reverse transactions
            if (item.equals(Constants.ALL)) {
                getDataFromFireStoreForAll(fromMilli, toMilli, tranType);
                if (transactionMapping.get(tranType) != null)
                    getDataFromFireStoreForAll(fromMilli, toMilli, transactionMapping.get(tranType));
            } else {
                getDataFromFireStoreForOne(fromMilli, toMilli, tranType);
                if (transactionMapping.get(tranType) != null)
                    getDataFromFireStoreForOne(fromMilli, toMilli, transactionMapping.get(tranType));
            }
        }
    }

    private void getDataFromFireStoreForAll(long fromMilli, long toMilli, String tranType) {
        String accountname = accountSpinner.getSelectedItem().toString();
        Query query = null;
        if (accountname.equals(Constants.ALL)) {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", fromMilli)
                    .whereLessThanOrEqualTo("timeInMilli", toMilli)
                    .whereEqualTo(Constants.TRANSACTION, tranType)
                    .orderBy("timeInMilli", Query.Direction.ASCENDING);
        } else {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", fromMilli)
                    .whereLessThanOrEqualTo("timeInMilli", toMilli)
                    .whereEqualTo(Constants.TRANSACTIONTYPE, tranType)
                    .whereEqualTo("accountId", accountsList.get(accountSpinner.getSelectedItemPosition()).getId())
                    .orderBy("timeInMilli", Query.Direction.ASCENDING);
        }
        showProgressBar(true, "Loading transactions for Item");
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        tmp.add(q.toObject(Transaction.class));
                    }
                    summarize();
                    tmp.clear();
                    showProgressBar(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Exception :" + e);
                showProgressBar(false);
            }
        });
        //}
    }

    private void getDataFromFireStoreForOne(long fromMilli, long toMilli, String tranType) {
        String accountname = accountSpinner.getSelectedItem().toString();
        Query query = null;

        if (accountname.equals(Constants.ALL)) {
            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", fromMilli)
                    .whereLessThanOrEqualTo("timeInMilli", toMilli)
                    .whereEqualTo(Constants.TRANSACTION, tranType)
                    .whereEqualTo("itemId", itemsList.get(itemSpinner.getSelectedItemPosition()).getId());
        } else {

            query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", fromMilli)
                    .whereLessThanOrEqualTo("timeInMilli", toMilli)
                    .whereEqualTo(Constants.TRANSACTIONTYPE, tranType)
                    .whereEqualTo("accountId", accountsList.get(accountSpinner.getSelectedItemPosition()).getId())
                    .whereEqualTo("itemId", itemsList.get(itemSpinner.getSelectedItemPosition()).getId());
        }
        showProgressBar(true, "Loading transactions for Item");
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        tmp.add(q.toObject(Transaction.class));
                    }
                    summarize();
                }
            }
        });
        showProgressBar(false);
    }

    private void summarize() {
        String itemname = "";
        if (tmp.size() > 0) {
            Map<String, String> result = new HashMap<>();


            for (Transaction transaction : tmp) {

                if (itemSpinner.getSelectedItem().toString().equals(Constants.FREETEXTITEM))
                    itemname = Constants.FREETEXTITEM;
                else if (transaction.getItemId() == null)
                    itemname = Constants.FREETEXTITEM;
                else itemname = transaction.getItemName();

                String key = "";

                String date = UHelper.militoddmmyyyy(transaction.getTimeInMilli());

                if (accountSpinner.getSelectedItem().toString().equals(Constants.ALL))
                    itemname = transaction.getAccountName();


                if (viewType.equals("daily")) {
                    key = date + "_" + itemname;
                } else
                    key = date.split("-")[1] + "-" + date.split("-")[2] + "_" + itemname;

                balance = balance + transaction.getAmount();
                if (transaction.getAmount() < 0)
                    out = out + transaction.getAmount();
                if (transaction.getAmount() > 0)
                    in = in + transaction.getAmount();

                double oldValue, oldQuantity;
                if (result.get(key) != null) {
                    oldValue = UHelper.parseDouble(result.get(key).split(Constants.Seperator)[0]);
                    oldQuantity = UHelper.parseDouble(result.get(key).split(Constants.Seperator)[1]);
                } else {
                    oldValue = 0;
                    oldQuantity = 0;
                }
                double value = transaction.getAmount();
                double quantity = transaction.getQuantity();
                double amt = value + oldValue;

                //double oldValue = result.get(key) != null ? result.get(key) : 0;
                result.put(key, amt + Constants.Seperator + (quantity + oldQuantity));

            }
            // Copy all data from hashMap into TreeMap for sorting
            TreeMap<String, String> sorted = new TreeMap<>();
            sorted.putAll(result);

            for (
                    Map.Entry<String, String> entry : sorted.entrySet()) {
                Transaction t = new Transaction();
                String date = entry.getKey().split("_")[0];
                String name = entry.getKey().split("_")[1];
                System.out.println("*** " + name);
                if (date.length() == 7)
                    t.setTimeInMilli(UHelper.ddmmyyyyTomili("01-" + entry.getKey().substring(0, 7)));
                else
                    t.setTimeInMilli(UHelper.ddmmyyyyTomili(entry.getKey().substring(0, 10)));

                t.setAmount(UHelper.parseDouble(entry.getValue().split(Constants.Seperator)[0]));
                t.setQuantity(UHelper.parseDouble(entry.getValue().split(Constants.Seperator)[1]));
                t.setItemName(entry.getKey().substring(entry.getKey().indexOf("_") + 1));
                transactionList.add(t);
            }
            recyclerViewAdapter.notifyDataSetChanged();
        }
        updateFooter();
        load = true;
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

    private void loadItemData() {
        itemsList.clear();
        Items tmp = new Items();
        tmp.setName("ALL");
        itemsList.add(tmp);
        Items freeTexItem = new Items();
        freeTexItem.setName(Constants.FREETEXTITEM);
        itemsList.add(freeTexItem);
        showProgressBar(true);
        Query query = firebaseFirestore.collection(Constants.USERS).document(user.getUid()).collection(Constants.ITEMS).whereEqualTo("usedFor." + transactionTypeSpinner.getSelectedItem().toString(), true);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                    Items item = q.toObject(Items.class);
                    itemsList.add(item);
                }
                showProgressBar(false);
                itemAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showProgressBar(false);
            }
        });

    }

    private void updateFooter() {
        tvin.setText("In Σ\n" + in);
        tvout.setText("Out Σ\n" + out);
        tvbalance.setText("Balance Σ\n" + (out + in));

    }

}
