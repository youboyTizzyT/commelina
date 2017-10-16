package com.framework.akka_router;

import com.framework.akka_router.cluster.AkkaMultiWorkerSystem;
import com.framework.akka_router.cluster.AkkaMultiWorkerSystemContext;
import com.framework.niosocket.ReplyUtils;
import com.framework.niosocket.RequestHandler;
import com.framework.niosocket.proto.SERVER_CODE;
import com.framework.niosocket.proto.SocketASK;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by @panyao on 2017/9/25.
 */
public abstract class DefaultClusterActorRequestHandler extends DefaultLocalActorRequestHandler{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void afterHook(ApiRequest request, ChannelHandlerContext ctx) {
        AkkaMultiWorkerSystem clusterSystem = AkkaMultiWorkerSystemContext.INSTANCE.getContext(getRouterId());
        if (clusterSystem == null) {
            ReplyUtils.reply(ctx, SERVER_CODE.RPC_API_NOT_FOUND, getRouterId(), request.getOpcode());
            return;
        }

        ReplyUtils.reply(ctx, getRouterId(), request.getOpcode(), clusterSystem.askRouterClusterNode(request));
    }

}