package com.mybudget.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IpLogFilterConfig {

    private static final Logger logger = LoggerFactory.getLogger(IpLogFilterConfig.class);

    @Bean
    public GlobalFilter ipLoggingFilter() {
        return (exchange, chain) -> {
            String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            String clientIp = null;
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                clientIp = xForwardedFor.split(",")[0].trim();
            } else if (exchange.getRequest().getRemoteAddress() != null) {
                clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            } else {
                clientIp = "unknown";
            }
            logger.info("REQUEST from client IP: {}", clientIp);
            return chain.filter(exchange);
        };
    }
}