package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.R;

import java.util.List;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.MyViewHolder> {

    private List<Items> itemsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public CheckBox cashSale, creditSale, cashPurchase, creditPurchase, otherPayments;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            cashSale = view.findViewById(R.id.cashSale);
            creditSale = view.findViewById(R.id.creditSale);
            cashPurchase = view.findViewById(R.id.cashPurchase);
            creditPurchase = view.findViewById(R.id.creditPurchase);
            otherPayments = view.findViewById(R.id.otherPayments);
            System.out.println("Clicked on " + cashSale.isChecked());

            cashSale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Clicked on " + cashSale.isChecked());

                }
            });
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Clicked on item name" + cashSale.isChecked());

                }
            });

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
            holder.cashSale.setChecked(item.getUsedFor().get("CashSale"));
            holder.creditSale.setChecked(item.getUsedFor().get("CreditSale"));
            holder.cashPurchase.setChecked(item.getUsedFor().get("CashPurchase"));
            holder.creditPurchase.setChecked(item.getUsedFor().get("CreditPurchase"));
            holder.otherPayments.setChecked(item.getUsedFor().get("OtherPayments"));
        }
    }


    @Override
    public int getItemCount() {
        return itemsList.size();
    }
}