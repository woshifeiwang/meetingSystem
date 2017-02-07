package com.hna.meetingsystem.base.utils;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by Jie on 2016-06-01.
 */
public class CustomTextWatch implements TextWatcher {//观察文字内容，改变自定义右边图标显示与否

    private boolean isShowIcon = true;
    private EditText editText;
    private Drawable iconDrawable;

    private Callback callback;

    public CustomTextWatch(EditText editText, Drawable iconDrawable) {
        this.editText = editText;
        this.iconDrawable = iconDrawable;
        editText.addTextChangedListener(this);
    }

    public CustomTextWatch setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int totalCount = start + count;
        if (callback != null) {
            callback.worldChang(totalCount);
        }
        if (totalCount > 0 && !isShowIcon) {
            isShowIcon = true;
            changEditTextIcon();
        } else if (totalCount == 0 && isShowIcon) {
            isShowIcon = false;
            changEditTextIcon();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changEditTextIcon() {
        if (editText == null)
            return;
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, isShowIcon ? iconDrawable : null, null);
    }


    public interface Callback {
        void worldChang(int total);
    }
}
