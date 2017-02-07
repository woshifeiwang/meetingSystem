package com.hna.meetingsystem.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hna.meetingsystem.R;
import com.hna.meetingsystem.base.utils.DateFormat;
import com.hna.meetingsystem.base.utils.DisplayUtil;
import com.hna.meetingsystem.model.MeetingInfoS;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FancyCoverFlowSampleAdapter extends FancyCoverFlowAdapter {
    //    public enum Type{
//
//    }
    public FancyCoverFlowSampleAdapter(List<MeetingInfoS> list) {
        if(list!=null)
        {
            for (int i = list.size() - 1; i >= 0; i--) {
                this.list.add(list.get(i));
            }
        }
    }


    public List<MeetingInfoS> list = new ArrayList<MeetingInfoS>();
    public MeetingInfoS curMeeting;
    public int curMeetingPosition;

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public MeetingInfoS getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getCoverFlowItem(int i, View reuseableView, ViewGroup viewGroup) {
        View view = null;
        RelativeLayout relativeLayout = null;
        if (reuseableView != null) {
            relativeLayout = (RelativeLayout) reuseableView;
        } else {
            relativeLayout = new RelativeLayout(viewGroup.getContext());
            if (DisplayUtil.isPad(viewGroup.getContext())) {
                relativeLayout.setLayoutParams(new FancyCoverFlow.LayoutParams(DisplayUtil.dip2px(relativeLayout.getContext(), 400), DisplayUtil.dip2px(relativeLayout.getContext(), 240)));
            } else {
                relativeLayout.setLayoutParams(new FancyCoverFlow.LayoutParams(DisplayUtil.dip2px(relativeLayout.getContext(), 280), DisplayUtil.dip2px(relativeLayout.getContext(), 160)));
            }
        }

        MeetingInfoS meetingInfoS = list.get(i);
        SimpleDateFormat sdf = DateFormat.getDateFormat();
        long sysTime = System.currentTimeMillis();
        //                    CharSequence sysTimeStr = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", sysTime);
        Date startTime = null;
        Date endTime = null;
        try {
            startTime = sdf.parse(meetingInfoS.startTime);
            endTime = sdf.parse(meetingInfoS.endTime);
            if (sysTime > startTime.getTime()) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_end, null);

            }
            if (sysTime >= startTime.getTime() && sysTime <= endTime.getTime() && meetingInfoS.status.equals("0")) {
                curMeeting = meetingInfoS;
                curMeetingPosition = i;
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_meeting, null);
                ImageView progress = (ImageView) view.findViewById(R.id.progess);
                ImageView plane = (ImageView) view.findViewById(R.id.plane);
            /*    FancyCoverFlow fancyCoverFlow = (FancyCoverFlow) view.getParent();
                fancyCoverFlow.setSelection(i);*/
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                float b = (float) (sysTime - startTime.getTime()) / (float) (endTime.getTime() - startTime.getTime());
                if (DisplayUtil.isPad(viewGroup.getContext())) {
                    layoutParams.setMargins(DisplayUtil.dip2px(viewGroup.getContext(), (int) (310 * b)), 0, 0, 0);
                } else {
                    layoutParams.setMargins(DisplayUtil.dip2px(viewGroup.getContext(), (int) (220 * b)), 0, 0, 0);
                }

                layoutParams.addRule(RelativeLayout.ABOVE, R.id.progess_bg);
                plane.setLayoutParams(layoutParams);

                RelativeLayout.LayoutParams layoutParams1;
                if (DisplayUtil.isPad(viewGroup.getContext())) {
                    layoutParams1 = new RelativeLayout.LayoutParams(DisplayUtil.dip2px(viewGroup.getContext(), (int) (340 * b)), DisplayUtil.dip2px(viewGroup.getContext(), 10));

                } else {
                    layoutParams1 = new RelativeLayout.LayoutParams(DisplayUtil.dip2px(viewGroup.getContext(), (int) (250 * b)), DisplayUtil.dip2px(viewGroup.getContext(), 10));

                }
                progress.setLayoutParams(layoutParams1);
            }
            if (sysTime < startTime.getTime()) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_card_wait, null);
            }

            TextView name = (TextView) view.findViewById(R.id.name);

            MeetingInfoS meeting = list.get(i);

            String title = (meeting.userName.length() > 2 ? meeting.userName.substring(0, 2) : meeting.userName)
                    + "·" + meeting.subject;
            if (title.length() > 9) {
                name.setText(title.substring(0, 6) + "..");
            } else {
                name.setText(title);
            }

            TextView time = (TextView) view.findViewById(R.id.time);
            TextView date = (TextView) view.findViewById(R.id.date);
            time.setText(list.get(i).startTime.substring(list.get(i).startTime.length() - 8,list.get(i).startTime.length() - 3)+ "--" + list.get(i).endTime.substring(list.get(i).endTime.length() - 8,list.get(i).endTime.length()-3));
            date.setText(list.get(i).startTime.substring(0,list.get(i).startTime.length()-9));
            relativeLayout.addView(view);
        } catch (ParseException e) {
            e.printStackTrace();
        }

//        view.setLayoutParams(new FancyCoverFlow.LayoutParams(DisplayUtil.dip2px(view.getContext(), 400), DisplayUtil.dip2px(view.getContext(),220)));

        return relativeLayout;

    }
}