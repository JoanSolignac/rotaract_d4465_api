package com.unapi.rotaract.rotaract_d4465_api.auth.jwt;

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

/**
 * Filtro que intercepta cada petición HTTP y realiza la autenticación
 * basada en tokens JWT.
 *
 * El filtro extrae el token del encabezado Authorization (Bearer), valida
 * su firma y expiración mediante {@link JwtService}, verifica la existencia
 * y estado del usuario en la base de datos, y registra la
 * {@link org.springframework.security.core.Authentication} en el
 * {@link SecurityContextHolder} cuando procede.
 *
 * Rutas con prefijo "/auth" se consideran públicas y no pasan por la
 * validación de token.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Servicio para operaciones con JWT (generación y validación). */
    private final JwtService jwtService;

    /** Servicio que carga los detalles del usuario (roles, credenciales). */
    private final UserDetailsService userDetailsService;

    /** Repositorio de usuarios para verificar existencia y estado. */
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Permitir acceso a las rutas públicas (como login o registro)
        if (request.getServletPath().startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener el encabezado Authorization
        final String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token del encabezado
        final String jwtToken = header.substring(7).trim();
        if (jwtToken.isEmpty()) {
            returnUnauthorized(response, "Missing token");
            return;
        }

        // Extraer el correo (username) del token JWT
        final String username;
        try {
            username = jwtService.extractUsername(jwtToken);
        } catch (Exception e) {
            returnUnauthorized(response, "Invalid token");
            return;
        }

        // Si ya existe una autenticación activa, continuar la ejecución
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Verificar si el usuario existe en la base de datos
        var userOpt = usuarioRepository.findByCorreo(username);
        if (userOpt.isEmpty()) {
            returnUnauthorized(response, "User not found");
            return;
        }

        var usuario = userOpt.get();
        if (!usuario.getActivo()) {
            returnUnauthorized(response, "User inactive");
            return;
        }

        // Validar el token (firma, expiración y coincidencia de usuario)
        boolean valid = jwtService.validateToken(jwtToken,
                userDetailsService.loadUserByUsername(username));

        if (!valid) {
            returnUnauthorized(response, "Invalid or expired token");
            return;
        }

        // Cargar los detalles del usuario (roles, permisos, etc.)
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Crear el objeto de autenticación con las autoridades del usuario
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Registrar la autenticación en el contexto de seguridad de Spring
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Envía una respuesta HTTP 401 con un cuerpo JSON simple que contiene
     * el mensaje de error proporcionado y limpia el contexto de seguridad.
     *
     * @param response objeto HttpServletResponse para escribir la respuesta
     * @param message  mensaje de error que será incluido en la respuesta JSON
     * @throws IOException si ocurre un error al escribir la respuesta
     */
    private void returnUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
