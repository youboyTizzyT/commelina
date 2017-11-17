package com.commelina.math24.play.match;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.commelina.akka.dispatching.nodes.AbstractBackendActor;
import com.commelina.akka.dispatching.nodes.ClusterBackendActorSystem;
import com.commelina.akka.dispatching.proto.ApiRequest;
import com.commelina.akka.dispatching.proto.MemberOfflineEvent;
import com.commelina.math24.play.match.mode.GlobalMatch;
import com.commelina.math24.play.match.proto.CancelMatch;
import com.commelina.math24.play.match.proto.JoinMatch;
import com.commelina.math24.play.match.proto.MATCH_METHODS;
import com.commelina.math24.play.match.proto.MATCH_MODE;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;

import java.util.Map;

/**
 * @author @panyao
 * @date 2017/9/26
 */
public class MatchPortal extends AbstractBackendActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    /**
     * 用户访问的最后一个 mode
     */
    private final Map<Long, MATCH_MODE> userLastAccessMode = Maps.newHashMap();

    /**
     * 10 人局游戏
     */
    private ActorRef globalMatchActorRef;

    public MatchPortal(ClusterBackendActorSystem backendActorSystem) {
        super(backendActorSystem);
    }

    @Override
    public void preStart() {
        super.preStart();
        //
        getContext().getSystem().actorOf(GlobalMatch.props(10, backendActorSystem()));
    }

    @Override
    public void onOffline(MemberOfflineEvent offlineEvent) {
        MATCH_MODE mode = userLastAccessMode.get(offlineEvent.getLogoutUserId());
        if (mode != null) {
            selectMatchModeActorRef(mode).tell(null, null);
            // 移除用户最后的访问记录
            userLastAccessMode.remove(offlineEvent.getLogoutUserId());
        } else {
            // 没在匹配里，就在临时房间里
//            roomManger.tell(null, null);
        }
        // 用户下线，取消匹配
        // match.forward(new Matching.CancelMatch(logoutUserId, () -> 0), getContext());
    }

    @Override
    public void onRequest(ApiRequest request) {
        switch (request.getOpcode()) {
            // 加入匹配
            case MATCH_METHODS.JOIN_MATCH_QUENE_VALUE:
                switchActor(request).forward(
                        JoinMatch.newBuilder()
                                .setUserId(request.getLoginUserId())
                                .build(), getContext());
                break;
            // 取消匹配
            case MATCH_METHODS.EXIT_MATCH_QUENE_VALUE:
                switchActor(request).forward(CancelMatch.newBuilder()
                        .setUserId(request.getLoginUserId())
                        .build(), getContext());
                break;
            // 加入临时房间
            case MATCH_METHODS.JOIN_TEMPORARY_ROOM_VALUE:
//                roomManger.forward(request, getContext());
                break;
            default:
                unhandled(request);
        }
    }

    private ActorRef switchActor(ApiRequest request) {
        ByteString matchMode = request.getArgs(0);
        if (matchMode == null || matchMode.isEmpty()) {
            throw new IllegalArgumentException("Arg : matchMode must be input.");
        }

        return selectMatchModeActorRef(MATCH_MODE.forNumber(Integer.valueOf(matchMode.toStringUtf8())));
    }

    private ActorRef selectMatchModeActorRef(MATCH_MODE mode) {
        switch (mode) {
            case FAST:
                return globalMatchActorRef;
            default:
                throw new IllegalArgumentException();
        }
    }

}