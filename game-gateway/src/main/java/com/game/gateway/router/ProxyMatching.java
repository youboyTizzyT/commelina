package com.game.gateway.router;

import com.framework.akka.router.DefaultClusterActorRequestHandler;
import com.framework.akka.router.proto.ApiRequest;
import com.framework.core.BusinessMessage;
import com.framework.core.DefaultMessageProvider;
import com.framework.core.MessageBody;
import com.framework.niosocket.ContextAdapter;
import com.framework.niosocket.NioSocketRouter;
import com.framework.niosocket.ReplyUtils;
import com.framework.niosocket.proto.SocketASK;
import com.game.gateway.proto.DOMAIN;
import com.game.gateway.proto.ERROR_CODE;
import com.google.protobuf.Internal;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author @panyao
 * @date 2017/9/22
 */
@NioSocketRouter(forward = DOMAIN.MATCHING_VALUE)
public class ProxyMatching extends DefaultClusterActorRequestHandler {

    private final MessageBody messageBody = DefaultMessageProvider.produceMessage(BusinessMessage.error(ERROR_CODE.MATCHING_API_UNAUTHORIZED));

    @Override
    protected boolean beforeHook(SocketASK ask, ApiRequest.Builder newRequestBuilder, ChannelHandlerContext ctx) {
        final long userId = ContextAdapter.getLoginUserId(ctx.channel().id());
        if (userId <= 0) {
            ReplyUtils.reply(ctx, DOMAIN.GATEWAY, ask.getOpcode(), messageBody);
            return false;
        }

        newRequestBuilder.setLoginUserId(userId);

        return true;
    }

    @Override
    public Internal.EnumLite getRouterId() {
        return DOMAIN.MATCHING;
    }

}
