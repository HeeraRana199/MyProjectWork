
package com.cts.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * AuthTokenFilter - JWT request filter.
 * Intercepts every HTTP request, extracts JWT from Authorization header,
 * validates it, and sets authentication in Spring Security's context.
 *
 * This is how stateless auth works - no sessions, just JWT on each request.
 */
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Main filter logic - runs once per request.
     * Extracts JWT → validates → sets SecurityContext → continues chain.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Step 1: Extract JWT from "Authorization: Bearer <token>" header
            String jwt = parseJwt(request);

            // Step 2: Validate and set authentication
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Extract username from token
                String username = jwtUtils.getUsernameFromJwtToken(jwt);

                // Load full user details from DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Build authentication object with authorities (roles)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                // Attach request details (IP, session, etc.)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // Store in security context - Spring now knows who the user is
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Continue processing the request
        filterChain.doFilter(request, response);
    }

    /**
     * Parse the JWT token from Authorization header.
     * Expected format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
}
 