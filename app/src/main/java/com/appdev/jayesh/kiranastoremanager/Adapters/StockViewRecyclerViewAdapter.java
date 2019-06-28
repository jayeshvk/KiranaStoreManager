package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.R;

import java.util.List;

public class StockViewRecyclerViewAdapter extends RecyclerView.Adapter<StockViewRecyclerViewAdapter.MyViewHolder> {

    private List<Items> itemsList;

    public StockViewRecyclerViewAdapter(List<Items> itemsList) {
        this.itemsList = itemsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list_item_stock_view, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Items item = itemsList.get(position);
        holder.name.setText(item.getName());
        holder.quantity.setText(String.format("%.2f", item.getRawStock()));

    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, quantity;

        MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            quantity = view.findViewById(R.id.quantity);
        }

    }
}