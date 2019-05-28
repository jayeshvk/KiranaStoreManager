package com.appdev.jayesh.kiranastoremanager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

import com.appdev.jayesh.kiranastoremanager.Adapters.DaySummaryViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Model.DaySummary;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DaysSummaryReport extends AppCompatActivity {
    private static final String TAG = "DaysSummaryReport";

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    CollectionReference collectionReference;

    RecyclerView recyclerView;
    DaySummaryViewAdapter recyclerViewAdapter;
    LinearLayoutManager manager;

    List<DaySummary> daySummaryList = new ArrayList<>();
    int currentItems, totalItems, scrollOutItems;
    Boolean isScrolling = false;

    private ProgressDialog pDialog;

    DocumentSnapshot lastVisible;
    int limit = 5;
    int documentsize = 0;

    Boolean adding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_days_summary_report);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection(Constants.USERS).document(user.getUid()).collection(Constants.POSTINGS);
        documentReference = firebaseFirestore.collection(Constants.USERS).document(user.getUid());

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        recyclerViewAdapter = new DaySummaryViewAdapter(daySummaryList);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(recyclerViewAdapter);

        loadData();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                    //System.out.println("Current Item : " + manager.getChildCount());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems = manager.getChildCount();
                totalItems = manager.getItemCount();
                scrollOutItems = manager.findFirstVisibleItemPosition();


                if (isScrolling && (currentItems + scrollOutItems == totalItems)) {
                    isScrolling = false;
                    if (documentsize > 0)
                        loadMore();
                }
            }
        });

    }

    private void loadMore() {
        showProgressBar(true);
        Query next = documentReference.collection(Constants.POSTINGS)
                .startAfter(lastVisible)
                .limit(limit);
        next.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                System.out.println(lastVisible.getId());
                if (documentSnapshots != null) {
                    for (DocumentSnapshot q : documentSnapshots) {
                        DaySummary d = q.toObject(DaySummary.class);
                        d.setDate(q.getId());
                        daySummaryList.add(d);
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                    showProgressBar(false);
                    if (documentSnapshots.size() > 0)
                        lastVisible = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() - 1);
                    documentsize = documentSnapshots.size();
                }

            }
        });

    }

    private void loadData() {
        showProgressBar(true);
        Query first = documentReference.collection(Constants.POSTINGS).limit(limit);

        first.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots != null) {
                            for (DocumentSnapshot q : documentSnapshots) {
                                DaySummary d = q.toObject(DaySummary.class);
                                d.setDate(q.getId());
                                daySummaryList.add(d);
                            }
                            recyclerViewAdapter.notifyDataSetChanged();
                            System.out.println("Current Item : " + manager.getChildCount());
                            showProgressBar(false);
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            documentsize = documentSnapshots.size();
                            System.out.println("Current Item : " + manager.getChildCount() + "Last visible Item" + manager.findLastVisibleItemPosition());
                        }
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

}