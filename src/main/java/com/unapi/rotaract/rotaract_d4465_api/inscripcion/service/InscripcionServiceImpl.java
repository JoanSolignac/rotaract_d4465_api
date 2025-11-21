package com.unapi.rotaract.rotaract_d4465_api.inscripcion.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.RolRepository;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.dtos.InscripcionResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.interfaces.IInscripcionService;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository.InscripcionRepository;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.repository.ProyectoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscripcionServiceImpl implements IInscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ConvocatoriaRepository convocatoriaRepository;
    private final ProyectoRepository proyectoRepository;

    // Obtener usuario autenticado
    private UsuarioEntity getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));
    }


    // ---------------------------------------------------------------
    // INSCRIPCIÓN EN CONVOCATORIAS (solo INVITADO)
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public void inscribirseEnConvocatoria(Long convocatoriaId) {

        UsuarioEntity usuario = getUsuarioAutenticado();

        String rol = usuario.getRol().getNombre(); // ← cambio real

        ConvocatoriaEntity convocatoriaEntity = convocatoriaRepository.findById(convocatoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Convocatoria no encontrada."));

        if (convocatoriaEntity.getCupoMaximo() - convocatoriaEntity.getInscritos()  <= 0){
            throw new IllegalArgumentException("La convocatoria no tiene cupo disponible.");
        }

        if (!rol.equalsIgnoreCase("INTERESADO")) {
            throw new IllegalArgumentException("Solo usuarios con rol INTERESADO pueden inscribirse a convocatorias.");
        }

        // Verificar que no tenga otra inscripción activa
        boolean tieneActiva = inscripcionRepository
                .existsByUsuarioIdAndConvocatoriaIsNotNullAndEstadoIn(
                        usuario.getId(),
                        List.of(
                                InscripcionEntity.EstadoInscripcion.PENDIENTE,
                                InscripcionEntity.EstadoInscripcion.ACEPTADA
                        )
                );

        if (tieneActiva) {
            throw new IllegalArgumentException("Ya tienes una inscripción activa a una convocatoria.");
        }

        ConvocatoriaEntity convocatoria = convocatoriaRepository.findById(convocatoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Convocatoria no encontrada."));

        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .convocatoria(convocatoria)
                .fechaRegistro(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .build();

        inscripcionRepository.save(inscripcion);

        convocatoriaEntity.setInscritos(convocatoriaEntity.getInscritos() + 1);
        convocatoriaRepository.save(convocatoriaEntity);
    }

    // ---------------------------------------------------------------
    // INSCRIPCIÓN EN PROYECTOS (SOCIO o PRESIDENTE)
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public void inscribirseEnProyecto(Long proyectoId) {

        UsuarioEntity usuario = getUsuarioAutenticado();

        String rol = usuario.getRol().getNombre();

        ProyectoEntity proyectoEntity = proyectoRepository.findById(proyectoId).orElseThrow(
                () -> new IllegalArgumentException("Proyecto no encontrado.")
        );

        if (!(rol.equalsIgnoreCase("SOCIO") || rol.equalsIgnoreCase("PRESIDENTE"))) {
            throw new IllegalArgumentException("Solo SOCIOS o PRESIDENTES pueden inscribirse a proyectos.");
        }

        if (proyectoEntity.getCupoMaximo() - proyectoEntity.getInscritos() <= 0) {
            throw new  IllegalArgumentException("La proyecto no tiene cupo disponible.");
        }

        boolean yaInscrito = inscripcionRepository.existsByUsuarioIdAndProyectoId(usuario.getId(), proyectoId);

        if (yaInscrito) {
            throw new IllegalArgumentException("Ya estás inscrito en este proyecto.");
        }

        ProyectoEntity proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado."));


        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .usuario(usuario)
                .proyecto(proyecto)
                .fechaRegistro(LocalDateTime.now())
                .estado(InscripcionEntity.EstadoInscripcion.PENDIENTE)
                .build();

        inscripcionRepository.save(inscripcion);
    }

    // ---------------------------------------------------------------
    // ACEPTAR INSCRIPCIÓN
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public void aceptarInscripcion(Long inscripcionId) {

        InscripcionEntity insc = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada."));

        insc.setEstado(InscripcionEntity.EstadoInscripcion.ACEPTADA);

        // Si pertenece a una convocatoria → cambiar club y rol
        if (insc.getConvocatoria() != null) {

            UsuarioEntity usuario = insc.getUsuario();
            usuario.setClub(insc.getConvocatoria().getClub());

            // Cambiar rol a SOCIO
            RolEntity rolSocio = rolRepository.findByNombre("SOCIO")
                    .orElseThrow(() -> new IllegalArgumentException("Rol SOCIO no encontrado."));

            usuario.setRol(rolSocio);
            usuarioRepository.save(usuario);
        }

        ProyectoEntity proyectoEntity = proyectoRepository.findById(insc.getProyecto().getId()).orElseThrow(
                () -> new IllegalArgumentException("Proyecto no encontrado.")
        );
        proyectoEntity.setInscritos(proyectoEntity.getInscritos() + 1);

        inscripcionRepository.save(insc);
        proyectoRepository.save(proyectoEntity);
    }

    // ---------------------------------------------------------------
    // RECHAZAR INSCRIPCIÓN
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public void rechazarInscripcion(Long inscripcionId) {

        InscripcionEntity insc = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada."));

        ProyectoEntity proyectoEntity = proyectoRepository.findById(insc.getProyecto().getId()).orElseThrow(
                () -> new IllegalArgumentException("Proyecto no encontrado.")
        );
        proyectoEntity.setInscritos(proyectoEntity.getInscritos() - 1);

        insc.setEstado(InscripcionEntity.EstadoInscripcion.RECHAZADA);
        inscripcionRepository.save(insc);
        proyectoRepository.save(proyectoEntity);
    }

    // ---------------------------------------------------------------
    // LISTADOS
    // ---------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Page<InscripcionResponseDto> listarInscripcionesConvocatoria(Long convocatoriaId, int page, int size) {
        var pageEntities = inscripcionRepository.findByConvocatoriaId(convocatoriaId, PageRequest.of(page, size));
        return pageEntities.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InscripcionResponseDto> listarInscripcionesProyecto(Long proyectoId, int page, int size) {
        var pageEntities = inscripcionRepository.findByProyectoId(proyectoId, PageRequest.of(page, size));
        return pageEntities.map(this::mapToResponse);
    }

    // ---------------------------------------------------------------
    // MAPPER
    // ---------------------------------------------------------------
    private InscripcionResponseDto mapToResponse(InscripcionEntity insc) {

        String tipo;
        Long referenciaId;
        String referenciaTitulo;

        if (insc.getConvocatoria() != null) {
            tipo = "CONVOCATORIA";
            referenciaId = insc.getConvocatoria().getId();
            referenciaTitulo = insc.getConvocatoria().getTitulo();
        } else {
            tipo = "PROYECTO";
            referenciaId = insc.getProyecto().getId();
            referenciaTitulo = insc.getProyecto().getTitulo();
        }

        UsuarioEntity u = insc.getUsuario();

        return new InscripcionResponseDto(
                insc.getId(),
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                tipo,
                referenciaId,
                referenciaTitulo,
                insc.getEstado().name(),
                insc.getFechaRegistro()
        );
    }
}
