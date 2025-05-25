package com.mybudget.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	private final Logger logger = LoggerFactory.getLogger(GatewayApplication.class);

	@Bean
	public KeyResolver userKeyResolver() {
		return exchange ->
				ReactiveSecurityContextHolder.getContext()
						.flatMap(context -> {
							Authentication auth = context.getAuthentication();
							if (auth != null && auth.isAuthenticated()) {
								Object principal = auth.getPrincipal();
								if (principal instanceof Jwt jwt) {
									String key = jwt.getClaim("preferred_username");
									logger.info("Resolved key: {}", key);
									return Mono.just(key);
								}
							}
							String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
							String clientIp = null;
							if (xForwardedFor != null && !xForwardedFor.isBlank()) {
								clientIp = xForwardedFor.split(",")[0].trim();
							} else if (exchange.getRequest().getRemoteAddress() != null) {
								clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
							} else {
								clientIp = "unknown";
							}
							return Mono.just(clientIp);
						})
						.defaultIfEmpty("unknown");
	}



}
