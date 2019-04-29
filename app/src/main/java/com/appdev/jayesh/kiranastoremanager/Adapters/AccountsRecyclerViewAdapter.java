package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Model.Accounts;
import com.appdev.jayesh.kiranastoremanager.R;

import java.util.List;

public class AccountsRecyclerViewAdapter extends RecyclerView.Adapter<AccountsRecyclerViewAdapter.MyViewHolder> {

    private List<Accounts> accountsList;

    public AccountsRecyclerViewAdapter(List<Accounts> accountsList) {
        this.accountsList = accountsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list_item_accounts, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Accounts account = accountsList.get(position);
        holder.name.setText(account.getName());
        holder.customer.setChecked(account.isCustomer());
        holder.vendor.setChecked(account.isVendor());

    }

    @Override
    public int getItemCount() {
        return accountsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public CheckBox customer, vendor;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            customer = view.findViewById(R.id.customer);
            vendor = view.findViewById(R.id.vendor);
        }

    }
}