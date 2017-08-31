package com.foundation.game_matching.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.collect.Lists;
import com.google.protobuf.Internal;
import com.foundation.game_matching.MessageProvider;
import com.framework.core_message.ResponseMessage;

import java.util.List;

/**
 * Created by @panyao on 2017/8/10.
 */
public class Matching extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final List<Long> matchList = Lists.newArrayList();
    private static final int MATCH_SUCCESS_PEOPLE = 100;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JOIN_MATCH.class, this::joinMatch)
                .match(REMOVE_MATCH.class, this::removeMatch)
                .match(CANCEL_MATCH.class, this::cancelMatch)
                .match(MatchingRedirect.CREATE_ROOM_FAILED.class, this::createMatchFailed)
                .matchAny(o -> log.info("Matching received unknown message" + o))
                .build();
    }

    private void joinMatch(JOIN_MATCH joinMatch) {
        final long userId = joinMatch.userId;
        if (matchList.contains(userId)) {
            log.info("userId exists in queue " + userId + ", ignored.");
            // 回复 MatchingRouter 的 调用者成功
            getSender().tell(ResponseMessage.newMessage(joinMatch.apiOpcode, MessageProvider.produceMessage()), getSelf());
            return;
        }
        log.info("add queue userId " + userId);
        matchList.add(userId);

        // 回复 MatchingRouter 的 调用者成功
        getSender().tell(ResponseMessage.newMessage(joinMatch.apiOpcode, MessageProvider.produceMessage()), getSelf());

        if (matchList.size() >= MATCH_SUCCESS_PEOPLE) {
            final long[] userIds = new long[MATCH_SUCCESS_PEOPLE];
//            int lastPoint = 0;
//            nextIterator:
//            while (userIds.length != MATCH_SUCCESS_PEOPLE && matchList.iterator().hasNext()) {
//                final Long findUserId = matchList.iterator().next();
//                for (int i = 0; i < userIds.length; i++) {
//                    if (userIds[i] == findUserId) {
//                        matchList.remove(findUserId);
//                        continue nextIterator;
//                    }
//                }
//                matchList.remove(findUserId);
//                userIds[lastPoint++] = findUserId;
//            }

            for (int i = 0; i < MATCH_SUCCESS_PEOPLE; i++) {
                userIds[i] = matchList.remove(i);
            }

            final ActorRef matchingRedirect = getContext().actorOf(MatchingRedirect.props());
            matchingRedirect.forward(new MatchingRedirect.CREATE_ROOM(userIds), getContext());
        } else {
            long[] userIds = new long[matchList.size()];
            for (int i = 0; i < matchList.size(); i++) {
                userIds[i] = matchList.get(i);
            }
            final ActorRef notifyMatchStatus = getContext().actorOf(MatchingStatus.props());
            notifyMatchStatus.forward(new MatchingStatus.NOTIFY_MATCH_STATUS(userIds), getContext());
        }
    }

    private void cancelMatch(CANCEL_MATCH cancelMatch) {
        long userId = cancelMatch.userId;

        boolean rs = matchList.remove(userId);

        log.info("cancel queue userId " + userId + ", result " + rs);

        // 回复 MatchingRouter 的 调用者成功
        getSender().tell(ResponseMessage.newMessage(cancelMatch.apiOpcode, MessageProvider.produceMessage()), getSelf());
    }

    private void removeMatch(REMOVE_MATCH removeMatch) {
        long userId = removeMatch.userId;

        boolean rs = matchList.remove(userId);

        log.info("remove queue userId " + userId + ", result " + rs);
    }

    private void createMatchFailed(MatchingRedirect.CREATE_ROOM_FAILED failed) {
        // 这里最好能够通知客户端匹配失败
        // fixme 待测试
        // 记录匹配失败
    }

    // http://doc.akka.io/docs/akka/current/java/guide/tutorial_3.html
    public static final class JOIN_MATCH {
        long userId;
        Internal.EnumLite apiOpcode;

        public JOIN_MATCH(long userId, Internal.EnumLite apiOpcode) {
            this.userId = userId;
            this.apiOpcode = apiOpcode;
        }

    }

    public static final class CANCEL_MATCH {
        long userId;
        Internal.EnumLite apiOpcode;

        public CANCEL_MATCH(long userId, Internal.EnumLite apiOpcode) {
            this.userId = userId;
            this.apiOpcode = apiOpcode;
        }
    }

    public static final class REMOVE_MATCH {
        long userId;

        public REMOVE_MATCH(long userId) {
            this.userId = userId;
        }
    }

    public static Props props() {
        return Props.create(Matching.class);
    }

    //    @Override
//    public void preStart() throws Exception {
//        for (ActorRef each : getContext().getChildren()) {
//            getContext().unwatch(each);
//            getContext().stop(each);
//        }
//        super.preStart();
//    }
//
//    @Override
//    public void postStop() throws Exception {
//        for (ActorRef each : getContext().getChildren()) {
//            getContext().unwatch(each);
//            getContext().stop(each);
//        }
//        super.postStop();
//    }

}
