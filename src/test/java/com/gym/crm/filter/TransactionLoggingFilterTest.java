package com.gym.crm.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        assertThat(response.getHeader("X-Transaction-Id")).isNotNull().isNotBlank();
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
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setMethod("POST");
        request2.setRequestURI("/api/trainees/register");
        MockHttpServletResponse response2 = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);
        filter.doFilterInternal(request2, response2, filterChain);

        assertThat(response.getHeader("X-Transaction-Id")).isNotEqualTo(response2.getHeader("X-Transaction-Id"));
    }

    @Test
    void shouldClearMDC_afterRequestCompletes() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertThat(MDC.get("transactionId")).isNull();
    }

    @Test
    void shouldClearMDC_evenWhenFilterChainThrowsException() throws Exception {
        doThrow(new RuntimeException("chain failure")).when(filterChain).doFilter(any(), any());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> filter.doFilterInternal(request, response, filterChain));

        assertThat(thrown.getMessage()).isEqualTo("chain failure");
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

        assertThat(capturedTxId[0]).isNotNull().isNotBlank().isEqualTo(response.getHeader("X-Transaction-Id"));
    }

    @Test
    void shouldHandleBlankTransactionIdHeader_asIfAbsent() throws Exception {
        request.addHeader("X-Transaction-Id", "   ");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-Transaction-Id")).isNotNull().isNotBlank().doesNotContain(" ");
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

    @ParameterizedTest
    @MethodSource("errorResponseProvider")
    void shouldPreserveResponseBody_onErrorStatus(int status, String errorBody) throws Exception {
        doAnswer(invocation -> {
            HttpServletResponse resp = invocation.getArgument(1);
            resp.setStatus(status);
            resp.setContentType("application/json");
            resp.getWriter().write(errorBody);
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getContentAsString()).isEqualTo(errorBody);
    }

    private static Stream<Arguments> errorResponseProvider() {
        return Stream.of(Arguments.of(404, "{\"errorCode\":2835,\"errorMessage\":\"Requested data was not found: User not found\"}"),
                Arguments.of(400, "{\"errorCode\":2760,\"errorMessage\":\"Validation error: firstName must not be null\"}"),
                Arguments.of(401, "{\"errorCode\":2805,\"errorMessage\":\"Authentication fails: No user authenticated\"}")
        );
    }
}