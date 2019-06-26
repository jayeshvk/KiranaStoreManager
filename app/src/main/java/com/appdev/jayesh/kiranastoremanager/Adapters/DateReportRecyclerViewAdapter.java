package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.graphics.Color;
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
        String date = UHelper.militoddmmyyyyhhmma(transaction.getTimeInMilli());
        holder.date.setText(date.split(" ")[0] + "\n" + date.split(" ")[1] + date.split(" ")[2]);
        holder.itemName.setText(transaction.getItemName());
        holder.quantity.setText(transaction.getQuantity() + "");
        holder.price.setText(transaction.getPrice() + "");
        holder.amount.setText(transaction.getAmount() + "");
        String uom = transaction.getUom() != null ? transaction.getUom() : "*";
        holder.uom.setText(uom);

        if (transaction.getNotes() != null) {
            if (transaction.getNotes().trim().length() > 0)
                holder.itemName.setTextColor(Color.BLUE);
            else
                holder.itemName.setTextColor(Color.BLACK);
        }
    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}