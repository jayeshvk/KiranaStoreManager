package com.appdev.jayesh.kiranastoremanager.Adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Constants;
import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.R;

import java.util.List;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.viewHolder> {

    private List<Items> itemsList;

    public class viewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        private CheckBox cashSale, creditSale, cashPurchase, creditPurchase, expenses, financeItem;

        public viewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            cashSale = itemView.findViewById(R.id.cashSale);
            creditSale = itemView.findViewById(R.id.creditSale);
            cashPurchase = itemView.findViewById(R.id.cashPurchase);
            creditPurchase = itemView.findViewById(R.id.creditPurchase);
            expenses = itemView.findViewById(R.id.expenses);
            financeItem = itemView.findViewById(R.id.financeItem);
        }

    }

    public ItemRecyclerViewAdapter(List<Items> itemlList) {
        this.itemsList = itemlList;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list_item_items, parent, false);

        return new viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {
        Items item = itemsList.get(position);
        holder.name.setText(item.getName());
        if (item.getUsedFor() != null) {
            holder.cashSale.setChecked(item.getUsedFor().get(Constants.CASHSALES));
            holder.creditSale.setChecked(item.getUsedFor().get(Constants.CREDITSALES));
            holder.cashPurchase.setChecked(item.getUsedFor().get(Constants.CASHPURCHASE));
            holder.creditPurchase.setChecked(item.getUsedFor().get(Constants.CREDITPURCHASE));
            holder.expenses.setChecked(item.getUsedFor().get(Constants.EXPENSES));
            holder.financeItem.setChecked(item.getUsedFor().get(Constants.LOAN));
        }
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

}