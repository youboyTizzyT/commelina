package com.game.matching.router_v3;

import com.framework.akka_router.cluster.ForwardHandler;
import com.framework.akka_router.local.AbstractServiceActor;
import com.google.protobuf.Internal;

/**
 * Created by @panyao on 2017/10/9.
 */
public class MatchingServiceForward implements ForwardHandler {

    @Override
    public Internal.EnumLite getRouterId() {
        return null;
    }

    @Override
    public Class<? extends AbstractServiceActor> getPropsClass() {
        return null;
    }

}
