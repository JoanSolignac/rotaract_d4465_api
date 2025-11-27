package com.unapi.rotaract.rotaract_d4465_api.auth.jwt;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // ----------------------------------------------------------
        // PÚBLICOS: SOLO ESTOS ENDPOINTS
        // ----------------------------------------------------------
        if (path.equals("/auth/login")
                || path.equals("/auth/register")
                || path.equals("/auth/forgot")
                || path.equals("/auth/reset")) {

            filterChain.doFilter(request, response);
            return;
        }

        // ----------------------------------------------------------
        // LEER HEADER
        // ----------------------------------------------------------
        final String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = header.substring(7).trim();
        if (jwtToken.isEmpty()) {
            returnUnauthorized(response, "Missing token");
            return;
        }

        // Extraer usuario del token
        final String username;
        try {
            username = jwtService.extractUsername(jwtToken);
        } catch (Exception e) {
            returnUnauthorized(response, "Invalid token");
            return;
        }

        // Si ya está autenticado, continuar
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validar usuario en BD
        UsuarioEntity usuario = usuarioRepository.findByCorreo(username).orElse(null);

        if (usuario == null) {
            returnUnauthorized(response, "User not found");
            return;
        }

        if (!usuario.getActivo()) {
            returnUnauthorized(response, "User inactive");
            return;
        }

        // Validar firma y expiración
        boolean valid = jwtService.validateToken(jwtToken,
                userDetailsService.loadUserByUsername(username));

        if (!valid) {
            returnUnauthorized(response, "Invalid or expired token");
            return;
        }

        // Validar tokenVersion
        Integer versionToken = jwtService.extractTokenVersion(jwtToken);
        Integer versionActual = usuario.getTokenVersion();

        if (versionToken == null || !versionToken.equals(versionActual)) {
            returnUnauthorized(response, "Token invalidated by server");
            return;
        }

        // Registrar autenticación
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private void returnUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
