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
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(final UsuarioEntity usuarioEntity){
        return buildToken(usuarioEntity, expiration);
    }

    public String generateRefreshToken(final UsuarioEntity userEntity){
        return buildToken(userEntity, refreshExpiration);
    }

    private String buildToken(UsuarioEntity usuarioEntity, final Long tokenExpiration) {
        return Jwts
                .builder()
                .setClaims(Map.of(
                        "id", usuarioEntity.getId(),
                        "rol", "ROLE_" + usuarioEntity.getRol().getNombre().toUpperCase(),
                        "nombre", usuarioEntity.getNombre()
                ))
                .setSubject(usuarioEntity.getCorreo())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(key)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    public Boolean isNotExpired(String token){
        return extractExpiration(token).after(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && isNotExpired(token));
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("rol", String.class);
    }

    // --------------------------------------------------------------------
    // 🔥 TOKEN DE RECUPERACIÓN (10 minutos)
    // --------------------------------------------------------------------
    public String generatePasswordResetToken(final UsuarioEntity usuarioEntity) {

        long resetExpiration = 10 * 60 * 1000; // 10 minutos

        return Jwts.builder()
                .setClaims(Map.of(
                        "id", usuarioEntity.getId(),
                        "rol", "ROLE_" + usuarioEntity.getRol().getNombre().toUpperCase(),
                        "reset", true,
                        "nombre", usuarioEntity.getNombre()
                ))
                .setSubject(usuarioEntity.getCorreo())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + resetExpiration))
                .signWith(key)
                .compact();
    }

    public boolean isPasswordResetToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object resetClaim = claims.get("reset");
            return resetClaim != null && Boolean.TRUE.equals(resetClaim);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return isNotExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
