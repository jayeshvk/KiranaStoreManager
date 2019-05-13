package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Model.SalesOrder;
import com.appdev.jayesh.kiranastoremanager.R;
import com.appdev.jayesh.kiranastoremanager.UHelper;

import java.util.List;

public class SalesOrderReportRecyclerViewAdapter extends RecyclerView.Adapter<SalesOrderReportRecyclerViewAdapter.MyViewHolder> {

    public List<SalesOrder> salesOrderList;

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

    public SalesOrderReportRecyclerViewAdapter(List<SalesOrder> salesOrderList) {
        this.salesOrderList = salesOrderList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list__salesorder_report, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SalesOrder salesOrder = salesOrderList.get(position);
        if (salesOrder.getStatus().contains("closed") || salesOrder.getStatus().contains("delivered"))
            holder.date.setText(UHelper.militoddmmyyyy(salesOrder.getAdd()));
        else
            holder.date.setText(UHelper.militoddmmyyyy(salesOrder.getCreated()));
        holder.itemName.setText(salesOrder.getItemName());
        holder.quantity.setText(salesOrder.getQuantity() + "");
        holder.price.setText(salesOrder.getPrice() + "");
        holder.amount.setText(salesOrder.getAmount() + "");
        holder.uom.setText(salesOrder.getUom() + "");
    }


    @Override
    public int getItemCount() {
        return salesOrderList.size();
    }
}