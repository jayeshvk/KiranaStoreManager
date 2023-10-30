package com.appdev.jayesh.kiranastoremanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.DaySummaryViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Model.DaySummary;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SalesSummaryReport extends AppCompatActivity {

    private static final String TAG = "SalesSummary";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    List<DaySummary> daySummaries = new ArrayList<>();

    private ProgressDialog pDialog;
    DaySummaryViewAdapter recyclerViewAdapter;

    RecyclerView recyclerView;

    DocumentReference documentReference;

    EditText fromdate, todate;

    Spinner summaryTypeSpinner;
    ArrayAdapter<String> summaryTypeAdapter;
    List<String> summaryTypeList = new ArrayList<>();

    Double in, out, balance;

    TextView tvin;
    TextView tvout;
    TextView tvbalance;

    WriteBatch batch;
    public static final int YEARLY = 2;
    public static final int MONTHLY = 1;
    public static final int DAILY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_summary_report);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection(Constants.USERS).document(user.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        recyclerViewAdapter = new DaySummaryViewAdapter(daySummaries);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(SalesSummaryReport.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(recyclerViewAdapter);

        fromdate = findViewById(R.id.fromdate);
        todate = findViewById(R.id.todate);

        tvin = findViewById(R.id.in);
        tvout = findViewById(R.id.out);
        tvbalance = findViewById(R.id.balance);
        summaryTypeList.add("Daily");
        summaryTypeList.add("Monthly");
        summaryTypeList.add("Yearly");
        summaryTypeSpinner = findViewById(R.id.summaryType);
        summaryTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, summaryTypeList);
        summaryTypeAdapter.setDropDownViewResource(R.layout.spinner_item);
        summaryTypeSpinner.setAdapter(summaryTypeAdapter);

        setDate();


    }

    private void updateFooter() {
        tvin.setText("Asset Σ\n" + String.format("%.2f", in));
        tvout.setText("Liability Σ\n" + String.format("%.2f", out));
        tvbalance.setText("Balance Σ\n" + String.format("%.2f", balance));

    }

    public void load(View view) {
        refreshView();
        loadTransactions();
    }

    private void loadTransactions() {
        showProgressBar(true);
        long fromMilli = 0, toMilli = 0;
        if (summaryTypeSpinner.getSelectedItemPosition() == DAILY) {
            fromMilli = UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00");
            toMilli = UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59");
        } else if (summaryTypeSpinner.getSelectedItemPosition() == MONTHLY) {
            String month = fromdate.getText().toString().split("-")[1];
            String year = fromdate.getText().toString().split("-")[2];
            String tomonth = todate.getText().toString().split("-")[1];
            String toyear = todate.getText().toString().split("-")[2];


            Calendar calendar = Calendar.getInstance();
            calendar.set(UHelper.parseInt(toyear), UHelper.parseInt(tomonth) + 1, 1);
            String lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + "";

            fromMilli = UHelper.ddmmyyyyhmsTomili("01-" + month + "-" + year + " 00:00:00");
            toMilli = UHelper.ddmmyyyyhmsTomili(lastDay + "-" + tomonth + "-" + toyear + " 23:59:59");
        } else {

            String year = fromdate.getText().toString().split("-")[2];
            String toyear = todate.getText().toString().split("-")[2];
            Calendar calendar = Calendar.getInstance();
            calendar.set(UHelper.parseInt(toyear), 13, 1);

            String lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + "";

            fromMilli = UHelper.ddmmyyyyhmsTomili("01-01-" + year + " 00:00:00");
            toMilli = UHelper.ddmmyyyyhmsTomili(lastDay + "-12-" + toyear + " 23:59:59");
        }
        Query query = documentReference.collection(Constants.POSTINGS).whereGreaterThanOrEqualTo("timeInMilli", fromMilli)
                .whereLessThanOrEqualTo("timeInMilli", toMilli);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        DaySummary daySummary = q.toObject(DaySummary.class);
                        daySummary.setDate(q.getId());
                        daySummaries.add(daySummary);
                    }
                    summarize();
                }
                recyclerViewAdapter.notifyDataSetChanged();
                updateFooter();
                showProgressBar(false);
            }
        });
    }

    private void summarize() {
        Map<String, DaySummary> result = new HashMap<>();
        double crs, creSum;
        for (DaySummary d : daySummaries) {
            String key = "";
            String date = d.getDate();
            if (date.equals(Constants.BankBalance))
                continue;
            int pos = summaryTypeSpinner.getSelectedItemPosition();

            if (pos == DAILY)
                key = date;
            else if (pos == MONTHLY)
                key = date.split("-")[1] + "-" + date.split("-")[2];
            else
                key = date.split("-")[2];

            Double bal = d.getCASHSALES() +
                    d.getLOAN() +
                    d.getCUSTOMERPAYMENTS() +
                    d.getCASHPURCHASES() +
                    d.getEXPENSES() +
                    d.getVENDORPAYMENTS() +
                    d.getLOANPAYMENT() + 0;
            d.setSUM(bal);

            in = in + d.getCASHSALES() + d.getLOAN() + Math.abs(d.getCUSTOMERPAYMENTS() + d.getCREDITSALES());
            out = out + d.getCASHPURCHASES() + d.getEXPENSES() + d.getLOANPAYMENT() + Math.abs(d.getCREDITPURCHASES() - +d.getVENDORPAYMENTS());
            balance = balance + bal;

            crs = d.getCASHSALES();
            creSum = result.get(key) != null ? result.get(key).getCASHSALES() : 0;
            d.setCASHSALES(creSum + crs);

            crs = d.getCREDITSALES();
            creSum = result.get(key) != null ? result.get(key).getCREDITSALES() : 0;
            d.setCREDITSALES(creSum + crs);

            crs = d.getCASHPURCHASES();
            creSum = result.get(key) != null ? result.get(key).getCASHPURCHASES() : 0;
            d.setCASHPURCHASES(creSum + crs);

            crs = d.getCREDITPURCHASES();
            creSum = result.get(key) != null ? result.get(key).getCREDITPURCHASES() : 0;
            d.setCREDITPURCHASES(creSum + crs);

            crs = d.getCUSTOMERPAYMENTS();
            creSum = result.get(key) != null ? result.get(key).getCUSTOMERPAYMENTS() : 0;
            d.setCUSTOMERPAYMENTS(creSum + crs);

            crs = d.getVENDORPAYMENTS();
            creSum = result.get(key) != null ? result.get(key).getVENDORPAYMENTS() : 0;
            d.setVENDORPAYMENTS(creSum + crs);

            crs = d.getLOAN();
            creSum = result.get(key) != null ? result.get(key).getLOAN() : 0;
            d.setLOAN(creSum + crs);

            crs = d.getLOANPAYMENT();
            creSum = result.get(key) != null ? result.get(key).getLOANPAYMENT() : 0;
            d.setLOANPAYMENT(creSum + crs);

            crs = d.getEXPENSES();
            creSum = result.get(key) != null ? result.get(key).getEXPENSES() : 0;
            d.setEXPENSES(creSum + crs);

            crs = d.getSUM();
            creSum = result.get(key) != null ? result.get(key).getSUM() : 0;
            d.setSUM(creSum + crs);


            if (pos == DAILY)
                d.setDate(date);
            else if (pos == MONTHLY)
                d.setDate(date.split("-")[1] + "-" + date.split("-")[2]);
            else
                d.setDate(date.split("-")[2]);

            result.put(key, d);
        }
        TreeMap<String, DaySummary> sorted = new TreeMap<>();
        sorted.putAll(result);
        daySummaries.clear();
        for (
                Map.Entry<String, DaySummary> entry : sorted.entrySet()) {
            daySummaries.add(entry.getValue());
        }
        recyclerViewAdapter.notifyDataSetChanged();

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

    private void refreshView() {
        daySummaries.clear();
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
