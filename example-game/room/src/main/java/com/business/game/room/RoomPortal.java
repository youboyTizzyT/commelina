package com.business.game.room;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.business.game.message.matching_room.proto.MATCHING_ROOM_METHODS;
import com.business.game.room.entity.PlayerStatus;
import com.business.game.room.event.PlayerStatusEvent;
import com.business.game.room.service.RoomMain;
import com.game.room.proto.ERROR_CODE;
import com.github.freedompy.commelina.akkadispatching.cluster.nodes.BackendActor;
import com.github.freedompy.commelina.akkadispatching.proto.ApiRequest;
import com.github.freedompy.commelina.akkadispatching.proto.ApiRequestForward;
import com.github.freedompy.commelina.core.BusinessMessage;
import com.github.freedompy.commelina.core.DefaultMessageProvider;
import com.github.freedompy.commelina.core.MessageBody;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author @panyao
 * @date 2017/9/26
 */
public class RoomPortal extends BackendActor {

    /**
     * roomId -> RoomMainActorRef
     */
    private final BiMap<Long, ActorRef> roomIdToRoomContextActor = HashBiMap.create(128);

    /**
     * userId -> roomId
     */
    private final Map<Long, Long> usersToRoomId = Maps.newHashMap();

    /**
     * 当前 子曾的房间id
     */
    private long currentRoomId = 0;

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private static final MessageBody ROOM_NOT_FOUND =
            DefaultMessageProvider.produceMessage(BusinessMessage.error(ERROR_CODE.ROOM_NOT_FOUND));

    @Override
    public void onOffline(long logoutUserId) {
        // 用户下线，标记为下线
        sendPlayerStatus(new PlayerStatusEvent(logoutUserId, PlayerStatus.Offline));
    }

    @Override
    public void onOnline(long logoutUserId) {
        // 用户上线,标记为重新上线
        sendPlayerStatus(new PlayerStatusEvent(logoutUserId, PlayerStatus.Online));
    }

    @Override
    public void onRequest(ApiRequest request) {
        ByteString roomIdArg = request.getArgs(0);
        if (roomIdArg == null) {
            response(ROOM_NOT_FOUND);
            return;
        }

        final long roomId = Long.valueOf(roomIdArg.toStringUtf8());
        if (roomId <= 0) {
            response(ROOM_NOT_FOUND);
            return;
        }

        ActorRef roomContext = roomIdToRoomContextActor.get(roomId);
        if (roomContext == null) {
            response(ROOM_NOT_FOUND);
            logger.info("Api request room id {} not instance RoomMain.", roomId);
            return;
        }

        // 移除第一个元素
        request.getArgsList().remove(0);

        roomContext.forward(request, getContext());
    }

    @Override
    public void onForward(ApiRequestForward forward) {
        switch (forward.getOpcode()) {
            case MATCHING_ROOM_METHODS.CREATE_ROOM_VALUE:
                createRoom(forward);
                break;
            default:

        }
    }

    private void createRoom(ApiRequestForward forward) {
        List<Long> userIds = forward.getArgsList()
                .stream()
                .map(v -> Long.valueOf(v.toStringUtf8()))
                .collect(Collectors.toList());

        // fixme 加载用户信息

        final long newRoomId = currentRoomId++;
        final ActorRef roomContext = getContext().actorOf(RoomMain.props(newRoomId, null), "roomContext");
        roomIdToRoomContextActor.put(newRoomId, roomContext);

        // 把当前用户加入 room Id 列表上
        userIds.forEach(v -> usersToRoomId.put(v, newRoomId));
    }

    private void sendPlayerStatus(PlayerStatusEvent event) {
        Long roomId = usersToRoomId.get(event.getUserId());

        if (roomId == null || roomId <= 0) {
            logger.info("User id {} not found room id.", event.getUserId());
            return;
        }

        ActorRef roomContext = roomIdToRoomContextActor.get(roomId);
        if (roomContext == null) {
            logger.info("Room id {} not instance RoomMain.", roomId);
            return;
        }

        roomContext.forward(event, getContext());
    }

}