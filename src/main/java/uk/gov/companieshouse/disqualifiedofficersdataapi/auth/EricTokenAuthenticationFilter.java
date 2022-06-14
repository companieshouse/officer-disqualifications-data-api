package uk.gov.companieshouse.disqualifiedofficersdataapi.auth;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.companieshouse.logging.Logger;

public class EricTokenAuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger;

    public EricTokenAuthenticationFilter(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ericIdentity = request.getHeader("ERIC-Identity");

        if (StringUtils.isBlank(ericIdentity)) {
            logger.error("Unauthorised request received without eric identity");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String ericId = request.getHeader("ERIC-Identity-Type");

        if (StringUtils.isBlank(ericId) ||
                ! (ericId.equalsIgnoreCase("key") || ericId.equalsIgnoreCase("oauth2"))) {
            logger.error("Unauthorised request received without eric identity type");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
