package com.game.gateway.router_v3;

import com.framework.message.ApiRequest;
import com.framework.niosocket.ChannelContextOutputHandler;
import com.framework.niosocket.NioSocketRouter;
import com.framework.niosocket.RequestHandler;

/**
 * Created by @panyao on 2017/9/22.
 */
@NioSocketRouter(apiPathCode = com.game.gateway.proto.GATEWAY_APIS.GAME_ROOM_V1_0_0_VALUE)
public class ProxyRoom implements RequestHandler {

    @Override
    public void onRequest(ApiRequest request, ChannelContextOutputHandler outputHandler) {

    }

}