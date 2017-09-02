package com.framework.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by @panyao on 2017/8/10.
 */
final class JsonMessage implements MessageBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMessage.class);

    private final BusinessMessage message;
    private final String version;

    JsonMessage(BusinessMessage message, String version) {
        this.message = message;
        this.version = version;
    }

    public byte[] getBytes() {
        try {
            return Generator.getJsonHolder().writeValueAsBytes(this.message);
        } catch (JsonProcessingException e) {
            LOGGER.error("{}", e);
            return null;
        }
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    public MessageBus.BusinessProtocol getBp() {
        return MessageBus.BusinessProtocol.JSON;
    }

}