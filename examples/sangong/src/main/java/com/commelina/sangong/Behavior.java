package com.commelina.sangong;

import com.commelina.sangong.context.Room;

/**
 * Created by panyao on 2017/12/2.
 */
public interface Behavior extends MemberEvent{

    // 返回延迟时间
    default int onStart(Room room){

    }

    default void onTimeoutExceute(Room room){

    }

}
