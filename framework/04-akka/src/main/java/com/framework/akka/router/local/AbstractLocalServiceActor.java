package com.framework.akka.router.local;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.framework.akka.router.Dispatch;
import com.framework.akka.router.RouterRegistration;
import com.framework.akka.router.proto.ApiRequest;
import com.framework.core.MessageBody;
import com.google.protobuf.Internal;

/**
 * @author @panyao
 * @date 2017/9/25
 */
public abstract class AbstractLocalServiceActor extends AbstractActor implements Dispatch {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), getClass());

    private final Internal.EnumLite routerId;

    public AbstractLocalServiceActor(Internal.EnumLite routerId) {
        this.routerId = routerId;
    }

    @Override
    public void preStart() throws Exception {
        AkkaLocalWorkerSystem.INSTANCE.localRouterRegister(new RouterRegistration(routerId), getSelf());
    }

    @Override
    public final Receive createReceive() {
        ReceiveBuilder builder = receiveBuilder();
        addLocalMatch(builder);
        return builder
                .match(ApiRequest.class, this::onRequest)
                .build();
    }

    protected ReceiveBuilder addLocalMatch(ReceiveBuilder builder) {
        return builder;
    }

    public final void response(MessageBody message) {
        getSender().tell(message, getSelf());
    }

    protected LoggingAdapter getLogger() {
        return logger;
    }
}