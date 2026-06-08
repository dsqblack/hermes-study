package com.example.stock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 ECS SSH 连接配置
 * 从 application.yml ecs.* 读取
 */
@Configuration
@ConfigurationProperties(prefix = "ecs")
public class EcsConfig {
    private String host;
    private int port = 22;
    private String username;
    private String password;

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
