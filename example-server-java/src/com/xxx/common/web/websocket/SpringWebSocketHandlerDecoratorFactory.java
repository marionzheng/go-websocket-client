package com.xxx.common.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

/**
 *
 * Created by pigme on 2016-04-01.
 */
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
