package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Constants;
import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.R;

import java.util.List;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.MyViewHolder> {

    private List<Items> itemsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, stock;
        private CheckBox cashSale, creditSale, cashPurchase, creditPurchase, expenses, financeItem;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            cashSale = view.findViewById(R.id.cashSale);
            creditSale = view.findViewById(R.id.creditSale);
            cashPurchase = view.findViewById(R.id.cashPurchase);
            creditPurchase = view.findViewById(R.id.creditPurchase);
            expenses = view.findViewById(R.id.expenses);
            financeItem = view.findViewById(R.id.financeItem);
            stock = view.findViewById(R.id.stock);


        }

    }

    public ItemRecyclerViewAdapter(List<Items> itemlList) {
        this.itemsList = itemlList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list_item_items, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Items item = itemsList.get(position);
        holder.name.setText(item.getName());
        if (item.getUsedFor() != null) {
            holder.cashSale.setChecked(item.getUsedFor().get(Constants.CASHSALES));
            holder.creditSale.setChecked(item.getUsedFor().get(Constants.CREDITSALES));
            holder.cashPurchase.setChecked(item.getUsedFor().get(Constants.CASHPURCHASE));
            holder.creditPurchase.setChecked(item.getUsedFor().get(Constants.CREDITPURCHASE));
            holder.expenses.setChecked(item.getUsedFor().get(Constants.EXPENSES));
            holder.financeItem.setChecked(item.getUsedFor().get(Constants.LOAN));
            holder.stock.setText("Stock Qty: " + item.getRawStock());
        }
    }


    @Override
    public int getItemCount() {
        return itemsList.size();
    }
}