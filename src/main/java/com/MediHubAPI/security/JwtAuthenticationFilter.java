package com.MediHubAPI.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                try {
                    if (tokenProvider.validateToken(jwt)) {
                        String username = tokenProvider.getUsernameFromToken(jwt);
                        logger.debug("Authenticating user: {}", username);

                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        logger.debug("User authorities: {}", userDetails.getAuthorities());

                        if (userDetails.getAuthorities().isEmpty()) {
                            logger.error("User has no granted authorities: {}", username);
                            throw new BadCredentialsException("User has no granted authorities");
                        }

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Authenticated user: {}", username);
                    }
                } catch (JwtAuthenticationException ex) {
                    logger.error("JWT validation failed: {}", ex.getMessage());
                    SecurityContextHolder.clearContext();
                    request.setAttribute("jwtAuthenticationException", ex);
                }
            }
        } catch (UsernameNotFoundException ex) {
            logger.error("User not found: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            request.setAttribute("userNotFound", ex);
        } catch (Exception ex) {
            logger.error("Authentication failed", ex);
            SecurityContextHolder.clearContext();
            request.setAttribute("authenticationFailure", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}