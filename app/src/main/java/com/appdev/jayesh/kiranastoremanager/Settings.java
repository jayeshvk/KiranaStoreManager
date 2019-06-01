package com.appdev.jayesh.kiranastoremanager;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.Model.Transaction;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Settings extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 99;

    List<Transaction> transactionList = new ArrayList<>();
    List<Items> itemsList = new ArrayList<>();
    List<Accounts> accountsList = new ArrayList<>();

    private ProgressDialog pDialog;
    EditText fromdate, todate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        fromdate = findViewById(R.id.fromdate);
        todate = findViewById(R.id.todate);
        fromdate.setText(UHelper.setPresentDateddMMyyyy());
        todate.setText(UHelper.setPresentDateddMMyyyy());

    }

    public void buttonExport(View view) {
        checkStorageAccess();
    }

    private void checkStorageAccess() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Settings.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(Settings.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            loadTransaction();
            // Permission has already been granted
        }
    }

    private void selectDIR() {

        //open folder dialog box
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        FilePickerDialog dialog = new FilePickerDialog(Settings.this, properties);
        dialog.setTitle("Select a Directory");

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                if (files.length > 0)
                    //new exportTOexcel().execute(files);
                    new exportTOexcel().execute(files[0]);
            }

        });
        dialog.show();


    }

    private void exportToExcel(String file) {
        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("KIRANA", "Storage not available or read only");
            return;
        }

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet newSheet = workbook.createSheet("Sheet1");

        // Column Headings
        String[] columNames = {"TRANSACTION DATE", "CREATED", "TRANSACTION", "TRANSACTION TYPE",
                "ACCOUNT NAME", "ITEM NAME", "QUANTITY", "UOM", "PRICE", "AMOUNT", "NOTES", "ACCOUNT ID", "ITEM ID"};

        //Cell style for header row
        CellStyle cs = workbook.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        // create columns for headings starting from column 0
        HSSFRow HSSFRow = newSheet.createRow(0);
        for (int i = 0; i < columNames.length; i++) {
            HSSFCell Cells = HSSFRow.createCell(i);
            Cells.setCellValue(columNames[i]);
            Cells.setCellStyle(cs);
        }

        // insert data
        for (int i = 0; i < transactionList.size(); i++) {
            System.out.println(transactionList.get(i).getItemName());
            org.apache.poi.hssf.usermodel.HSSFRow dataHSSFRow = newSheet.createRow(i + 1);

            //Transaction Date
            HSSFCell tdate = dataHSSFRow.createCell(0);
            tdate.setCellValue(UHelper.militoddmmyyyy(transactionList.get(i).getTimeInMilli()));
            //Created on
            HSSFCell createdate = dataHSSFRow.createCell(1);
            createdate.setCellValue(UHelper.militoddmmyyyy(transactionList.get(i).getTimestamp()));
            //transaction
            HSSFCell transaction = dataHSSFRow.createCell(2);
            transaction.setCellValue(transactionList.get(i).getTransaction());
            //transaction type
            HSSFCell ttype = dataHSSFRow.createCell(3);
            ttype.setCellValue(transactionList.get(i).getTransactionType());
            //Account name
            HSSFCell accountName = dataHSSFRow.createCell(4);
            accountName.setCellValue(transactionList.get(i).getAccountName());
            //Item Name
            HSSFCell itemName = dataHSSFRow.createCell(5);
            itemName.setCellValue(transactionList.get(i).getItemName());
            //quantity
            HSSFCell quantity = dataHSSFRow.createCell(6);
            quantity.setCellValue(transactionList.get(i).getQuantity());
            //UOM
            HSSFCell uom = dataHSSFRow.createCell(7);
            uom.setCellValue(transactionList.get(i).getUom());
            //price
            HSSFCell price = dataHSSFRow.createCell(8);
            price.setCellValue(transactionList.get(i).getPrice());
            //amount
            HSSFCell amount = dataHSSFRow.createCell(9);
            amount.setCellValue(transactionList.get(i).getAmount());
            //Note
            HSSFCell note = dataHSSFRow.createCell(10);
            note.setCellValue(transactionList.get(i).getNotes());
            //account id
            HSSFCell accountId = dataHSSFRow.createCell(11);
            accountId.setCellValue(transactionList.get(i).getAccountId());
            //Note
            HSSFCell itemId = dataHSSFRow.createCell(12);
            itemId.setCellValue(transactionList.get(i).getItemId());
        }

        try {
            System.out.println("File " + file);
            String filePath = file + "/" + "KiranaStoreManager" + UHelper.setPresentDateddMMyyyy() + ".xls";
            FileOutputStream fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Error" + e);
        }

    }

    private void loadTransaction() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection(Constants.USERS).document(user.getUid());

        showProgressBar(true, "Downloading Transaction List");
        Query query = documentReference.collection(Constants.TRANSACTIONS).whereGreaterThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(fromdate.getText().toString() + " 00:00:00"))
                .whereLessThanOrEqualTo("timeInMilli", UHelper.ddmmyyyyhmsTomili(todate.getText().toString() + " 23:59:59"));
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                        transactionList.add(q.toObject(Transaction.class));
                    }
                    showProgressBar(false);
                    selectDIR();
                }

            }
        });

        query.get().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showProgressBar(false);
                toast("Failes :" + e);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is canHSSFCelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadTransaction();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    class exportTOexcel extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            showProgressBar(true, "Creating Excel File");
        }

        @Override
        protected Void doInBackground(String... params) {

            String file = params[0];
            // check if available and not read only
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                Log.e("KIRANA", "Storage not available or read only");
                return null;
            }

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet newSheet = workbook.createSheet("Sheet1");

            // Column Headings
            String[] columNames = {"TRANSACTION DATE", "CREATED", "TRANSACTION", "TRANSACTION TYPE",
                    "ACCOUNT NAME", "ITEM NAME", "QUANTITY", "UOM", "PRICE", "AMOUNT", "NOTES", "ACCOUNT ID", "ITEM ID"};

            //Cell style for header row
            CellStyle cs = workbook.createCellStyle();
            cs.setFillForegroundColor(HSSFColor.LIME.index);
            cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            // create columns for headings starting from column 0
            HSSFRow HSSFRow = newSheet.createRow(0);
            for (int i = 0; i < columNames.length; i++) {
                HSSFCell Cells = HSSFRow.createCell(i);
                Cells.setCellValue(columNames[i]);
                Cells.setCellStyle(cs);
            }

            // insert data
            for (int i = 0; i < transactionList.size(); i++) {
                System.out.println(transactionList.get(i).getItemName());
                org.apache.poi.hssf.usermodel.HSSFRow dataHSSFRow = newSheet.createRow(i + 1);

                //Transaction Date
                HSSFCell tdate = dataHSSFRow.createCell(0);
                tdate.setCellValue(UHelper.militoddmmyyyy(transactionList.get(i).getTimeInMilli()));
                //Created on
                HSSFCell createdate = dataHSSFRow.createCell(1);
                createdate.setCellValue(UHelper.militoddmmyyyy(transactionList.get(i).getTimestamp()));
                //transaction
                HSSFCell transaction = dataHSSFRow.createCell(2);
                transaction.setCellValue(transactionList.get(i).getTransaction());
                //transaction type
                HSSFCell ttype = dataHSSFRow.createCell(3);
                ttype.setCellValue(transactionList.get(i).getTransactionType());
                //Account name
                HSSFCell accountName = dataHSSFRow.createCell(4);
                accountName.setCellValue(transactionList.get(i).getAccountName());
                //Item Name
                HSSFCell itemName = dataHSSFRow.createCell(5);
                itemName.setCellValue(transactionList.get(i).getItemName());
                //quantity
                HSSFCell quantity = dataHSSFRow.createCell(6);
                quantity.setCellValue(transactionList.get(i).getQuantity());
                //UOM
                HSSFCell uom = dataHSSFRow.createCell(7);
                uom.setCellValue(transactionList.get(i).getUom());
                //price
                HSSFCell price = dataHSSFRow.createCell(8);
                price.setCellValue(transactionList.get(i).getPrice());
                //amount
                HSSFCell amount = dataHSSFRow.createCell(9);
                amount.setCellValue(transactionList.get(i).getAmount());
                //Note
                HSSFCell note = dataHSSFRow.createCell(10);
                note.setCellValue(transactionList.get(i).getNotes());
                //account id
                HSSFCell accountId = dataHSSFRow.createCell(11);
                accountId.setCellValue(transactionList.get(i).getAccountId());
                //Note
                HSSFCell itemId = dataHSSFRow.createCell(12);
                itemId.setCellValue(transactionList.get(i).getItemId());
            }

            try {
                System.out.println("File " + file);
                String filePath = file + "/" + "KiranaStoreManager" + UHelper.setPresentDateddMMyyyy() + ".xls";
                FileOutputStream fileOut = new FileOutputStream(filePath);
                workbook.write(fileOut);
                fileOut.close();

            } catch (Exception e) {
                System.out.println("Error" + e);
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            showProgressBar(false);
            toast("File Created Successfully");
        }
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

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
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

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

}
