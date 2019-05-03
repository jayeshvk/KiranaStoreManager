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

import com.appdev.jayesh.kiranastoremanager.Adapters.AccountsRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Adapters.RecyclerTouchListener;
import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

import javax.annotation.Nullable;

public class AccountsActivity extends AppCompatActivity {

    private static final String TAG = "ItemsActivity";
    EditText accountName;
    EditText accountMobile;
    CheckBox customer, vendor;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    FirebaseUser user;
    List<Accounts> accountsList = new ArrayList<>();

    private ProgressDialog pDialog;

    String globalITemId = null;
    int globalItemPosition = -1;

    AccountsRecyclerViewAdapter adapter = new AccountsRecyclerViewAdapter(accountsList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        this.setTitle("Manage Accounts");

        accountName = findViewById(R.id.accountName);
        accountMobile = findViewById(R.id.accountMobile);
        customer = findViewById(R.id.customer);
        vendor = findViewById(R.id.vendor);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        loadAccountsFromFireStore();


    }

    private void loadAccountsFromFireStore() {

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(AccountsActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        showProgressBar(true);
        firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ACCOUNTS)
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
                                    ObjectMapper mapper = new ObjectMapper();
                                    Accounts account = mapper.convertValue(dc.getDocument().getData(), Accounts.class);
                                    accountsList.add(account);
                                    adapter.notifyDataSetChanged();
                                    resetView();
                                    break;
                                case MODIFIED:
                                    mapper = new ObjectMapper();
                                    account = mapper.convertValue(dc.getDocument().getData(), Accounts.class);
                                    accountsList.set(globalItemPosition, account);
                                    resetView();
                                    break;
                                case REMOVED:
                                    try {
                                        toast("Item " + accountsList.get(globalItemPosition).getName() + " deleted successfully");
                                    } catch (ArrayIndexOutOfBoundsException error) {
                                        Log.d(TAG, error.toString());
                                    }
                                    accountsList.remove(globalItemPosition);
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
                //on touch of the item, set data on the scree
                Accounts account = accountsList.get(position);

                accountName.setText(account.getName());
                accountMobile.setText(account.getMobile() + "");
                customer.setChecked(account.isCustomer());
                vendor.setChecked(account.isVendor());

                globalITemId = account.getId();
                globalItemPosition = position;
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    public void save(View view) {
        String name = accountName.getText().toString();
        String mobile = accountMobile.getText().toString();
        boolean customerCB = customer.isChecked();
        boolean vendorCB = vendor.isChecked();

        final Accounts accounts = new Accounts();
        accounts.setName(name);
        if (mobile == null)
            mobile = "";
        accounts.setMobile(mobile);
        accounts.setCustomer(customerCB);
        accounts.setVendor(vendorCB);
        accounts.setId(globalITemId);

        if (name.length() == 0)
            return;

        //update account if item has been selected before
        if (globalItemPosition != -1 && globalITemId != null) {
            firebaseFirestore.collection(Constants.USERS).document(mAuth.getUid()).collection(Constants.ACCOUNTS).document(globalITemId)
                    .set(accounts)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            toast("Account " + accounts.getName() + " updated successfully");

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


        // Add a new account with a generated ID
        DocumentReference documentAccount = firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ACCOUNTS).document();
        accounts.setId(documentAccount.getId());
        documentAccount.set(accounts, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                toast("Account " + accounts.getName() + " added successfully");

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
            firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ACCOUNTS).document(globalITemId)
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

        accountName.setText(null);
        accountMobile.setText(null);
        customer.setChecked(false);
        vendor.setChecked(false);

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
