package com.framework.akka_router;

import com.framework.core.MessageBody;

/**
 * Created by @panyao on 2017/9/30.
 */
public final class LoginUserEntity {

    private final long userId;
    private final MessageBody messageBody;

    public LoginUserEntity(long userId, MessageBody messageBody) {
        this.userId = userId;
        this.messageBody = messageBody;
    }

    public long getUserId() {
        return userId;
    }

    public MessageBody getMessageBody() {
        return messageBody;
    }
}
