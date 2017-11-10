package org.commelina.match24.play.gateway.service;

import akka.japi.pf.ReceiveBuilder;
import org.commelina.akka.dispatching.ActorServiceHandler;
import org.commelina.akka.dispatching.LocalServiceHandler;
import org.commelina.akka.dispatching.local.AbstractLocalServiceActor;
import org.commelina.match24.play.gateway.proto.GATEWAY_METHODS;
import com.google.protobuf.Internal;

/**
 * @author panyao
 * @date 2017/8/30
 * <p>
 * 记录用户状态，是匹配中，还是游戏中
 */
@ActorServiceHandler
public class RoomStatusService implements LocalServiceHandler {

    @Override
    public Internal.EnumLite getRouterId() {
        return GATEWAY_METHODS.ROOM_STATUS;
    }

    @Override
    public Class<? extends AbstractLocalServiceActor> getPropsClass() {
        return RoomStatusActor.class;
    }

    private static class RoomStatusActor extends AbstractLocalServiceActor {

        public RoomStatusActor(Internal.EnumLite routerId) {
            super(routerId);
        }

        @Override
        protected ReceiveBuilder addLocalMatch(ReceiveBuilder builder) {
            return builder;
        }

    }

}