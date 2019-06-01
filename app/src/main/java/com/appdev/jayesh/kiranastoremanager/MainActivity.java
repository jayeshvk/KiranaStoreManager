package com.appdev.jayesh.kiranastoremanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.ExpandableMenu.ExpandableListAdapter;
import com.appdev.jayesh.kiranastoremanager.ExpandableMenu.ExpandedMenuModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;
    ExpandableListAdapter mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;
    private int lastExpandedPosition = -1;
    ProgressDialog progressDialog;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigationDrawer();
        PrepareMenu();
        populateExpandableList();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        db = FirebaseFirestore.getInstance();

        TextView userTv = findViewById(R.id.userTv);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            userTv.setText("Current User : " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
        else
            startActivity(new Intent(MainActivity.this, SignIn.class));

    }

    //Drawer Navigation Related Code*******************************************
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            //to collapse all the menu item on toggle
            int count = mMenuAdapter.getGroupCount();
            for (int i = 0; i < count; i++)
                expandableList.collapseGroup(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mNavigationView = findViewById(R.id.navigation_view);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                return false;
            }
        });
    }

    private void PrepareMenu() {
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();

        final String HeaderItem1 = "Accounts";

        ExpandedMenuModel account = new ExpandedMenuModel(HeaderItem1, R.drawable.ic_account_circle, false);
        // Adding data header
        listDataHeader.add(account);

        ExpandedMenuModel items = new ExpandedMenuModel("Items", R.drawable.ic_account_circle, false);
        listDataHeader.add(items);

        ExpandedMenuModel settings = new ExpandedMenuModel("Settings", R.drawable.ic_account_circle, false);
        listDataHeader.add(settings);


        ExpandedMenuModel report = new ExpandedMenuModel("Report", R.drawable.ic_account_circle, true);
        listDataHeader.add(report);
        List<String> reportItems = new ArrayList<>();
        reportItems.add("Sales Orders");
        reportItems.add("Date Report");
        reportItems.add("Item Summary");
        reportItems.add("Sales Summary");
        listDataChild.put(report, reportItems);

        ExpandedMenuModel logout = new ExpandedMenuModel("Logout", R.drawable.ic_account_circle, false);
        listDataHeader.add(logout);

    }

    private void populateExpandableList() {
        expandableList = findViewById(R.id.navigationmenu);
        mMenuAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, expandableList);

        // setting list recyclerViewAdapter
        expandableList.setAdapter(mMenuAdapter);

        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {

                if (listDataChild.get(listDataHeader.get(groupPosition)) != null) {
                    String subMenu = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                    actOnMenuClick(subMenu);
                }
                return false;
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                actOnMenuClick(listDataHeader.get(i).getMainMenu());
                return false;
            }
        });
        expandableList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    expandableList.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

    }

    private void actOnMenuClick(String subMenu) {
        switch (subMenu) {
            case "Items":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivity(new Intent(MainActivity.this, ItemsActivity.class));
                break;
            case "Accounts":
                mDrawerLayout.closeDrawer(Gravity.START);
                //startActivity(new Intent(MainActivity.this, Reports.class));
                startActivity(new Intent(MainActivity.this, AccountsActivity.class));

                break;
            case "Date Report":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivity(new Intent(MainActivity.this, DateReport.class));
                break;
            case "Sales Orders":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivity(new Intent(MainActivity.this, SalesOrdersReport.class));
                break;
            case "Settings":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivity(new Intent(MainActivity.this, Settings.class));

                break;
            case "Item Summary":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivity(new Intent(MainActivity.this, ItemSummaryReport.class));

                break;
            case "Days Summary":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivity(new Intent(MainActivity.this, DaysSummaryReport.class));

                break;
            case "Sales Summary":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivity(new Intent(MainActivity.this, SalesSummaryReport.class));


                break;
            case "Logout":
                progressDialog.show();
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.cancel();
                                if (task.isSuccessful())
                                    startActivity(new Intent(MainActivity.this, SignIn.class));
                                else toast("Sign out failed, please try again");
                            }
                        });
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void cashSales(View view) {
        Intent intent = new Intent(MainActivity.this, CashSales.class);
        intent.putExtra(Constants.TRANSACTIONTYPE, Constants.CASHSALES);
        intent.putExtra(Constants.SIGN, 1);
        intent.putExtra("title", "Cash Sales");
        startActivity(intent);

    }

    public void creditSales(View view) {
        Intent intent = new Intent(MainActivity.this, CreditSales.class);
        intent.putExtra(Constants.TRANSACTIONTYPE, Constants.CREDITSALES);
        intent.putExtra(Constants.TRANSACTIONTYPEREVERSE, Constants.CUSTOMERPAYMENTS);
        intent.putExtra(Constants.ACCOUNTS, Constants.customer);
        intent.putExtra(Constants.SIGN, -1);
        intent.putExtra(Constants.TITLE, "Credit Sales");
        startActivity(intent);
    }

    public void cashPurchase(View view) {
        Intent intent = new Intent(MainActivity.this, CashSales.class);
        intent.putExtra(Constants.TRANSACTIONTYPE, Constants.CASHPURCHASE);
        intent.putExtra(Constants.TITLE, "Cash Purchase");
        intent.putExtra(Constants.SIGN, -1);
        startActivity(intent);

    }

    public void expenses(View view) {
        Intent intent = new Intent(MainActivity.this, CashSales.class);
        intent.putExtra(Constants.TRANSACTIONTYPE, Constants.EXPENSES);
        intent.putExtra(Constants.TITLE, Constants.EXPENSES);
        intent.putExtra(Constants.SIGN, -1);

        startActivity(intent);
    }

    public void creditPurchase(View view) {
        Intent intent = new Intent(MainActivity.this, CreditSales.class);
        intent.putExtra(Constants.TRANSACTIONTYPE, Constants.CREDITPURCHASE);
        intent.putExtra(Constants.TITLE, "Credit Purchase");
        intent.putExtra(Constants.TRANSACTIONTYPEREVERSE, Constants.VENDORPAYMENTS);
        intent.putExtra(Constants.ACCOUNTS, Constants.vendor);
        intent.putExtra(Constants.SIGN, 1);

        startActivity(intent);
    }


    public void banking(View view) {
        Intent intent = new Intent(MainActivity.this, Banking.class);
        startActivity(intent);
    }

    public void loan(View view) {
        Intent intent = new Intent(MainActivity.this, CreditSales.class);
        intent.putExtra(Constants.TRANSACTIONTYPE, Constants.LOAN);
        intent.putExtra(Constants.TRANSACTIONTYPEREVERSE, Constants.LOANPAYMENT);
        intent.putExtra(Constants.ACCOUNTS, Constants.lender);
        intent.putExtra(Constants.TITLE, "Loan Management");
        startActivity(intent);
    }

    public void salesOrder(View view) {
        Intent intent = new Intent(MainActivity.this, SalesOrderActivity.class);
        startActivity(intent);
    }
}
