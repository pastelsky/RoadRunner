package com.example.shubhamkanodia.roadrunner;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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
//}
//public class RecordingsAdapter extends ArrayAdapter<RecordingItem> {
//
//
//        private final Activity context;
//        private final ArrayList<RecordingItem> records;
//
//        static class ViewHolder {
//            public TextView tvTime;
//            public TextView tvUploadProgress;
//            public TextView tvDistance;
//            public ProgressBar pbUploadProgress;
//            public ImageView ivSyncIcon;
//        }
//
//        public RecordingsAdapter(Activity context, ArrayList<RecordingItem> records) {
//            super(context, R.layout.list_record, records);
//            this.context = context;
//            this.records = records;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View rowView = convertView;
//            // reuse views
//            if (rowView == null) {
//                LayoutInflater inflater = context.getLayoutInflater();
//                rowView = inflater.inflate(R.layout.list_record, null);
//
//                ViewHolder viewHolder = new ViewHolder();
//                viewHolder.tvTime = (TextView) rowView.findViewById(R.id.tvTime);
//                viewHolder.tvUploadProgress = (TextView) rowView.findViewById(R.id.tvUploadProgress);
//                viewHolder.tvDistance = (TextView) rowView.findViewById(R.id.tvDistance);
//                viewHolder.pbUploadProgress = (ProgressBar) rowView.findViewById(R.id.pbUploadProgress);
//                viewHolder.ivSyncIcon = (ImageView) rowView.findViewById(R.id.ivSyncIcon);
//
//                rowView.setTag(viewHolder);
//            }
//
//            ViewHolder holder = (ViewHolder) rowView.getTag();
//            RecordingItem r = records.get(position);
//
//            holder.tvTime.setText( r.start_time + " - " + r.end_time);
//            holder.tvDistance.setText( Haversine.haversine(r.start_Lat,r.start_Long, r.end_Lat, r.end_Long) + " ");
//
//            if(r.isSynced || !r.isSyncing) {
//                holder.pbUploadProgress.setVisibility(View.GONE);
//                holder.tvUploadProgress.setVisibility(View.GONE);
//
//            }
//
//            if(r.isSynced)
//                holder.ivSyncIcon.setImageResource(R.mipmap.ic_synced);
//
//            if(r.isSyncing) {
//
//                holder.ivSyncIcon.setImageResource(R.mipmap.ic_syncing);
//                Animation rotateAnim = new RotateAnimation(0f, 360f);
//                rotateAnim.setDuration(400);
//                rotateAnim.setRepeatCount(Animation.INFINITE);
//                rotateAnim.setInterpolator(new LinearInterpolator());
//                holder.ivSyncIcon.startAnimation(rotateAnim);
//
//            }
//
//            return rowView;
//        }
//}
