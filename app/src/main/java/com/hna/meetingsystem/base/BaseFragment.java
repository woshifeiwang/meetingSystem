package com.hna.meetingsystem.base;

import android.support.v4.app.Fragment;

/**
 * Created by Jie on 2016-04-18.
 */
public class BaseFragment extends Fragment implements BackHandleInterface {
    @Override
    public boolean onBackPress() {
        return false;
    }
}
