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

    // Inicializa la clave solo después de que Spring inyecta 'secret'
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
                .setSubject(usuarioEntity.getCorreo())
                .setClaims(Map.of("rol", usuarioEntity.getRol().getNombre()))
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

}
