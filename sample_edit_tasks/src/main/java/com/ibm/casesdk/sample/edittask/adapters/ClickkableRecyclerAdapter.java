package com.ibm.casesdk.sample.edittask.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Base class for creating RecyclerView adapters that can handle item clicks.
 * Created by stelian on 27/10/2015.
 */
public abstract class ClickkableRecyclerAdapter<VH extends ClickkableRecyclerAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {

    private int mSelectedItemPosition = -1;
    private RecyclerView mRecyclerView;
    private RecyclerViewClickListener mClickListener;

    public void setRecyclerViewClickListener(@NonNull RecyclerViewClickListener listener) {
        mClickListener = listener;
    }

    public interface RecyclerViewClickListener {
        void onRecyclerViewItemClicked(int position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);

            // Handle item click and set the selection
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Redraw the old selection and the new
                    notifyItemChanged(mSelectedItemPosition);
                    mSelectedItemPosition = mRecyclerView.getChildAdapterPosition(v);
                    notifyItemChanged(mSelectedItemPosition);

                    if (mClickListener != null) {
                        mClickListener.onRecyclerViewItemClicked(mSelectedItemPosition);
                    }
                }
            });
        }
    }
}
