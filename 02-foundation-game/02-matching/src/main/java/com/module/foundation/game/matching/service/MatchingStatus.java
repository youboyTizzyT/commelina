package com.module.foundation.game.matching.service;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.module.foundation.game.matching.MessageProvider;
import com.module.foundation.game.matching.constants.OPCODE_CONSTANTS;
import com.nexus.maven.core.message.BroadcastMessage;

/**
 * Created by @panyao on 2017/8/14.
 * <p>
 * 同步匹配状态dao到客户端
 */
public class MatchingStatus extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(NOTIFY_MATCH_STATUS.class, this::notifyMatchStatus)
                .matchAny(o -> log.info("MatchingStatus received unknown message."))
                .build();
    }

    private void notifyMatchStatus(NOTIFY_MATCH_STATUS notifyMatchStatus) {
        BroadcastMessage broadcast = BroadcastMessage.newBroadcast(OPCODE_CONSTANTS.NOTIFY_MATCH_SUCCESS, notifyMatchStatus.userIds, MessageProvider.produceMessageForKV("matchUserCount", notifyMatchStatus.userIds.length));
        log.info("Broadcast match status people: " + notifyMatchStatus.userIds.length);
        // 把消息发回到主 actor 由，主 actor 发送广播消息到 gate way
        getSender().tell(broadcast, getSelf());
        getContext().stop(getSelf());
    }

    static final class NOTIFY_MATCH_STATUS {
        long[] userIds;

        NOTIFY_MATCH_STATUS(long[] userIds) {
            this.userIds = userIds;
        }
    }

    static Props props() {
        return Props.create(MatchingStatus.class);
    }

}