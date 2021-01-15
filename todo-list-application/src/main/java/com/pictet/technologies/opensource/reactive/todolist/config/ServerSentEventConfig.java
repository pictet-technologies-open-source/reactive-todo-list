package com.pictet.technologies.opensource.reactive.todolist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.pictet.technologies.opensource.reactive.todolist.sse")
@Data
public final class ServerSentEventConfig {

    // Interval of time between two heart beats (used to keep the connection alive and avoid any timeout)
    private int heartBeatDelayMs = 0;

    // Period of time that the client should wait before trying to reconnect to the server is case of connection issue
    private int reconnectionDelayMs = 2_000;

}
