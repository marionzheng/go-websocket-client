package com.xxx.common.web.websocket;

import org.springframework.web.socket.WebSocketHandler;

public interface IWebSocketHandler extends WebSocketHandler {
    void setWebSocketHandler(WebSocketHandler webSocketHandler);
}
