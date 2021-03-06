package com.commelina.akka.dispatching.nodes;

import akka.actor.AbstractActor;
import akka.actor.Terminated;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.commelina.akka.dispatching.Constants;
import com.commelina.akka.dispatching.DispatchForward;
import com.commelina.akka.dispatching.MemberEvent;
import com.commelina.akka.dispatching.proto.*;

/**
 * 后端 actor
 *
 * @author @panyao
 * @date 2017/9/25
 */
public abstract class AbstractBackendActor extends AbstractActor implements DispatchForward, MemberEvent {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), getClass());

    private final Cluster cluster = Cluster.get(getContext().system());

    private BackendFindFrontend findFrontendSocket;

    //subscribe to cluster changes, MemberUp
    @Override
    public void preStart() {
        cluster.subscribe(self(), ClusterEvent.MemberUp.class, ClusterEvent.MemberRemoved.class);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ApiRequest.class, this::onRequest)
                .match(ApiRequestForward.class, this::onForward)
                .match(MemberOfflineEvent.class, this::onOffline)
                .match(MemberOnlineEvent.class, this::onOnline)
                .match(BackendFindEvent.class, e -> getSender().tell(findFrontendSocket, getSelf()))

                .match(ClusterEvent.CurrentClusterState.class, state -> {
                    for (Member member : state.getMembers()) {
                        if (member.status().equals(MemberStatus.up())) {
                            register(member);
                        } else if (member.status().equals(MemberStatus.removed())) {
                            remove(member);
                        }
                    }
                })
                .match(ClusterEvent.MemberUp.class, mUp -> register(mUp.member()))
                .match(ClusterEvent.MemberRemoved.class, mRem -> remove(mRem.member()))
                .match(Terminated.class, t -> {
                    logger.info("Frontend {} left.", getSender());
//                    getContext().unwatch(getSender());
//                    ClusterChildNodeSystem.INSTANCE.removeRouterFronted();
                })
                .build();
    }

    @Override
    public void onOnline(MemberOnlineEvent onlineEvent) {
        // nothing to do
    }

    @Override
    public void onOffline(MemberOfflineEvent offlineEvent) {
        // nothing to do
    }

    @Override
    public void onForward(ApiRequestForward forward) {
        // nothing to do
    }

    void register(Member member) {
        if (member.hasRole(Constants.CLUSTER_FRONTEND)) {
            logger.info("Frontend port:{} , nodes register.", member.address().port().get());
            String frontendPath = member.address() + Constants.CLUSTER_FRONTEND_PATH;
            findFrontendSocket = BackendFindFrontend.newBuilder().setFrontendAddress(frontendPath).build();
        }
    }

    void remove(Member member) {
        if (member.hasRole(Constants.CLUSTER_FRONTEND)) {
            logger.info("Frontend port:{} , nodes remove.", member.address().port().get());
            findFrontendSocket = BackendFindFrontend.getDefaultInstance();
        }
    }

    protected void response(ActorResponse message) {
        getSender().tell(message, getSelf());
    }

    protected void emptyResponse() {
        response(ActorResponse.getDefaultInstance());
    }

    protected LoggingAdapter getLogger() {
        return logger;
    }

}
