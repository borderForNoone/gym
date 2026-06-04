package com.gym.crm.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    private static final String VALID_BEARER_TOKEN = "Bearer validToken";
    private static final String VALID_TOKEN = "validToken";
    private static final String USERNAME = "tom.tomas";

    private final UserDetails userDetails = User.builder().username(USERNAME).password("hash").roles("USER").build();

    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldAuthenticateAndContinueChain_whenTokenIsValidAndUsernamePresent() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, VALID_BEARER_TOKEN);

        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(USERNAME);
        assertThat(authentication.isAuthenticated()).isTrue();
        verify(filterChain).doFilter(request, response);
        verify(jwtService).isTokenValid(VALID_TOKEN);
        verify(jwtService).extractUsername(VALID_TOKEN);
        verify(userDetailsService).loadUserByUsername(USERNAME);
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenHeaderIsMissing() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenHeaderDoesNotStartWithBearer() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abcdef");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenTokenIsInvalid() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalidToken");
        when(jwtService.isTokenValid("invalidToken")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService).isTokenValid(anyString());
        verify(jwtService, never()).extractUsername(anyString());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenAlreadyAuthenticated() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, VALID_BEARER_TOKEN);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "pass", Collections.emptyList()));

        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService).isTokenValid(VALID_TOKEN);
        verify(jwtService, never()).extractUsername(anyString());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenUsernameIsNull() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, VALID_BEARER_TOKEN);

        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService).isTokenValid(VALID_TOKEN);
        verify(jwtService).extractUsername(VALID_TOKEN);
        verifyNoInteractions(userDetailsService);
    }
}
