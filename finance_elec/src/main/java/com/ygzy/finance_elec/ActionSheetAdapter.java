package com.ygzy.finance_elec;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author : taomf
 * Date    : 2025/1/15 015/11:00
 * Desc    :
 */
public class ActionSheetAdapter extends RecyclerView.Adapter<ActionSheetAdapter.SheetHolder>{

    private Context mContext;
    private JSONArray mItemList;
    private OnSheetListener mListener;
    public ActionSheetAdapter(Context context, JSONArray itemList,OnSheetListener listener){
        this.mContext = context;
        this.mItemList = itemList;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public SheetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View bottomView = LayoutInflater.from(mContext).inflate(R.layout.menu_item, parent, false);

        return new SheetHolder(bottomView);
    }

    @Override
    public void onBindViewHolder(@NonNull SheetHolder holder, int position) {
        String itemString = (String) mItemList.get(position);
        holder.text.setText(itemString);

        holder.text.setOnClickListener(v -> {
            if (mListener != null){
                mListener.onSheetItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mItemList == null){
            return 0;
        } else {
            return mItemList.toArray().length;
        }
    }

    public static class SheetHolder extends RecyclerView.ViewHolder{
        TextView text;

        public SheetHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_menu_text);
        }
    }

    public interface OnSheetListener {
        void onSheetItemClick(int position);
    }
}
