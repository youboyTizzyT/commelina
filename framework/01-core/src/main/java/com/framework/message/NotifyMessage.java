package com.framework.message;

import com.google.protobuf.Internal;

import java.io.Serializable;

/**
 * Created by @panyao on 2017/8/15.
 */
public class NotifyMessage implements Serializable {

    private final Internal.EnumLite opcode;
    private final long userId;
    private final MessageBus message;

    private NotifyMessage(Internal.EnumLite opcode, long userId, MessageBus messageBus) {
        this.opcode = opcode;
        this.userId = userId;
        this.message = messageBus;
    }

    public static NotifyMessage newMessage(Internal.EnumLite opcode, long userId, MessageBus messageBus) {
        return new NotifyMessage(opcode, userId, messageBus);
    }

    public long getUserId() {
        return userId;
    }

    public MessageBus getMessage() {
        return message;
    }

    public Internal.EnumLite getOpcode() {
        return opcode;
    }
}
