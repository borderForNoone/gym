package org.gym.crm.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class TransactionLoggingFilter extends OncePerRequestFilter {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";
    private static final int MAX_PAYLOAD_SIZE = 10_000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_TRANSACTION_ID_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_SIZE);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            String authHeader = request.getHeader("Authorization");
            log.info(">>> [{} {}] ip={} auth={} txId={}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(),
                    SensitiveDataMasker.maskAuthHeader(authHeader),
                    transactionId);

            filterChain.doFilter(wrappedRequest, wrappedResponse);

            long duration = System.currentTimeMillis() - startTime;
            int status = wrappedResponse.getStatus();

            if (status >= 400) {
                String rawBody = new String(
                        wrappedResponse.getContentAsByteArray(),
                        wrappedResponse.getCharacterEncoding());
                log.warn("<<< [{} {}] status={} duration={}ms body={}", request.getMethod(), request.getRequestURI(), status, duration, SensitiveDataMasker.maskBody(rawBody));
            } else {
                log.info("<<< [{} {}] status={} duration={}ms", request.getMethod(), request.getRequestURI(), status, duration);
            }
        } finally {
            wrappedResponse.copyBodyToResponse();
            MDC.remove(MDC_TRANSACTION_ID_KEY);
        }
    }
}