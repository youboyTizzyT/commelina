package com.framework.akka_router.cluster.node;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.framework.akka_router.ActorResponse;
import com.framework.core.MessageBody;
import com.google.protobuf.ByteString;

import java.io.IOException;

/**
 * Created by @panyao on 2017/9/25.
 */
public abstract class AbstractServiceActor extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), getClass());

    protected final boolean response(MessageBody message) {
        byte[] bytes;
        try {
            bytes = message.getBytes();
        } catch (IOException e) {
            logger.error("{}", e);
            return false;
        }
        // 使用 此方法，必须是有 actor.forward 从定向过来的 ask
        // 回复到 on request 的发送者那里
        getSender().tell(ActorResponse.newBuilder()
                        .setMessage(ByteString.copyFrom(bytes))
                        .build()
                , getSelf());
        return true;
    }

    protected final void response(ActorResponse response) {
        // 使用 此方法，必须是有 actor.forward 从定向过来的 ask
        // 回复到 on forward 的发送者那里
        // FIXME: 2017/10/16 这里待定
        getSender().tell(response, getSelf());
    }

    protected LoggingAdapter getLogger() {
        return logger;
    }

}