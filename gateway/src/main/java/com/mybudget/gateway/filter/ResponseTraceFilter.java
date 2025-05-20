package com.mybudget.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Configuration
public class ResponseTraceFilter {

    private static final Logger log = LoggerFactory.getLogger(ResponseTraceFilter.class);

    @Autowired
    private FilterUtility filterUtility;

    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) -> chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    HttpHeaders headers = exchange.getRequest().getHeaders();
                    String cid = filterUtility.getCorrelationId(headers);
                    if (cid.isEmpty()) {
                        log.warn("Response without CID – header missing in request");
                    } else {
                        log.debug("Echo CID in response headers: {}", cid);
                    }
                    exchange.getResponse()
                            .getHeaders()
                            .add(FilterUtility.CORRELATION_ID, cid);
                }));
    }
}
