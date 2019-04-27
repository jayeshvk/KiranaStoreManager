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
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.ExpandableMenu.ExpandableListAdapter;
import com.appdev.jayesh.kiranastoremanager.ExpandableMenu.ExpandedMenuModel;
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

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigationDrawer();
        PrepareMenu();
        populateExpandableList();

        db = FirebaseFirestore.getInstance();


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


        ExpandedMenuModel backup = new ExpandedMenuModel("Backup", R.drawable.ic_account_circle, false);
        listDataHeader.add(backup);

        ExpandedMenuModel logout = new ExpandedMenuModel("Logout", R.drawable.ic_account_circle, false);
        listDataHeader.add(logout);

    }

    private void populateExpandableList() {
        expandableList = findViewById(R.id.navigationmenu);
        mMenuAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, expandableList);

        // setting list adapter
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
            case "Detailed Report":
                mDrawerLayout.closeDrawer(Gravity.START);
                //startActivity(new Intent(MainActivity.this, ReportsDetailed.class));
                break;

            case "Settings":
                mDrawerLayout.closeDrawer(Gravity.START);
                break;
            case "Online Backup":
                mDrawerLayout.closeDrawer(Gravity.START);

                break;
            case "Online Restore":
                mDrawerLayout.closeDrawer(Gravity.START);

                break;
            case "Selective Restore":
                mDrawerLayout.closeDrawer(Gravity.START);

                break;
            case "Logout":
                mDrawerLayout.closeDrawer(Gravity.START);
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, Login.class));
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

}
