package com.appdev.jayesh.kiranastoremanager.Adapters;

import android.content.DialogInterface;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.appdev.jayesh.kiranastoremanager.Model.Transaction;
import com.appdev.jayesh.kiranastoremanager.R;
import com.appdev.jayesh.kiranastoremanager.UHelper;

import java.util.ArrayList;
import java.util.List;

public class TransactionsRecyclerViewAdapter extends RecyclerView.Adapter<TransactionsRecyclerViewAdapter.MyViewHolder> implements Filterable {

    public List<Transaction> transactionList;
    public List<Transaction> transactionListFull;
    private List<Transaction> transactionListFiltered;
    public double total;


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        EditText quantity, price, amount, uom;
        ImageView note;

        MyViewHolder(final View view) {
            super(view);

            itemName = view.findViewById(R.id.itemName);
            quantity = view.findViewById(R.id.quantity);
            price = view.findViewById(R.id.price);
            amount = view.findViewById(R.id.amount);
            note = view.findViewById(R.id.note);
            uom = view.findViewById(R.id.uom);

            quantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    double qty = UHelper.parseDouble(quantity.getText().toString());
                    double prc = UHelper.parseDouble(price.getText().toString());
                    amount.setText(String.format("%.2f", (qty * prc)));

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            price.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    double qty = UHelper.parseDouble(quantity.getText().toString());
                    double prc = UHelper.parseDouble(price.getText().toString());
                    amount.setText(String.format("%.2f", (qty * prc)));
                }


                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            amount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    transactionList.get(getAdapterPosition()).setQuantity(UHelper.parseDouble(quantity.getText().toString()));
                    transactionList.get(getAdapterPosition()).setPrice(UHelper.parseDouble(price.getText().toString()));
                    transactionList.get(getAdapterPosition()).setAmount(UHelper.parseDouble(amount.getText().toString()));
                    total = UHelper.parseDouble(amount.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

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
                                note.setColorFilter(Color.parseColor("#303F9F"));
                            } else
                                note.setColorFilter(Color.BLACK);

                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                }
            });

            uom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(view.getContext(), v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.uom, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.kg:
                                    transactionList.get(getAdapterPosition()).setUom(item.getTitle().toString());
                                    uom.setText(item.getTitle());
                                    break;
                                case R.id.gms:
                                    transactionList.get(getAdapterPosition()).setUom(item.getTitle().toString());
                                    uom.setText(item.getTitle());
                                    break;
                                case R.id.bag:
                                    transactionList.get(getAdapterPosition()).setUom(item.getTitle().toString());
                                    uom.setText(item.getTitle());
                                    break;
                                case R.id.pce:
                                    transactionList.get(getAdapterPosition()).setUom(item.getTitle().toString());
                                    uom.setText(item.getTitle());
                                    break;
                                default:
                                    return false;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });

        }

    }


    public TransactionsRecyclerViewAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
        this.transactionListFull = transactionList;
        this.transactionListFiltered = transactionList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_list_item_transactions, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        String price = transaction.getPrice() + "";
        holder.itemName.setText(transaction.getItemName());
        holder.quantity.setText("");
        holder.price.setText(price);
        holder.amount.setText("");
        holder.uom.setText("");
        //System.out.println("Position" + position);


/*        if (transaction.getQuantity() > 0)
            holder.quantity.setText(transaction.getQuantity() + "");
        else
            holder.quantity.setText("");
        if (transaction.getPrice() > 0)
            holder.price.setText(transaction.getPrice() + "");
        else
            holder.price.setText("");
        if (transaction.getAmount() > 0)
            holder.amount.setText(transaction.getAmount() + "");
        else
            holder.amount.setText("");
        if (transaction.getUom() == null)
            holder.uom.setText("");
        else
            holder.uom.setText(transaction.getUom() + "");*/

        holder.note.setColorFilter(Color.BLACK);
        transaction.setNotes("");

    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Transaction> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(transactionListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Transaction item : transactionListFull) {
                    if (item.getItemName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            transactionList = (ArrayList<Transaction>) results.values;
            notifyDataSetChanged();
        }
    };

}