package com.tasks.organizer.authentication;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tasks.organizer.authentication.service.JwtService;
import com.tasks.organizer.organization.context.OrgAuthenticationToken;
import com.tasks.organizer.organization.context.OrgContext;
import com.tasks.organizer.user.service.UserService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String orgAuthHeader = request.getHeader("Org-Authorization");
        final String jwt;
        final String userEmail;
        
        //Missing or an invalid format header
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); //extract the token
        userEmail = jwtService.extractUserName(jwt); //extract username
        UUID userId = jwtService.extractUserId(jwt); //extract user id
        
        String orgJwt = null;
        if (StringUtils.isNotEmpty(orgAuthHeader) && StringUtils.startsWith(orgAuthHeader, "Bearer ")) {
            String candidate = orgAuthHeader.substring(7);
            if (StringUtils.isNotEmpty(candidate) && candidate.split("\\.").length == 3) {
                orgJwt = candidate;
            }
        }

        if (StringUtils.isNotEmpty(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                OrgContext orgContext = null;
                if (orgJwt != null && jwtService.isTokenValid(orgJwt, userDetails)) {
                    orgContext = jwtService.extractOrgContext(orgJwt);
                }

                OrgAuthenticationToken authToken =
                    new OrgAuthenticationToken(userDetails, userId, userDetails.getAuthorities(), orgContext);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }

        filterChain.doFilter(request, response);
    }

}
