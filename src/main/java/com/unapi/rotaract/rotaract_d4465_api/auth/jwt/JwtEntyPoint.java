package com.unapi.rotaract.rotaract_d4465_api.auth.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Punto de entrada de autenticación para peticiones no autorizadas.
 *
 * Implementa {@link AuthenticationEntryPoint} y se utiliza para responder
 * con un estado HTTP 401 cuando una petición intenta acceder a un recurso
 * protegido sin credenciales válidas.
 */
public class JwtEntyPoint implements AuthenticationEntryPoint {

    /**
     * Envía una respuesta 401 (Unauthorized) cuando la autenticación falla o
     * cuando no se proporcionan credenciales válidas.
     *
     * @param request petición HTTP entrante
     * @param response respuesta HTTP que se enviará al cliente
     * @param authException excepción de autenticación que causó el fallo
     * @throws IOException si ocurre un error al escribir la respuesta
     * @throws ServletException si ocurre un error a nivel de servlet
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
