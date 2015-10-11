package com.example.shubhamkanodia.roadrunner;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shubhamkanodia.roadrunner.Models.RecordingItem;

import java.util.List;

/**
 * Created by shubhamkanodia on 04/09/15.
 */

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.BindingHolder>  {
    private List<RecordingItem> records;
     static Context context;


    public static class BindingHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ViewDataBinding binding;

        public BindingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(context, "Clicky", Toast.LENGTH_SHORT).show();
        }
    }

    public RecordingsAdapter(Context context, List<RecordingItem> records) {
        this.records = records;
        this.context = context;
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
        final RecordingItem record = records.get(position);
        holder.getBinding().setVariable(com.example.shubhamkanodia.roadrunner.BR.record, record);
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}