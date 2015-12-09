package com.ibm.casesdk.sample.edittask.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.casemanagersdk.sdk.interfaces.ICMTask;
import com.ibm.casesdk.sample.edittask.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by stelian on 26/10/2015.
 */
public class TaskListAdapter extends ClickkableRecyclerAdapter<TaskListAdapter.TaskHolder> {

    private Context mContext;
    private ArrayList<ICMTask> mTasks;
    private LayoutInflater mInflater;

    public TaskListAdapter(@NonNull Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mTasks = new ArrayList<>();
    }

    @Override
    public TaskListAdapter.TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskHolder(mContext, mInflater.inflate(R.layout.list_item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(TaskListAdapter.TaskHolder holder, int position) {
        final ICMTask task = mTasks.get(position);

        //  populate the rest of the fields
        holder.taskName.setText(task.getStepName());
        holder.taskSubject.setText(task.getSubject());

        if (TextUtils.isEmpty(task.getLockedUser())) {
            holder.taskStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_lock_open));
        } else {
            holder.taskStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_lock_closed));
        }

        holder.taskStatus.refreshDrawableState();
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    /**
     * Display a new list of tasks.
     *
     * @param newTasks
     */
    public void updateTasks(final List<ICMTask> newTasks) {
        mTasks.clear();

        mTasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    /**
     * Get the {@link ICMTask} at the given position.
     *
     * @param position
     * @return the {@link ICMTask} at the given position or {@code null} if there are no tasks
     * or the {@param position} is out of bounds.
     */
    public ICMTask getItem(int position) {
        ICMTask task = null;
        if (!mTasks.isEmpty() && position <= mTasks.size()) {
            task = mTasks.get(position);
        }

        return task;
    }

    public class TaskHolder extends ClickkableRecyclerAdapter.ViewHolder {

        @Bind(R.id.task_status)
        ImageView taskStatus;

        @Bind(R.id.task_name)
        TextView taskName;

        @Bind(R.id.task_subject)
        TextView taskSubject;

        public TaskHolder(Context context, View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}
