package com.framework.akka_router;

import com.google.protobuf.Internal;

/**
 * Created by @panyao on 2017/9/25.
 */
public final class RouterNotFoundEntity {

    private final Internal.EnumLite routerId;

    public RouterNotFoundEntity(Internal.EnumLite routerId) {
        this.routerId = routerId;
    }

    public Internal.EnumLite getRouterId() {
        return routerId;
    }

}