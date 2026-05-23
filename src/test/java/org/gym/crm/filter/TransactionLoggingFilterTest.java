package org.gym.crm.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionLoggingFilterTest {
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private TransactionLoggingFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/trainees/tom.tomas");
        request.setRemoteAddr("127.0.0.1");

        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldGenerateTransactionId_whenNoneProvided() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        String txId = response.getHeader("X-Transaction-Id");
        assertThat(txId).isNotNull().isNotBlank();
    }

    @Test
    void shouldReuseTransactionId_whenProvidedByUpstream() throws Exception {
        String upstreamTxId = "upstream-tx-abc-123";
        request.addHeader("X-Transaction-Id", upstreamTxId);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-Transaction-Id")).isEqualTo(upstreamTxId);
    }

    @Test
    void shouldGenerateUniqueTransactionId_perRequest() throws Exception {
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setMethod("POST");
        request2.setRequestURI("/api/trainees/register");

        filter.doFilterInternal(request, response, filterChain);
        filter.doFilterInternal(request2, response2, filterChain);

        String txId1 = response.getHeader("X-Transaction-Id");
        String txId2 = response2.getHeader("X-Transaction-Id");

        assertThat(txId1).isNotEqualTo(txId2);
    }

    @Test
    void shouldClearMDC_afterRequestCompletes() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertThat(MDC.get("transactionId")).isNull();
    }

    @Test
    void shouldClearMDC_evenWhenFilterChainThrowsException() throws Exception {
        doThrow(new RuntimeException("chain failure")).when(filterChain).doFilter(any(), any());

        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException ignored) {
        }

        assertThat(MDC.get("transactionId")).isNull();
    }

    @Test
    void shouldPopulateMDC_duringFilterChainExecution() throws Exception {
        String[] capturedTxId = new String[1];

        doAnswer(invocation -> {
            capturedTxId[0] = MDC.get("transactionId");
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(capturedTxId[0]).isNotNull().isNotBlank();
        assertThat(capturedTxId[0]).isEqualTo(response.getHeader("X-Transaction-Id"));
    }

    @Test
    void shouldPassRequestThroughFilterChain() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void shouldReturn200_onSuccessfulRequest() throws Exception {
        response.setStatus(200);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldPreserveResponseBody_on404() throws Exception {
        String errorBody = "{\"errorCode\":2835,\"errorMessage\":\"Requested data was not found: User not found\"}";

        doAnswer(invocation -> {
            HttpServletResponse resp = invocation.getArgument(1);
            resp.setStatus(404);
            resp.setContentType("application/json");
            resp.getWriter().write(errorBody);
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentAsString()).isEqualTo(errorBody);
    }

    @Test
    void shouldPreserveResponseBody_on400() throws Exception {
        String errorBody = "{\"errorCode\":2760,\"errorMessage\":\"Validation error: firstName must not be null\"}";

        doAnswer(invocation -> {
            HttpServletResponse resp = invocation.getArgument(1);
            resp.setStatus(400);
            resp.setContentType("application/json");
            resp.getWriter().write(errorBody);
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).isEqualTo(errorBody);
    }

    @Test
    void shouldPreserveResponseBody_on401() throws Exception {
        String errorBody = "{\"errorCode\":2805,\"errorMessage\":\"Authentication fails: No user authenticated\"}";

        doAnswer(invocation -> {
            HttpServletResponse resp = invocation.getArgument(1);
            resp.setStatus(401);
            resp.setContentType("application/json");
            resp.getWriter().write(errorBody);
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).isEqualTo(errorBody);
    }

    @Test
    void shouldHandleBlankTransactionIdHeader_asIfAbsent() throws Exception {
        request.addHeader("X-Transaction-Id", "   ");

        filter.doFilterInternal(request, response, filterChain);

        String txId = response.getHeader("X-Transaction-Id");
        assertThat(txId).isNotNull().isNotBlank().doesNotContain(" ");
    }
}