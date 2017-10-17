package com.game.robot.interfaces;

import com.framework.niosocket.proto.SocketMessage;
/**
 *
 * @author @panyao
 * @date 2017/9/11
 */
public interface ReadEvent extends Identify {

    EventResult read(MemberEventLoop eventLoop, SocketMessage msg);

    // 如果不再使用，则返回 remove 则会不会再在事件循环内了
    enum EventResult {
        REMOVE, UN_REMOVE, ADD_HISTORY
    }

}
