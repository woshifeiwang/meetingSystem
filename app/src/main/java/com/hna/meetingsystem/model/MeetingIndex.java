package com.hna.meetingsystem.model;

/**
 * Created by pactera on 2017/1/20.
 */

import java.util.List;
import java.util.Set;

public class MeetingIndex {

    private List<MeetingInfoS> meetings;

    private Set<Integer> registed;

    public List<MeetingInfoS> getMeetings() {
        return meetings;
    }
    public Set<Integer> getRegisted()
    {
        return registed;
    }
    @Override
    public String toString() {
        return "MeetingIndex [meetings=" + meetings + ", registed=" + registed + "]";
    }

}

