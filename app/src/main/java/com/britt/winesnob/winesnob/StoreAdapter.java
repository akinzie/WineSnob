package com.britt.winesnob.winesnob;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sap.cloud.mobile.foundation.securestore.SecureDatabaseResultSet;

import java.util.ArrayList;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private int mNumberOfItems;
    private ArrayList<Store> stores;

    private int onCreateViewHolderCalled = 0;
    private int onBindViewHolderCalled = 0;
    private int onGetItemsCalled = 0;

    public StoreAdapter (ArrayList<Store> stores) {
        this.stores = stores;

        mNumberOfItems = stores.size();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d("STOREADAPTER", "onCreateViewHolder called + " + onCreateViewHolderCalled + " times");
        onCreateViewHolderCalled++;

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.storeviewlayout, parent, false);

        StoreViewHolder holder = new StoreViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Log.d("STOREADAPTER", "onBindViewHolder called " + onBindViewHolderCalled + " times");
        onBindViewHolderCalled++;

        Store currStore = stores.get(position);

        holder.storeName.setText(currStore.name);

        holder.storeLocation.setText(currStore.add1 + ", " + currStore.city + ", " + currStore.postal_code);

    }

    @Override
    public int getItemCount() {


        Log.d("STOREADAPTER", "getItemCount called " + onGetItemsCalled + " times");
        onGetItemsCalled++;

        return mNumberOfItems;
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder {

        TextView storeName;
        TextView storeLocation;
        LinearLayout parentLayout;

        public StoreViewHolder(View itemView) {
            super(itemView);

            storeName = itemView.findViewById(R.id.storename);
            storeLocation = itemView.findViewById(R.id.location);
            parentLayout = itemView.findViewById(R.id.parent_layout);

        }
    }
}
