package com.mybudget.accounts.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.CsrfToken;

import java.io.IOException;

import static org.mockito.Mockito.*;

class CsrfCookieFilterTest {

    @Test
    void shouldAddCsrfTokenToResponse() throws ServletException, IOException {
        // GIVEN
        CsrfCookieFilter filter = new CsrfCookieFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);
        when(csrfToken.getToken()).thenReturn("test-csrf-token");

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(response).addHeader("X-CSRF-TOKEN", "test-csrf-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAddCsrfTokenIfNotPresent() throws ServletException, IOException {
        // GIVEN
        CsrfCookieFilter filter = new CsrfCookieFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(response, never()).addHeader(eq("X-CSRF-TOKEN"), anyString());
        verify(filterChain).doFilter(request, response);
    }
}