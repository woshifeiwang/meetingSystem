package com.hna.meetingsystem.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * Created by pactera on 2016/12/29.
 */

public class MarqueeText extends TextView {
    public MarqueeText(Context con) {
        super(con);
    }
    CharSequence mtext;
    public MarqueeText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MarqueeText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if(mtext==null) {
            mtext = text;
            super.setText(text,type);
        }
        else if(mtext.equals(text)){
            return;
        }else {
            super.setText(text, type);
        }
    }
}
