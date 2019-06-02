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
    CheckBox customer, vendor, lender;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    FirebaseUser user;
    List<Accounts> accountsList = new ArrayList<>();

    private ProgressDialog pDialog;

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
        lender = findViewById(R.id.lender);

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
                            Accounts account = dc.getDocument().toObject(Accounts.class);
                            switch (dc.getType()) {
                                case ADDED:
                                    accountsList.add(account);
                                    adapter.notifyDataSetChanged();
                                    resetView();
                                    break;
                                case MODIFIED:
                                    if (accountMobile.getTag() != null) {
                                        accountsList.set(UHelper.parseInt(accountMobile.getTag().toString()), account);
                                    } else {
                                        int p = -1;
                                        for (int i = 0; i < accountsList.size(); i++) {
                                            if (accountsList.get(i).getId().equals(account.getId()))
                                                p = i;
                                        }
                                        accountsList.set(p, account);
                                    }
                                    resetView();
                                    break;
                                case REMOVED:
                                    if (accountMobile.getTag() != null) {
                                        toast("Account " + accountsList.get(UHelper.parseInt(accountMobile.getTag().toString())).getName() + " deleted successfully");
                                        accountsList.remove(UHelper.parseInt(accountMobile.getTag().toString()));
                                    } else {
                                        int p = -1;
                                        for (int i = 0; i < accountsList.size(); i++) {
                                            if (accountsList.get(i).getId().equals(account.getId()))
                                                p = i;
                                        }
                                        accountsList.remove(p);
                                    }
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
                lender.setChecked(account.isLender());

                accountName.setTag(account.getId());
                accountMobile.setTag(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    public void save(View view) {
        String name = accountName.getText().toString();
        String mobile = accountMobile.getText().toString();

        final Accounts accounts = new Accounts();
        accounts.setName(name);
        if (mobile == null)
            mobile = "";
        accounts.setMobile(mobile);
        accounts.setCustomer(customer.isChecked());
        accounts.setVendor(vendor.isChecked());
        accounts.setLender(lender.isChecked());
        if (accountName.getTag() != null)
            accounts.setId(accountName.getTag().toString());

        if (name.length() == 0)
            return;

        //update account if item has been selected before
        if (accountName.getTag() != null) {
            firebaseFirestore.collection(Constants.USERS).document(mAuth.getUid()).collection(Constants.ACCOUNTS).document(accountName.getTag().toString())
                    .set(accounts,SetOptions.merge())
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
        if (accountName.getTag() != null) {
            firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ACCOUNTS).document(accountName.getTag().toString())
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
        accountName.setTag(null);
        accountMobile.setTag(null);

        accountName.setText(null);
        accountMobile.setText(null);
        customer.setChecked(false);
        vendor.setChecked(false);
        lender.setChecked(false);

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
