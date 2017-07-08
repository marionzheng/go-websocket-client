package com.xxx.common.web.websocket;

import com.xxx.common.annotations.web.LoginType;
import com.xxx.common.annotations.web.NeedLogin;
import com.xxx.common.exception.ErrorCode;
import com.xxx.common.exception.ValidationException;
import com.xxx.common.utility.json.JsonUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpringWebSocketHandler implements IWebSocketHandler {
    private static final Logger LOG = LogManager.getLogger(SpringWebSocketHandler.class);

    private final Map<String, WebSocketSession> webSocketSessionMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private IEndPointConfigurer configurer = null;

    public void setConfigurer(IEndPointConfigurer configurer) {
        this.configurer = configurer;
    }

    private WebSocketHandler webSocketHandler;

    @Override
    public void setWebSocketHandler(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if(!webSocketSessionMap.containsKey(session.getId())) {
            LOG.info("WebSocket新增会话: { \"sessionId\": \"" + session.getId() + "\" }");
            webSocketSessionMap.put(session.getId(), session);
        }
        if(null != this.webSocketHandler)
            this.webSocketHandler.afterConnectionEstablished(session);
    }


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message.getPayloadLength() == 0)
            return;

        final String payload = message.getPayload().toString();

        // 广播测试，接收到一条消息时触发
        try {
            new Thread(() -> {
                try {
                    for(int i=1; i<=10; i++) {
                        Thread.sleep(1500);
                        broadcast("{\"org\":1,\"topic\":0,\"content\":\"Say hello to `" + payload + "` for " + i + " times.\"}");
                    }
                } catch (Exception ignored) {}
            }).start();
        } catch (Exception ignored) {}


        if(null != this.configurer) {
            NeedLogin loginAnn = this.configurer.getNeedLogin();
            if (null != loginAnn && payload.startsWith(CONNECT)) {
                Map<String, String> headers = getPayloadHeaders(payload);
                if (!authByPassword(headers, loginAnn))
                    throw new ValidationException(ErrorCode.AUTHREJECTED, "权限校验失败");
            }
        }

        if (null != this.webSocketHandler)
            this.webSocketHandler.handleMessage(session, message);
    }

    private static final String CONNECT = "CONNECT";
    //private static final String SUBSCRIBE = "SUBSCRIBE";
    //private static final String MESSAGE = "MESSAGE";
    //private static final String SEND = "SEND";
    private static Map<String, String> getPayloadHeaders(String payload) {
        Map<String, String> map = new HashMap<>();
        Pattern regex = Pattern.compile("([^:\\n]+):([^\\n]+)\\n");
        Matcher matcher = regex.matcher(payload);
        String key;
        String value;
        while (matcher.find()) {
            if(matcher.groupCount() == 2) {
                key = matcher.group(1);
                value = matcher.group(2);
                map.put(key, value);
            }
        }
        return map;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen())
            session.close();
        removeSession(session.getId());
        if(null != this.webSocketHandler)
            this.webSocketHandler.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        LOG.info("WebSocket: { \"sessionId\": \"" + session.getId() + "\" } 已经关闭");
        removeSession(session.getId());
        if(null != this.webSocketHandler)
            this.webSocketHandler.afterConnectionClosed(session, closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return null == this.webSocketHandler || this.webSocketHandler.supportsPartialMessages();
    }

    /**
     * 广播
     * @param payload msgBody
     */
    public void broadcast(Object payload) {
        if(this.webSocketSessionMap.size() == 0)
            return;

        final TextMessage message = new TextMessage(JsonUtils.toString(payload));
        this.webSocketSessionMap.entrySet().parallelStream().forEach(entry -> {
            try {
                if (entry.getValue().isOpen()) {
                    entry.getValue().sendMessage(message);
                }
            } catch (Exception e) {
                LOG.error("SpringWebSocketHandler broadcast failed.", e);
            }
        });
    }

    /**
     * 给某个用户发送消息
     * @param sessionId id
     * @param payload msgBody
     */
    public void sendToUser(String sessionId, Object payload) throws IOException {
        WebSocketSession session = webSocketSessionMap.get(sessionId);
        if (null == session || !session.isOpen())
            return;

        TextMessage message = new TextMessage(JsonUtils.toString(payload));
        session.sendMessage(message);
    }


    private void removeSession(String sessionId) {
        List<String> ids = webSocketSessionMap.keySet().stream().filter(p -> p.equals(sessionId)).collect(Collectors.toList());
        if(null == ids || ids.size() == 0)
            return;

        for (String id : ids) {
            webSocketSessionMap.remove(id);
        }
        LOG.info("WebSocket会话已经移除: { \"sessionId\": \"" + sessionId + "\" }");
    }

    private static final String LOGIN = "login";
    private static final String PASSCODE = "passcode";
    //基于密码校验权限
    private static boolean authByPassword(final Map<String, String> headers, final NeedLogin loginAnn)
            throws ValidationException {
        if (Arrays.stream(loginAnn.type()).noneMatch(p -> p.equals(LoginType.PRIVATE) || p.equals(LoginType.MANAGE)))
            return false;

        String key = headers.get(LOGIN);
        String pwd = headers.get(PASSCODE);
        return (loginAnn.key().equals(key) && loginAnn.pwd().equals(pwd));
    }
}
