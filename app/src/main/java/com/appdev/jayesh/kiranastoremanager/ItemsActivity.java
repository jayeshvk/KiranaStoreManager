package com.appdev.jayesh.kiranastoremanager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.ItemRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Adapters.RecyclerTouchListener;
import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class ItemsActivity extends AppCompatActivity {

    private static final String TAG = "ItemsActivity";

    EditText itemName;
    EditText itemPrice;
    EditText itemCost;
    CheckBox cashSale;
    CheckBox creditSale;
    CheckBox cashPurchase;
    CheckBox creditPurchase;
    CheckBox expenses;


    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    FirebaseUser user;
    List<Items> itemsList = new ArrayList<>();
    String globalITemId = null;
    int globalItemPosition = -1;
    ItemRecyclerViewAdapter adapter = new ItemRecyclerViewAdapter(itemsList);
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        this.setTitle("Manage Items");

        itemName = findViewById(R.id.itemName);
        itemPrice = findViewById(R.id.itemPrice);
        itemCost = findViewById(R.id.itemCost);
        cashSale = findViewById(R.id.cashSale);
        creditSale = findViewById(R.id.creditSale);
        cashPurchase = findViewById(R.id.cashPurchase);
        creditPurchase = findViewById(R.id.creditPurchase);
        expenses = findViewById(R.id.otherPayments);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        loadItemsFromFireStore();


    }

    private void loadItemsFromFireStore() {

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ItemsActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        showProgressBar(true);
        firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ITEMS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            showProgressBar(false);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Items itemAdded = dc.getDocument().toObject(Items.class);
                                    //itemAdded.setId(dc.getDocument().getId());
                                    itemsList.add(itemAdded);
                                    adapter.notifyDataSetChanged();
                                    resetView();
                                    break;
                                case MODIFIED:
                                    Items itemModified = dc.getDocument().toObject(Items.class);
                                    //itemModified.setId(dc.getDocument().getId());
                                    itemsList.set(globalItemPosition, itemModified);
                                    resetView();
                                    break;
                                case REMOVED:
                                    try {
                                        toast("Item " + itemsList.get(globalItemPosition).getName() + " deleted successfully");
                                    } catch (ArrayIndexOutOfBoundsException error) {
                                        Log.d(TAG, error.toString());
                                    }
                                    if (globalItemPosition != -1)
                                        itemsList.remove(globalItemPosition);
                                    resetView();
                                    break;
                            }
                        }
                        showProgressBar(false);
                    }
                });

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Items item = itemsList.get(position);

                itemName.setText(item.getName());
                itemPrice.setText(item.getPrice() + "");
                itemCost.setText(item.getCost() + "");
                cashSale.setChecked(item.getUsedFor().get(Constants.CASHSALES));
                creditSale.setChecked(item.getUsedFor().get(Constants.CREDITSALES));
                cashPurchase.setChecked(item.getUsedFor().get(Constants.CASHPURCHASE));
                creditPurchase.setChecked(item.getUsedFor().get(Constants.CREDITPURCHASE));
                expenses.setChecked(item.getUsedFor().get(Constants.EXPENSES));

                globalITemId = item.getId();
                globalItemPosition = position;
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    public void save(View view) {
        String name = itemName.getText().toString();
        String price = itemPrice.getText().toString();
        String cost = itemCost.getText().toString();

        final Items item = new Items();
        HashMap<String, Boolean> usedFor = new HashMap<>();
        item.setName(name);
        item.setPrice(UHelper.parseDouble(price));
        item.setCost(UHelper.parseDouble(cost));
        item.setId(globalITemId);
        usedFor.put(Constants.CASHSALES, cashSale.isChecked());
        usedFor.put(Constants.CREDITSALES, creditSale.isChecked());
        usedFor.put(Constants.CASHPURCHASE, cashPurchase.isChecked());
        usedFor.put(Constants.CREDITPURCHASE, creditPurchase.isChecked());
        usedFor.put(Constants.EXPENSES, expenses.isChecked());
        item.setUsedFor(usedFor);

        if (name.length() == 0)
            return;

        //update item if item has been selected before
        if (globalItemPosition != -1 && globalITemId != null) {
            firebaseFirestore.collection(Constants.USERS).document(mAuth.getUid()).collection(Constants.ITEMS).document(globalITemId)
                    .set(item)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            toast("Item " + item.getName() + " updated successfully");
                            resetView();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error writing document", e);
                        }
                    });
            return;
        }


        // Add a new document with a generated ID
        DocumentReference documentItem = firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ITEMS).document();
        item.setId(documentItem.getId());
        documentItem.set(item, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                toast("Item " + item.getName() + " added successfully");
                resetView();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error adding document try again " + e);

            }
        });
    }

    public void delete(View view) {
        if (globalItemPosition != -1 && globalITemId != null) {
            firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ITEMS).document(globalITemId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document", e);
                        }
                    });
        }
    }

    private void resetView() {
        globalItemPosition = -1;
        globalITemId = null;

        itemName.setText(null);
        itemPrice.setText(null);
        itemCost.setText(null);

        cashSale.setChecked(false);
        creditSale.setChecked(false);
        cashPurchase.setChecked(false);
        creditPurchase.setChecked(false);
        expenses.setChecked(false);

        adapter.notifyDataSetChanged();
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
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
            resetView();
        }
        return super.onOptionsItemSelected(item);
    }


}
