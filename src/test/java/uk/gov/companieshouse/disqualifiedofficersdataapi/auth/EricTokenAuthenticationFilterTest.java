package uk.gov.companieshouse.disqualifiedofficersdataapi.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class EricTokenAuthenticationFilterTest {

    @Mock
    Logger logger;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;

    EricTokenAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        filter = new EricTokenAuthenticationFilter(logger);
    }

    @Test
    void ericTokenFilterAllowsCallWithKeyCredentials() throws Exception {
        when(request.getHeader("ERIC-Identity")).thenReturn("TEST");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("KEY");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void ericTokenFilterAllowsCallWithOauth2Credentials() throws Exception {
        when(request.getHeader("ERIC-Identity")).thenReturn("TEST");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("OAUTH2");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void ericTokenFilterBlocksCallWithEmptyIdentity() throws Exception {
        when(request.getHeader("ERIC-Identity")).thenReturn("");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("OAUTH2");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
        verify(response, times(1)).sendError(403);
    }

    @Test
    void ericTokenFilterBlocksCallWithIncorrectIdentityType() throws Exception {
        when(request.getHeader("ERIC-Identity")).thenReturn("TEST");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("INVALID");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
        verify(response, times(1)).sendError(403);
    }
}
