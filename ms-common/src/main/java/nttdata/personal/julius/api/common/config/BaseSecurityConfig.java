package nttdata.personal.julius.api.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nttdata.personal.julius.api.common.security.JwtService;
import nttdata.personal.julius.api.common.security.SecurityUtils;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

public abstract class BaseSecurityConfig {

    protected final JwtService jwtService;

    protected BaseSecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health"
                ).permitAll();
                configureSpecificEndpoints(auth);
                auth.anyRequest().authenticated();
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {
                    authenticateRequest(request);
                    chain.doFilter(request, response);
                }
            }, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void authenticateRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7);
        if (jwtService.isTokenValid(token)) {
            Long userId = jwtService.extractUserId(token);
            SecurityUtils.setAuthentication(userId, request);
        }
    }

    protected abstract void configureSpecificEndpoints(
        org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    );
}
