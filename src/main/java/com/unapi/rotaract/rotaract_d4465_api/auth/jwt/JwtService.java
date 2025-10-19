package com.unapi.rotaract.rotaract_d4465_api.auth.jwt;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * Servicio responsable de la creación, parseo y validación de tokens JWT.
 *
 * Lee la configuración desde las propiedades (secret, expiration, refresh-expiration)
 * y expone utilidades para generar tokens de acceso y refresh, extraer claims
 * como el username (correo) o el rol, y validar la vigencia de un token.
 */
@Service
@RequiredArgsConstructor
public class JwtService {
    /**
     * Clave secreta en Base64 para firmar y verificar JWT. Se inyecta desde
     * la propiedad {@code jwt.secret} del archivo de configuración.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Tiempo de vida (en milisegundos) de los access tokens (propiedad {@code jwt.expiration}).
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Tiempo de vida (en milisegundos) de los refresh tokens (propiedad {@code jwt.refresh-expiration}).
     */
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * Representación en memoria de la clave derivada de {@code secret}, usada para firmar/validar JWT.
     */
    private Key key;

    // Inicializa la clave solo después de que Spring inyecta 'secret'
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }


    /**
     * Genera un token JWT de acceso para el usuario proporcionado.
     * Utiliza la expiración configurada en la propiedad 'jwt.expiration'.
     *
     * @param usuarioEntity entidad de usuario desde la que se extraen el correo y rol
     * @return token JWT firmado (compact string)
     */
    public String generateToken(final UsuarioEntity usuarioEntity){
        return buildToken(usuarioEntity, expiration);
    }

    /**
     * Genera un token JWT de refresh para el usuario proporcionado.
     * Utiliza la expiración configurada en la propiedad 'jwt.refresh-expiration'.
     *
     * @param userEntity entidad de usuario desde la que se extraen el correo y rol
     * @return token JWT de refresh firmado
     */
    public String generateRefreshToken(final UsuarioEntity userEntity){
        return buildToken(userEntity, refreshExpiration);
    }

    /**
     * Construye el JWT con los claims básicos (subject = correo, claim 'rol').
     *
     * @param usuarioEntity entidad que contiene información del usuario
     * @param tokenExpiration duración en milisegundos desde ahora hasta la expiración
     * @return token JWT firmado y serializado
     */
    private String buildToken(UsuarioEntity usuarioEntity, final Long tokenExpiration) {
        return Jwts
                .builder()
                .setClaims(Map.of("id", usuarioEntity.getId(),
                    "rol", "ROLE_" + usuarioEntity.getRol().getNombre().toUpperCase()
                    ))
                .setSubject(usuarioEntity.getCorreo())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(key)
                .compact();
    }


    /**
     * Parsea y valida el JWT usando la clave configurada y devuelve los Claims.
     * Lanzará una excepción si el token no es válido o ha sido manipulado.
     *
     * @param token token JWT (compact form)
     * @return Claims extraídos del token
     */
    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrae el nombre de usuario (subject) del token.
     *
     * @param token token JWT
     * @return subject (correo) contenido en el token
     */
    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }

    /**
     * Obtiene la fecha de expiración del token.
     *
     * @param token token JWT
     * @return fecha de expiración
     */
    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Comprueba si el token NO ha expirado.
     *
     * @param token token JWT
     * @return true si la fecha de expiración es posterior a la fecha actual
     */
    public Boolean isNotExpired(String token){
        return extractExpiration(token).after(new Date());
    }

    /**
     * Valida que el token pertenezca al usuario (comparando usernames) y que no haya expirado.
     * Nota: no comprueba otros claims ni roles más allá del 'subject' y expiración.
     *
     * @param token token JWT a validar
     * @param userDetails información del usuario esperada
     * @return true si el token es válido para el usuario dado
     */
    public Boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && isNotExpired(token));
    }

    /**
     * Extrae el rol del usuario contenido en el token JWT.
     * Este método obtiene el valor del claim {@code "rol"} incluido en el JWT durante su generación.
     * El rol se devuelve con el formato esperado por Spring Security (por ejemplo: {@code "ROLE_INVITADO"}).
     *
     * @param token token JWT del cual se desea obtener el rol del usuario
     * @return el rol del usuario según el claim {@code "rol"}; o {@code null} si el claim no está presente
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("rol", String.class);
    }

}
