package com.appdev.jayesh.kiranastoremanager;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Adapters.ItemRecyclerViewAdapter;
import com.appdev.jayesh.kiranastoremanager.Adapters.RecyclerTouchListener;
import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemsActivity extends AppCompatActivity {

    private static final String TAG = "ItemsActivity";

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mAuth;
    FirebaseUser user;
    List<Items> itemsList = new ArrayList<>();
    List<Items> itemsSpinnerList = new ArrayList<>();
    ItemRecyclerViewAdapter adapter;
    private ProgressDialog pDialog;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        this.setTitle("Manage Items");

        adapter = new ItemRecyclerViewAdapter(itemsList);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        showProgressBar(true);
        firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ITEMS).orderBy("name", Query.Direction.ASCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        Items item = q.toObject(Items.class);
                        System.out.println(item);
                        itemsList.add(item);
                    }
                    showProgressBar(false);
                    adapter.notifyDataSetChanged();
                }
            }
        });
           /*     .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            showProgressBar(false);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Items item = dc.getDocument().toObject(Items.class);

                            switch (dc.getType()) {
                                case ADDED:
                                    itemsList.add(item);
                                    adapter.notifyDataSetChanged();
                                    resetView();
                                    break;
                                case MODIFIED:
                                    if (itemPrice.getTag() != null) {
                                        itemsList.set(UHelper.parseInt(itemPrice.getTag().toString()), item);
                                    } else {
                                        int p = -1;
                                        for (int i = 0; i < itemsList.size(); i++) {
                                            if (itemsList.get(i).getId().equals(item.getId()))
                                                p = i;
                                        }
                                        itemsList.set(p, item);
                                    }
                                    resetView();
                                    break;
                                case REMOVED:
                                    if (itemPrice.getTag() != null) {
                                        toast("Item " + itemsList.get(UHelper.parseInt(itemPrice.getTag().toString())).getName() + " deleted successfully");
                                        itemsList.remove(UHelper.parseInt(itemPrice.getTag().toString()));
                                    } else {
                                        int p = -1;
                                        for (int i = 0; i < itemsList.size(); i++) {
                                            if (itemsList.get(i).getId().equals(item.getId()))
                                                p = i;
                                        }
                                        itemsList.remove(p);
                                    }
                                    resetView();
                                    break;
                            }
                        }
                        showProgressBar(false);
                    }
                });
*/
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                showPopUp(position);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    private void showPopUp(int position) {

        Items selectedItem = new Items();

        dialog = new Dialog(ItemsActivity.this);
        dialog.setContentView(R.layout.activity_items_edit);
        dialog.setCancelable(false);
        itemsSpinnerList.clear();
        // custom dialog

        Button savePopupBtn = dialog.findViewById(R.id.save);
        Button deletePopupBtn = dialog.findViewById(R.id.delete);
        Button closePopupBtn = dialog.findViewById(R.id.close);

        EditText itemName = dialog.findViewById(R.id.itemName);
        EditText itemPrice = dialog.findViewById(R.id.itemPrice);
        EditText itemCost = dialog.findViewById(R.id.itemCost);
        CheckBox cashSale = dialog.findViewById(R.id.cashSale);
        CheckBox creditSale = dialog.findViewById(R.id.creditSale);
        CheckBox cashPurchase = dialog.findViewById(R.id.cashPurchase);
        CheckBox creditPurchase = dialog.findViewById(R.id.creditPurchase);
        CheckBox expenses = dialog.findViewById(R.id.otherPayments);
        CheckBox financeItem = dialog.findViewById(R.id.financeItem);
        CheckBox isInventory = dialog.findViewById(R.id.isInventory);
        CheckBox isProcessed = dialog.findViewById(R.id.isProcessed);
        CheckBox isBatchItem = dialog.findViewById(R.id.isBatchItem);
        Spinner itemSpinner = dialog.findViewById(R.id.itemSpinner);
        EditText stock = dialog.findViewById(R.id.stock);

        ArrayAdapter<Items> itemAdapter = new ArrayAdapter<>(ItemsActivity.this, android.R.layout.simple_spinner_item, itemsSpinnerList);
        itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemSpinner.setAdapter(itemAdapter);
        Items none = new Items();
        none.setName("None");
        itemsSpinnerList.add(none);
        itemsSpinnerList.addAll(itemsList);
        itemAdapter.notifyDataSetChanged();
        isInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInventory.isChecked())
                    stock.setVisibility(View.VISIBLE);
                else
                    stock.setVisibility(View.GONE);
            }
        });
        dialog.show();

        if (itemsList.size() > 0 && position != -1) {
            selectedItem = itemsList.get(position);
            itemName.setText(selectedItem.getName());
            itemPrice.setText(selectedItem.getPrice() + "");
            itemCost.setText(selectedItem.getCost() + "");
            cashSale.setChecked(selectedItem.getUsedFor().get(Constants.CASHSALES));
            creditSale.setChecked(selectedItem.getUsedFor().get(Constants.CREDITSALES));
            cashPurchase.setChecked(selectedItem.getUsedFor().get(Constants.CASHPURCHASE));
            creditPurchase.setChecked(selectedItem.getUsedFor().get(Constants.CREDITPURCHASE));
            expenses.setChecked(selectedItem.getUsedFor().get(Constants.EXPENSES));
            financeItem.setChecked(selectedItem.getUsedFor().get(Constants.LOAN));

            isInventory.setChecked(selectedItem.getIsInventory());
            isBatchItem.setChecked(selectedItem.getIsBatchItem());
            isProcessed.setChecked(selectedItem.getIsProcessed());
            if (selectedItem.getIsInventory()) {
                stock.setVisibility(View.VISIBLE);
                stock.setText(selectedItem.getRawStock() + "");
            }

            for (Items i : itemsSpinnerList) {
                if (i.getId() != null) {
                    if (i.getId().equals(selectedItem.getRawMaterial())) {
                        itemSpinner.setSelection(itemAdapter.getPosition(i));
                        break;
                    } else itemSpinner.setSelection(0);
                }
            }
        }

        Items finalSelectedItem = selectedItem;

        savePopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View v1 = getCurrentFocus();
                if (v1 != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v1.getWindowToken(), 0);
                }

                if (itemName.getText().toString().length() == 0)
                    return;

                Items item = new Items();
                item.setId(finalSelectedItem.getId());
                item.setName(itemName.getText().toString());
                item.setPrice(UHelper.parseDouble(itemPrice.getText().toString()));
                item.setCost(UHelper.parseDouble(itemCost.getText().toString()));
                HashMap<String, Boolean> usedFor = new HashMap<>();
                usedFor.put(Constants.CASHSALES, cashSale.isChecked());
                usedFor.put(Constants.CREDITSALES, creditSale.isChecked());
                usedFor.put(Constants.CASHPURCHASE, cashPurchase.isChecked());
                usedFor.put(Constants.CREDITPURCHASE, creditPurchase.isChecked());
                usedFor.put(Constants.EXPENSES, expenses.isChecked());
                usedFor.put(Constants.LOAN, financeItem.isChecked());
                item.setUsedFor(usedFor);
                item.setIsInventory(isInventory.isChecked());
                item.setIsProcessed(isProcessed.isChecked());
                item.setIsBatchItem(isBatchItem.isChecked());
                if (item.getIsInventory())
                    item.setRawStock(UHelper.parseDouble(stock.getText().toString()));
                if (itemSpinner.getSelectedItemPosition() != 0) {
                    if (itemsSpinnerList.get(itemSpinner.getSelectedItemPosition()).getIsInventory())
                        item.setRawMaterial(itemsSpinnerList.get(itemSpinner.getSelectedItemPosition()).getId());
                    else {
                        toast(itemsSpinnerList.get(itemSpinner.getSelectedItemPosition()).getName() + " - is not an Inventory Item");
                        return;
                    }
                } else item.setRawMaterial(null);

                saveOrUpdate(item, position);
            }
        });
        deletePopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View v1 = getCurrentFocus();
                if (v1 != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v1.getWindowToken(), 0);
                }
                if (finalSelectedItem.getId() != null) {
                    showProgressBar(true);
                    firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ITEMS).document(finalSelectedItem.getId())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    showProgressBar(false);
                                    itemsList.remove(position);
                                    adapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                }

                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showProgressBar(false);
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });
                }
            }


        });
        closePopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View v1 = getCurrentFocus();
                if (v1 != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v1.getWindowToken(), 0);
                }
                dialog.dismiss();
                dialog = null;
            }
        });

    }

    public void saveOrUpdate(Items item, int position) {
        if (item.getIsInventory() && item.getRawMaterial() != null) {
            toast("Item can be either Inventory Item or Raw Material");
            return;
        }

        if (item.getId() != null) {
            showProgressBar(true);
            firebaseFirestore.collection(Constants.USERS).document(mAuth.getUid()).collection(Constants.ITEMS).document(item.getId())
                    .set(item, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            toast("Item " + item.getName() + " updated successfully");
                            itemsList.set(position, item);
                            showProgressBar(false);
                            dialog.dismiss();
                            dialog = null;
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showProgressBar(false);
                            Log.d(TAG, "Error writing document", e);
                        }
                    });
        } else {

            // Add a new document with a generated ID
            DocumentReference documentItem = firebaseFirestore.collection(Constants.USERS).document(mAuth.getCurrentUser().getUid()).collection(Constants.ITEMS).document();
            item.setId(documentItem.getId());
            documentItem.set(item, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    showProgressBar(false);
                    toast("Item " + item.getName() + " added successfully");
                    itemsList.add(item);
                    itemsSpinnerList.add(item);
                    dialog.dismiss();
                    dialog = null;
                    adapter.notifyDataSetChanged();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showProgressBar(false);
                    Log.d(TAG, "Error adding document try again " + e);

                }
            });
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add)
            showPopUp(-1);
        return super.onOptionsItemSelected(item);
    }

}
