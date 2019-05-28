package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Model.DaySummary;
import com.appdev.jayesh.kiranastoremanager.R;

import java.util.List;

public class DaySummaryViewAdapter extends RecyclerView.Adapter<DaySummaryViewAdapter.MyViewHolder> {

    private List<DaySummary> daySummaries;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView date, total, cashSales, customerpayments, loan, cashpurchases, loanpayments, vendorpayments, expenses, creditSales, creditpurchases;

        MyViewHolder(View view) {
            super(view);
            date = view.findViewById(R.id.date);
            total = view.findViewById(R.id.total);
            cashSales = view.findViewById(R.id.cashsales);
            loan = view.findViewById(R.id.loan);
            customerpayments = view.findViewById(R.id.customerpayments);
            cashpurchases = view.findViewById(R.id.cashpurchases);
            loanpayments = view.findViewById(R.id.loanpayments);
            vendorpayments = view.findViewById(R.id.vendorpayments);
            expenses = view.findViewById(R.id.expenses);
            creditSales = view.findViewById(R.id.creditSales);
            creditpurchases = view.findViewById(R.id.creditpurchases);
        }

    }

    public DaySummaryViewAdapter(List<DaySummary> daySummaries) {
        this.daySummaries = daySummaries;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list_item_day_summary, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DaySummary daySummary = daySummaries.get(position);
        holder.date.setText(daySummary.getDate());
        holder.total.setText(daySummary.getSUM() + "");
        holder.cashSales.setText(daySummary.getCASHSALES() + "");
        holder.loan.setText(daySummary.getLOAN() + "");
        holder.customerpayments.setText(daySummary.getCUSTOMERPAYMENTS() + "");
        holder.cashpurchases.setText(daySummary.getCASHPURCHASES() + "");
        holder.loanpayments.setText(daySummary.getLOANPAYMENT() + "");
        holder.expenses.setText(daySummary.getEXPENSES() + "");
        holder.vendorpayments.setText(daySummary.getVENDORPAYMENTS() + "");
        holder.creditSales.setText(daySummary.getCREDITSALES() + "");
        holder.creditpurchases.setText(daySummary.getCREDITPURCHASES() + "");
    }

    @Override
    public int getItemCount() {
        return daySummaries.size();
    }
}