package com.britt.winesnob.winesnob;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.britt.winesnob.winesnob.databinding.ObjectCellLayoutBinding;

import com.britt.winesnob.winesnob.databinding.ObjectCellLayoutBinding;
import com.sap.cloud.mobile.fiori.object.ObjectCell;
import com.sap.cloud.mobile.foundation.securestore.SecureDatabaseResultSet;

import java.util.ArrayList;

public class ObjectCellStoreAdapter extends RecyclerView.Adapter<ObjectCellStoreAdapter.StoreViewHolder> {

    private int mNumberOfItems;
    private ArrayList<Store> stores;
    private Context viewContext;

    public ObjectCellStoreAdapter (Context context, ArrayList<Store> stores) {
        this.stores = stores;

        viewContext = context;

        mNumberOfItems = stores.size();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("STOREADAPTER", "onCreateViewHolder called");

        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.object_cell_layout, parent, false);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.object_cell_layout, parent, false);

        //StoreViewHolder holder = new StoreViewHolder(view);
        StoreViewHolder holder = new StoreViewHolder(binding.getRoot());

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, final int position) {
        Log.d("STOREADAPTER", "onBindViewHolder called");

        Store currStore = stores.get(position);

        // For Databinding
        ObjectCellLayoutBinding binding = DataBindingUtil.getBinding(holder.itemView);

        //binding.setCustomer(currStore);
        binding.setStore(currStore);

        // We'll try to use databinding here....
        //holder.currentStore.setHeadline(currStore.name);
        //holder.currentStore.setSubheadline(currStore.add1 + ", " + currStore.city + ", " + currStore.postal_code);

        //BitmapDrawable myImage = ContextCompat.getDrawable(viewContext, R.drawable.martini);
        //Drawable drawable = ContextCompat.getDrawable(viewContext, R.drawable.ic_account_circle_black_24dp);
        //Drawable drawable = ContextCompat.getDrawable(viewContext, R.drawable.martini);
        //holder.currentStore.setDetailImage(drawable);

        holder.currentStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(viewContext, StoreDetail.class);
                viewContext.startActivity(intent);

                //Toast.makeText(viewContext, stores.get(position).name, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNumberOfItems;
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder {

        ObjectCell currentStore;

        public StoreViewHolder(View itemView) {
            super(itemView);

            Log.d("STOREADAPTER", "StoreviewHolder constructor called");
            currentStore = itemView.findViewById(R.id.currStoreCell);

        }
    }
}
