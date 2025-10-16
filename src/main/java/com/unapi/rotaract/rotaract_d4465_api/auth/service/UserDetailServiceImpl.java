package com.unapi.rotaract.rotaract_d4465_api.auth.service;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UsuarioEntity usuarioEntity = usuarioRepository.findByCorreo(username).orElseThrow(
            () -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + username)
        );

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuarioEntity.getRol().getNombre()));

        return org.springframework.security.core.userdetails.User.builder()
            .username(usuarioEntity.getCorreo())
            .password(usuarioEntity.getContrasena())
            .disabled(!usuarioEntity.getActivo())
            .authorities(authorities)
            .build();
    }
}
