package com.xxx.common.web.websocket;

import com.xxx.common.web.request.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HandshakeInterceptor
 * Created by pigme on 2016-03-24.
 */
public class SpringWebSocketInterceptor implements HandshakeInterceptor {
    private static final Logger LOG = LogManager.getLogger(SpringWebSocketInterceptor.class);

    private boolean isLoggingConnected = true;

    public void setLoggingConnected(boolean loggingConnected) {
        isLoggingConnected = loggingConnected;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if(null != request && request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession(false);
            String sessionId = "";
            if(null != session) {
                sessionId = session.getId();
                /*String userName = (String) session.getAttribute(Constants.SESSION_USERNAME);
                if(userName == null){
                    userName = "WEBSOCKET_USERNAME_IS_NULL";
                }
                attributes.put(Constants.WEBSOCKET_USERNAME, userName);*/
            }
            String uri = request.getURI().toString();
            String simpSessionId = getSimpSessionId(uri);
            if(null != simpSessionId && simpSessionId.length() > 0)
                attributes.put("simpSessionId", simpSessionId);

            if (this.isLoggingConnected) {
                LOG.info("WebSocket: { \"remoteIP\":" + Request.getRemoteAddr(servletRequest.getServletRequest()) + "\", \"method\": \"" +
                        request.getMethod().name() + "\", \"clientSessionId\": \"" + sessionId + "\", \"sessionId\": \"" +
                        simpSessionId + "\", \"uri\": \"" + uri + "\" } 已经建立连接");
            }
        }

        return true;
    }

    private static String getSimpSessionId(String uri) {
        String simpSessionId = null;
        Pattern regex = Pattern.compile("\\/(notify|queue)\\/\\d+\\/(\\w+)\\/");
        Matcher matcher = regex.matcher(uri);
        if (matcher.find()) {
            simpSessionId = matcher.group(2);
        }
        return simpSessionId;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
