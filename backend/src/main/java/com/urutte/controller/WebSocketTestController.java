package com.urutte.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketTestController {

    @GetMapping("/api/websocket/test")
    public String testWebSocket() {
        return "WebSocket endpoint is accessible";
    }
}
