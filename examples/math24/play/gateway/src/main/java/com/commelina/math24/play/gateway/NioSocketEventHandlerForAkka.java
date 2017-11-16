package com.commelina.math24.play.gateway;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.commelina.akka.dispatching.ActorSystemCreator;
import com.commelina.akka.dispatching.proto.ActorResponse;
import com.commelina.akka.dispatching.proto.ApiRequest;
import com.commelina.math24.common.proto.DOMAIN;
import com.commelina.math24.play.gateway.proto.GATEWAY_METHODS;
import com.commelina.niosocket.ReplyUtils;
import com.commelina.niosocket.SocketEventHandler;
import com.commelina.niosocket.proto.SERVER_CODE;
import com.commelina.niosocket.proto.SocketASK;
import com.commelina.niosocket.proto.SocketMessage;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

/**
 * @author panyao
 * @date 2017/11/15
 */
@Component
public class NioSocketEventHandlerForAkka implements SocketEventHandler {

    private static final Timeout DEFAULT_TIMEOUT = new Timeout(Duration.create(5, TimeUnit.SECONDS));
    private Dispatching dispatching = new Dispatching();

    @Override
    public void onRequest(ChannelHandlerContext ctx, SocketASK ask) {
        final ApiRequest request = ApiRequest.newBuilder()
                .setOpcode(ask.getOpcode())
                .setVersion(ask.getVersion())
                .addAllArgs(ask.getArgsList())
                .build();

        switch (ask.getForward()) {
            case DOMAIN.GATEWAY_VALUE:
                dispatching.requestGateway(ctx, request);
                break;
            default:
                throw new InvalidParameterException("Undefined type" + ask.getForward());
        }

    }

    @Override
    public void onOnline(ChannelHandlerContext ctx) {

    }

    @Override
    public void onOffline(ChannelHandlerContext ctx, long logoutUserId) {

    }

    @Override
    public void onException(ChannelHandlerContext ctx, Throwable cause) {

    }

    private static class Dispatching {
        final ActorSystem gateway;
        final ActorSystem match;
        final ActorRef matchFrontend;
        final ActorSystem room;
        final ActorRef roomFrontend;

        {
            gateway = ActorSystemCreator.create("gateway", "gateway");

            ActorSystemCreator.ClusterSystem system =
                    ActorSystemCreator.createAsCluster("ClusterMatchingSystem", "cluster-requestGateway-match");
            match = system.getActorSystem();
            matchFrontend = system.getFrontend();

            ActorSystemCreator.ClusterSystem roomSystem =
                    ActorSystemCreator.createAsCluster("ClusterRoomSystem", "cluster-requestGateway-room");

            room = roomSystem.getActorSystem();
            roomFrontend = roomSystem.getFrontend();
        }

        public void requestGateway(ChannelHandlerContext ctx, ApiRequest request) {
            switch (request.getOpcode()) {
                case GATEWAY_METHODS.PASSPORT_CONNECT_VALUE:

                    break;
                default:
            }

//            ReplyUtils.reply(ctx, ask.getForward(), ask.getOpcode(), body)
        }

        public void requestMatch(ChannelHandlerContext ctx, ApiRequest request) {
            ActorResponse response = (ActorResponse) PatternsCS.ask(matchFrontend, request, DEFAULT_TIMEOUT)
                    .toCompletableFuture()
                    .join();

            SocketMessage.newBuilder()
                    .setCode(SERVER_CODE.RESONSE_CODE)
                    .setDomain(DOMAIN.MATCHING_VALUE)
                    .setOpcode(request.getOpcode())
                    .setMsg(response.getMessage())
                    .build();

            ReplyUtils.reply(ctx, ask.getForward(), ask.getOpcode(), body)
        }

        private void passortValid(ChannelHandlerContext ctx, ApiRequest request) {

//            if (tokenArg == null) {
//                // token 转换错误
////                ReplyUtils.reply();
//                DefaultMessageProvider.produceMessage(BusinessMessage.error(ERROR_CODE.TOKEN_PARSE_ERROR))
//                return;
//            }
//
//            //        String token = tokenArg.getAsString();
//            //        String parseToken = new String(BaseEncoding.base64Url().decode(token));
//            //        List<String> tokenChars = Splitter.on('|').splitToList(parseToken);
//            //        ContextAdapter.userLogin(context.getRawContext().channel().id(), Long.valueOf(tokenChars.get(0)));
//            //        ContextAdapter.userLogin(context.channel().id(), tokenArg.getAsLong());
//
//            long userId = Long.valueOf(tokenArg.toStringUtf8());
////            getLogger().info("userId:{}, 登录成功", userId);
        }

    }

}
