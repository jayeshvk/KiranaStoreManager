package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Model.Transaction;
import com.appdev.jayesh.kiranastoremanager.R;
import com.appdev.jayesh.kiranastoremanager.UHelper;

import java.util.List;

public class DateReportRecyclerViewAdapter extends RecyclerView.Adapter<DateReportRecyclerViewAdapter.MyViewHolder> {

    public List<Transaction> transactionList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView date, itemName, quantity, price, amount, uom;

        MyViewHolder(final View view) {
            super(view);
            date = view.findViewById(R.id.date);
            itemName = view.findViewById(R.id.itemName);
            quantity = view.findViewById(R.id.quantity);
            price = view.findViewById(R.id.price);
            amount = view.findViewById(R.id.amount);
            uom = view.findViewById(R.id.uom);
/*
            note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                    alert.setTitle("Notes");
                    alert.setIcon(R.drawable.ic_event_note_black_24dp);
                    final EditText input = new EditText(view.getContext());
                    input.setText(transactionList.get(getAdapterPosition()).getNotes());
                    alert.setView(input);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String text = input.getText().toString();
                            transactionList.get(getAdapterPosition()).setNotes(text);
                            if (text.trim().length() > 0) {
                                note.setTextColor(Color.GREEN);
                            } else
                                note.setTextColor(Color.BLACK);

                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    alert.show();
                }
            });
*/

        }

    }

    public DateReportRecyclerViewAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list_item_datereport, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.date.setText(UHelper.militoddmmyyyy(transaction.getTimeInMilli()));
        holder.itemName.setText(transaction.getItemName());
        holder.quantity.setText(transaction.getQuantity() + "");
        holder.price.setText(transaction.getPrice() + "");
        holder.amount.setText(transaction.getAmount() + "");
        holder.uom.setText(transaction.getUom() + "");
    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}