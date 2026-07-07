package com.senhorcafe.openfeed.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/entrar";
    private static final String SIGNUP_PATH = "/api/v1/auth/criar-conta";

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> signUpBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Bucket bucket = switch (request.getRequestURI()) {
            case LOGIN_PATH -> loginBuckets.computeIfAbsent(request.getRemoteAddr(), key -> newLoginBucket());
            case SIGNUP_PATH -> signUpBuckets.computeIfAbsent(request.getRemoteAddr(), key -> newSignUpBucket());
            default -> null;
        };

        if (bucket == null) {
            filterChain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setHeader("Retry-After", String.valueOf(waitSeconds));
            response.setStatus(429);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Muitas tentativas, tente novamente mais tarde");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Bucket newLoginBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newSignUpBucket() {
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofHours(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
