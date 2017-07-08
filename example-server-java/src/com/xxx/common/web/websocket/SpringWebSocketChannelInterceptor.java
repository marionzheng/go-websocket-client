package com.xxx.common.web.websocket;

import org.apache.commons.codec.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.nio.ByteBuffer;

public class SpringWebSocketChannelInterceptor implements ChannelInterceptor {
    private static final Logger LOG = LogManager.getLogger(SpringWebSocketChannelInterceptor.class);

    private boolean isLoggingMessage = true;

    public void setLoggingMessage(boolean isLoggingMessage) {
        isLoggingMessage = isLoggingMessage;
    }

    private void logMessage(Message<?> message) {
        if(this.isLoggingMessage && null != message) {
            String payload = "";
            if (null != message.getPayload() && message.getPayload() instanceof byte[]) {
                byte[] bytes = (byte[]) message.getPayload();
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                payload = Charsets.UTF_8.decode(buffer).toString();
                buffer.clear();
            }
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            LOG.info("WebSocket: [" + accessor.getMessageType().name() + "] { \"destination\": \"" + accessor.getDestination() +
                    "\", \"sessionId\": \"" + accessor.getSessionId() + "\", \"command\": \"" + (null == accessor.getCommand() ? null : accessor.getCommand().toString()) +
                    "\", \"payload\": " + payload + " }");
        }
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        logMessage(message);
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        return true;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        logMessage(message);
        return message;
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
    }
}
