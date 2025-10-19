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

/**
 * Implementación de {@link UserDetailsService} usada por Spring Security para
 * cargar los detalles de un usuario a partir de su nombre de usuario (en este
 * caso, el correo electrónico).
 *
 * Esta clase consulta {@link UsuarioRepository} para obtener la entidad
 * {@link UsuarioEntity} y construye un {@link UserDetails} con las credenciales,
 * el estado (activo/deshabilitado) y las autoridades correspondientes.
 *
 * Nota: las autoridades se generan con el prefijo {@code ROLE_} seguido del
 * nombre del rol almacenado en la entidad (por ejemplo {@code ROLE_ADMIN}).
 */
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga los datos del usuario por su nombre de usuario (correo).
     *
     * - Busca la entidad {@link UsuarioEntity} por correo.
     * - Lanza {@link UsernameNotFoundException} si no existe.
     * - Construye un {@link UserDetails} con username, password, estado y authorities.
     *
     * @param username correo electrónico del usuario a cargar
     * @return {@link UserDetails} usado por Spring Security para autenticación y autorización
     * @throws UsernameNotFoundException si no existe un usuario con el correo proporcionado
     */
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
