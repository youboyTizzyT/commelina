package com.game.matching.service;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.framework.akka.router.cluster.nodes.ClusterChildNodeSystem;
import com.framework.core.DefaultMessageProvider;
import com.game.matching.proto.OPCODE;
import scala.concurrent.duration.Duration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 同步匹配状态dao到客户端
 *
 * @author @panyao
 * @date 2017/8/14
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
        log.info("Broadcast match status people: " + notifyMatchStatus.userIds.size());
        // 把消息发回到主 actor 由，主 actor 发送广播消息到 gate way
        ClusterChildNodeSystem.INSTANCE.broadcast(
                OPCODE.NOTIFY_MATCH_SUCCESS_VALUE,
                notifyMatchStatus.userIds,
                DefaultMessageProvider.produceMessageForKV("matchUserCount", notifyMatchStatus.userIds.size()));

        // 延迟 关闭此 actor
        getContext().getSystem().scheduler()
                .scheduleOnce(
                        Duration.create(3, TimeUnit.SECONDS),
                        () -> getContext().stop(getSelf()),
                        getContext().getSystem().dispatcher()
                );
    }

    static final class NOTIFY_MATCH_STATUS {
        List<Long> userIds;

        NOTIFY_MATCH_STATUS(List<Long> userIds) {
            this.userIds = userIds;
        }
    }

    static Props props() {
        return Props.create(MatchingStatus.class);
    }

}