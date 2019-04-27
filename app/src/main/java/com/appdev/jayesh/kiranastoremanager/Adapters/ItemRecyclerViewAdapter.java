package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Model.Items;
import com.appdev.jayesh.kiranastoremanager.R;

import java.util.List;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.MyViewHolder> {

    private List<Items> itemsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, key, price;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            price = view.findViewById(R.id.price);
            key = view.findViewById(R.id.key);
        }

    }

    public ItemRecyclerViewAdapter(List<Items> backupFileList) {
        this.itemsList = backupFileList;
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
        holder.price.setText(item.getPrice() + "");
        holder.key.setText(item.getKey());
    }


    @Override
    public int getItemCount() {
        return itemsList.size();
    }
}