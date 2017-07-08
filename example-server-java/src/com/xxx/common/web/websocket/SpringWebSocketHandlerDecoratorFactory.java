package com.xxx.common.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

public class SpringWebSocketHandlerDecoratorFactory implements WebSocketHandlerDecoratorFactory {
    private IWebSocketHandler webSocketHandler;

    @Autowired
    public SpringWebSocketHandlerDecoratorFactory(IWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        webSocketHandler.setWebSocketHandler(handler);
        return webSocketHandler;
    }
}
