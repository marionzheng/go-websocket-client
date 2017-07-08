package com.xxx.services.controller;

import com.xxx.common.annotations.web.NeedLogin;
import com.xxx.common.web.websocket.IEndPointConfigurer;

public class NotifyEndpointConfigurer implements IEndPointConfigurer {
    @Override
    public NeedLogin getNeedLogin() {
        return null;
    }
}
