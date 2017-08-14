package com.game.foundation.gateway.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.nexus.maven.akka.AkkaBroadcastDomain;
import com.nexus.maven.akka.AkkaNotifyDomain;
import com.nexus.maven.akka.AkkaRequestDomain;
import com.nexus.maven.akka.AkkaResponseDomain;
import com.nexus.maven.netty.socket.BusinessMessage;
import com.nexus.maven.netty.socket.MessageAdapter;
import com.nexus.maven.netty.socket.MessageHandler;
import com.nexus.maven.netty.socket.MessageNotifyJsonHandler;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * Created by @panyao on 2017/8/14.
 */
@Component
public class AkkaMatching {

    private final ActorSystem matchingSystem = ActorSystem.create("matching");

    @Resource
    private MessageAdapter messageAdapter;

    public Future<MessageHandler> handler1(String typeRouter, Object[] args) {
        final ActorRef remote = matchingSystem.actorOf(Props.create(EmptyRemote.class), "matchingRemote");

        // FIXME: 2017/8/14 akka 超时设置
        final Timeout timeout = new Timeout(Duration.create(5, "seconds"));

        final Future<Object> matching = Patterns.ask(remote, (ActorRef actorRef) -> AkkaRequestDomain.newDomain(typeRouter, args), timeout);
        Iterable<Future<Object>> futureArray = Arrays.asList(matching);

        Future<Iterable<Object>> futureResult = Futures.traverse(futureArray,
                (final Future<Object> param) ->
                        Futures.future(() ->
                                Await.result(param, timeout.duration()), matchingSystem.dispatcher()),
                matchingSystem.dispatcher());

        // FIXME: 2017/8/14 这里明天需要仔细看 akka 的远程文档

        return null;
    }

    public Future<MessageHandler> handler(String typeRouter, Object[] args) {

        class AkkaSender extends UntypedActor {

            @Override
            public void onReceive(Object o) throws Throwable {

                if (o instanceof AkkaResponseDomain) {
                    AkkaResponseDomain response = (AkkaResponseDomain) o;


                } else if (o instanceof AkkaNotifyDomain) {
                    AkkaNotifyDomain notify = (AkkaNotifyDomain) o;
                    for (AkkaNotifyDomain.MessageEntity messageEntity : notify.getMessages()) {
                        messageAdapter.addNotify(MessageNotifyJsonHandler
                                .newHandler(messageEntity.getUserId(), 0,
                                        BusinessMessage.success(0, messageEntity.getMessage())
                                ));
                    }

                } else if (o instanceof AkkaBroadcastDomain) {
                    AkkaBroadcastDomain broadcast = (AkkaBroadcastDomain) o;
                    for (Long aLong : broadcast.getUserIds()) {
                        messageAdapter.addNotify(MessageNotifyJsonHandler
                                .newHandler(aLong, 0,
                                        BusinessMessage.success(0, broadcast.getMessage())
                                ));
                    }
                } else {
                    this.unhandled(o);
                }
            }

        }

        final ActorRef remote = matchingSystem.actorOf(Props.create(EmptyRemote.class), "matchingRemote");

        final ActorRef sender = matchingSystem.actorOf(Props.create(AkkaSender.class), "matchingSender");

        remote.tell(AkkaRequestDomain.newDomain(typeRouter, args), sender);

        return null;
    }


    private static class EmptyRemote {

    }

}
