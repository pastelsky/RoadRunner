package com.example.shubhamkanodia.roadrunner.Adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shubhamkanodia.roadrunner.BR;
import com.example.shubhamkanodia.roadrunner.Models.Journey;
import com.example.shubhamkanodia.roadrunner.Models.RecordingItem;
import com.example.shubhamkanodia.roadrunner.R;

import java.util.List;

/**
 * Created by shubhamkanodia on 04/09/15.
 */

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.BindingHolder>  {
     static Context context;
     static private RecyclerViewClickListener itemClicker;
    private List<Journey> journies;


    public RecordingsAdapter(Context context, RecyclerViewClickListener r, List<Journey> journeys) {
        this.journies = journeys;
        this.context = context;
        this.itemClicker = r;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_record, parent, false);
        BindingHolder holder = new BindingHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        final Journey record = journies.get(position);
        holder.getBinding().setVariable(BR.journey, record);
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return journies.size();
    }

    public interface RecyclerViewClickListener
    {
        public void recyclerViewListClicked(View v, int position);
    }

    public static class BindingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ViewDataBinding binding;

        public BindingHolder(View v) {
            super(v);

            v.setOnClickListener(this);
            binding = DataBindingUtil.bind(v);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }

        @Override
        public void onClick(View v) {

            itemClicker.recyclerViewListClicked(v, getAdapterPosition());
        }
    }
}