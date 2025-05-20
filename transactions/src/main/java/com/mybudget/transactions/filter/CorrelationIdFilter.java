package com.mybudget.transactions.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID = "mybudget-correlation-id";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String cid = Optional.ofNullable(request.getHeader(CORRELATION_ID))
                .orElse(UUID.randomUUID().toString());

        MDC.put("cid", cid);
        response.setHeader(CORRELATION_ID, cid);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("cid");
        }
    }
}
