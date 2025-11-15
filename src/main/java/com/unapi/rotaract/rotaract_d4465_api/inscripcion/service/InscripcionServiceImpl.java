package com.unapi.rotaract.rotaract_d4465_api.inscripcion.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces.IInscripcionService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InscripcionServiceImpl implements IInscripcionService {

    private final UsuarioRepository usuarioRepository;
    private final ConvocatoriaRepository convocatoriaRepository;
    private final InscripcionRepository inscripcionRepository;
    private final RolRepository rolRepository;

    @Override
    public void inscribir(Long convocatoriaId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new RuntimeException("No hay usuario autenticado.");
        }

        String correo = authentication.getName();

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));

        Long userId = usuario.getId();

        ConvocatoriaEntity convocatoria = convocatoriaRepository.findById(convocatoriaId)
                .orElseThrow(() -> new RuntimeException("Convocatoria no encontrada."));

        if (!convocatoria.getActivo()) {
            throw new RuntimeException("La convocatoria ya no está activa.");
        }

        if (convocatoria.getFechaFin().isBefore(LocalDate.now())) {
            throw new RuntimeException("La convocatoria ya finalizó.");
        }

        if (inscripcionRepository.existsByUsuarioIdAndConvocatoriaId(userId, convocatoriaId)) {
            throw new RuntimeException("Ya estás inscrito en esta convocatoria.");
        }

        if (inscripcionRepository.existsByUsuarioIdAndEstado(
                userId,
                InscripcionEntity.EstadoInscripcion.PENDIENTE
        )) {
            throw new RuntimeException("Ya tienes una inscripción pendiente en otra convocatoria.");
        }

        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .convocatoria(convocatoria)
                .fechaInscripcion(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .tipoInscripcion(InscripcionEntity.TipoInscripcion.CONVOCATORIA)
                .build();

        inscripcionRepository.save(inscripcion);
    }

    @Override
    public Page<InscripcionResponseDto> listarInscripciones(Long convocatoriaId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<InscripcionEntity> resultado = inscripcionRepository.findByConvocatoriaId(convocatoriaId, pageable);

        return resultado.map(inscripcion ->
                new InscripcionResponseDto(
                        inscripcion.getId(),
                        inscripcion.getUsuario().getId(),
                        inscripcion.getUsuario().getNombre(),
                        inscripcion.getConvocatoria().getId(),
                        inscripcion.getEstado().name(),
                        inscripcion.getTipoInscripcion().name(),
                        inscripcion.getFechaInscripcion().toString()
                )
        );
    }

    @Override
    public void aceptarInscripcion(Long inscripcionId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();

        UsuarioEntity presidente = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!presidente.getRol().getNombre().equalsIgnoreCase("PRESIDENTE")) {
            throw new RuntimeException("No tiene permisos para aceptar inscripciones.");
        }

        ClubEntity clubDelPresidente = presidente.getClub();

        if (clubDelPresidente == null) {
            throw new RuntimeException("El presidente no tiene un club asignado.");
        }

        InscripcionEntity inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada."));

        ConvocatoriaEntity convocatoria = inscripcion.getConvocatoria();

        if (!convocatoria.getClub().getId().equals(clubDelPresidente.getId())) {
            throw new RuntimeException("No puede aceptar inscripciones de convocatorias de otro club.");
        }

        UsuarioEntity usuarioAceptado = inscripcion.getUsuario();

        usuarioAceptado.setClub(clubDelPresidente);

        RolEntity rolSocio = rolRepository.findByNombre("SOCIO")
                .orElseThrow(() -> new RuntimeException("Rol SOCIO no encontrado."));

        usuarioAceptado.setRol(rolSocio);

        usuarioRepository.save(usuarioAceptado);

        inscripcion.setEstado(InscripcionEntity.EstadoInscripcion.ACEPTADO);
        inscripcionRepository.save(inscripcion);
    }


    @Override
    public void rechazarInscripcion(Long inscripcionId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();

        UsuarioEntity usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getRol().getNombre().equalsIgnoreCase("PRESIDENTE")) {
            throw new RuntimeException("No tiene permisos para rechazar inscripciones.");
        }

        InscripcionEntity inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada."));

        inscripcion.setEstado(InscripcionEntity.EstadoInscripcion.RECHAZADO);
        inscripcionRepository.save(inscripcion);
    }


}
