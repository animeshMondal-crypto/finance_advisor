package com.krypto.financeadvisor.security;

import com.krypto.financeadvisor.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Step A — Read the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step B — If no token present, skip this filter entirely
        // The request will hit SecurityConfig and be rejected there if the
        // endpoint requires authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step C — Strip "Bearer " prefix to get the raw token
        String token = authHeader.substring(7);

        // Step D — Extract email and validate
        String email = jwtUtil.extractEmail(token);

        // Step E — Only set authentication if not already set
        // SecurityContextHolder.getContext().getAuthentication() == null
        // means this request hasn't been authenticated yet in this thread
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.validate(token)) {
                // Step F — Create an authentication token and put it in the context
                // This is what tells Spring Security "this request is authenticated"
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                        // credentials — null after auth
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // This is the key line — once set, Spring Security treats
                // this request as authenticated for its entire lifecycle
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step G — Always continue the filter chain
        filterChain.doFilter(request, response);
    }
}
