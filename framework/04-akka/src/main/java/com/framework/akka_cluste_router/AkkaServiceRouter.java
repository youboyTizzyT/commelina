package com.framework.akka_cluste_router;

import org.springframework.stereotype.Component;

/**
 * Created by @panyao on 2017/9/25.
 */
@Component
public @interface AkkaServiceRouter {

    int routerId();

}
