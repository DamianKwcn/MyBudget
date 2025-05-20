package com.mybudget.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1)
@Component
public class RequestTraceFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTraceFilter.class);

    @Autowired
    private FilterUtility filterUtility;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String cid = filterUtility.getCorrelationId(exchange.getRequest().getHeaders());

        if (cid.isEmpty()) {
            cid = filterUtility.getOrCreateCorrelationId(exchange);
            log.debug("Generated CID in gateway: {}", cid);
        } else {
            log.debug("CID found in gateway: {}", cid);
        }
        String finalCid = cid;
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put("cid", finalCid));
    }
}
