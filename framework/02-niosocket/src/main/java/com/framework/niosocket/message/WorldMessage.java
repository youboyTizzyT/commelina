package com.framework.niosocket.message;

import com.framework.core.MessageBody;
import com.google.common.base.Preconditions;

/**
 * @author @panyao
 * @date 2017/8/15
 */
public class WorldMessage {

    private final int opcode;
    private final MessageBody message;

    private WorldMessage(int opcode, MessageBody messageBody) {
        this.opcode = opcode;
        this.message = messageBody;
    }

    public static WorldMessage newMessage(int opcode, MessageBody messageBody) {
        Preconditions.checkNotNull(messageBody);
        return new WorldMessage(opcode, messageBody);
    }

    public MessageBody getMessage() {
        return message;
    }

    public int getOpcode() {
        return opcode;
    }

}