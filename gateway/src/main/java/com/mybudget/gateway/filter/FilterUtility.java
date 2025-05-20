package com.mybudget.gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;


@Component
public class FilterUtility {
    public static final String CORRELATION_ID = "mybudget-correlation-id";

    public String getCorrelationId(HttpHeaders headers) {
        String value = headers.getFirst(CORRELATION_ID);
        return value != null ? value : "";
    }

    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String name, String value) {
        return exchange.mutate()
                .request(builder -> builder.header(name, value))
                .build();
    }

    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }

    public String getOrCreateCorrelationId(ServerWebExchange exchange) {
        String cid = getCorrelationId(exchange.getRequest().getHeaders());
        if (cid.isEmpty()) {
            cid = UUID.randomUUID().toString();
            setCorrelationId(exchange, cid);
        }
        return cid;
    }
}