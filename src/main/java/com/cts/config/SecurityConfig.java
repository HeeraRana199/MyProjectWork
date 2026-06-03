package com.cts.config;
import com.cts.security.AuthTokenFilter;
import com.cts.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig - Main Spring Security configuration.
 *
 * Sets up:
 *   - JWT-based stateless authentication (no sessions/cookies)
 *   - Role-based access control (ADMIN, TRAINEE, LEADER)
 *   - CORS for React frontend
 *   - BCrypt password hashing
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize on controller methods
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * BCrypt password encoder - industry standard for hashing passwords.
     * Strength factor 12 means 2^12 hashing rounds (computationally expensive to crack).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * DaoAuthenticationProvider - Links Spring Security with our UserDetailsService.
     * Loads user from DB and verifies BCrypt-hashed password.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        //authProvider.setUserDetailsPasswordService(userDetailsService);
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager - Used in login endpoint to authenticate credentials.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Main security filter chain - defines URL-level access rules.
     *
     * Public endpoints (no token needed):
     *   POST /auth/login  - login for all users
     *
     * Protected endpoints (JWT required):
     *   /admin/**        - ADMIN only
     *   /leader/**       - LEADER only
     *   /trainee/**      - TRAINEE only
     *   /candidates/**   - LEADER + ADMIN
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for REST APIs with JWT)
            .csrf(csrf -> csrf.disable())

            // Enable CORS with our configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless session - no HttpSession, only JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL access rules
            .authorizeHttpRequests(auth -> auth

                    // ✅ ALLOW profile image upload & fetch (NO ROLE check)
                    // ✅ Allow image existence check + fetch + upload
                            .requestMatchers(HttpMethod.GET, "/trainee/profile-photo/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/trainee/profile-photo/**").permitAll()
//                            .requestMatchers(HttpMethod.HEAD, "/admin/profile-photo/**").permitAll()
                            .requestMatchers(HttpMethod.GET,  "/admin/profile-photo/**").permitAll()

                    // /auth/** is open at the Spring Security layer; the change-password
                    // endpoint enforces authentication itself by inspecting the SecurityContext
                    // (set by AuthTokenFilter), so it can return a proper JSON 401 instead of
                    // Spring Security's default empty-body 403.
                    .requestMatchers("/auth/**").permitAll()

                    // Public Swagger / OpenAPI documentation endpoints.
                    // /v3/api-docs returns the raw OpenAPI JSON; /swagger-ui/** serves the UI.
                    .requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-resources/**",
                            "/webjars/**"
                    ).permitAll()
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/leader/**").hasAnyAuthority("ROLE_LEADER", "ROLE_ADMIN")
                .requestMatchers("/trainee/**").hasAuthority("ROLE_TRAINEE")
                .anyRequest().authenticated()
            );

        // Register our custom authentication provider
        http.authenticationProvider(authenticationProvider());

        // Add JWT filter BEFORE Spring's default username/password filter
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration - allows React frontend (localhost:3000) to call our API.
     * In production, replace with your actual frontend domain.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow frontend origin(s) from comma-separated config property
        List<String> allowedOriginList = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        configuration.setAllowedOrigins(allowedOriginList);

        // Allow all standard HTTP methods
        configuration.setAllowedMethods(List.of("HEAD","GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allow all request headers including Authorization (JWT token)
        configuration.setAllowedHeaders(List.of("*"));

        // Expose headers for browser clients if needed
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // Allow credentials (cookies, auth headers)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}