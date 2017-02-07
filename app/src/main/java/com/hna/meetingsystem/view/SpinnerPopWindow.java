package com.hna.meetingsystem.view;

import java.util.List;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.hna.meetingsystem.R;
import com.hna.meetingsystem.city.adapter.SpinnerAdapter;

/**
 * 自定义SpinerPopWindow类
 * @author gao_chun
 *
 */
public class SpinnerPopWindow extends PopupWindow implements OnItemClickListener{

    private Context mContext;
    private ListView mListView;
    private SpinnerAdapter mAdapter;
    private SpinnerAdapter.IOnItemSelectListener mItemSelectListener;


    public SpinnerPopWindow(Context context)
    {
        super(context);
        mContext = context;
        init();
    }


    public void setItemListener(SpinnerAdapter.IOnItemSelectListener listener){
        mItemSelectListener = listener;
    }

    public void setAdatper(SpinnerAdapter adapter){
        mAdapter = adapter;
        mListView.setAdapter(mAdapter);
    }


    private void init()
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.spiner_window_layout, null);
        setContentView(view);
        setWidth(LayoutParams.WRAP_CONTENT);
        setHeight(LayoutParams.WRAP_CONTENT);

        setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00);
        setBackgroundDrawable(dw);


        mListView = (ListView) view.findViewById(R.id.lv_spinner_listview);
        mListView.setOnItemClickListener(this);
    }


    public void refreshData(List<String> list, int selIndex)
    {
        if (list != null && selIndex  != -1)
        {
            if (mAdapter != null){
                mAdapter.refreshData(list, selIndex);
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
        dismiss();
        if (mItemSelectListener != null){
            mItemSelectListener.onItemClick(pos);
        }
    }

}

