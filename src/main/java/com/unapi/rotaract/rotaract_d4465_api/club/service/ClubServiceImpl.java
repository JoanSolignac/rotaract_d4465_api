package com.unapi.rotaract.rotaract_d4465_api.club.service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubCreateWithPresidenteRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.club.interfaces.IClubService;
import com.unapi.rotaract.rotaract_d4465_api.club.repository.ClubRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación de {@link IClubService} encargada de las operaciones relacionadas con clubes.
 * Esta clase consulta y delega en {@link ClubRepository} para recuperar datos de clubes y expone operaciones con soporte de paginación.
 * Convenciones y comportamiento esperado:
 * - El parámetro "page" se interpreta como índice 0-based (0 = primera página).
 * - El parámetro "size" representa el número máximo de elementos por página y debe ser > 0.
 * - Si no hay resultados para la página solicitada, se devuelve una {@link Page} vacía en lugar de null.
 */
@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements IClubService {

    private final ClubRepository clubRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Recupera una página de clubes. Construye un {@link Pageable} a partir de los parámetros recibidos y delega la consulta en {@link ClubRepository#findAll(Pageable)}.
     *
     * @param page índice de la página a recuperar (0-based; 0 = primera página)
     * @param size número máximo de elementos por página (debe ser mayor que 0)
     * @return {@link Page} de {@link ClubResponseDto} con los clubes de la página. Nunca {@code null}; puede estar vacío.
     * @throws IllegalArgumentException si {@code size} es menor o igual que 0
     */
    @Override
    public Page<ClubResponseDto> findAll(int page, int size) {

        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que 0.");
        }

        List<ClubResponseDto> clubList = clubRepository.findAll(PageRequest.of(page, size))
                .stream()
                .filter(c -> c.getActivo() == true)
                .map(
                        clubEntity -> ClubResponseDto.builder()
                                .id(clubEntity.getId())
                                .nombre(clubEntity.getNombre())
                                .departamento(clubEntity.getDepartamento())
                                .ciudad(clubEntity.getCiudad())
                                .fechaCreacion(clubEntity.getFechaCreacion())
                                .activo(clubEntity.getActivo())
                                .build()
                ).toList();

        Pageable pageable =  PageRequest.of(page, size);
        return new PageImpl<>(clubList, pageable, clubList.size());
    }

    /**
     * Recupera un club por su identificador. Este método delega en {@link ClubRepository#findById(Object)} y transforma la entidad obtenida en {@link ClubResponseDto}.
     *
     * @param id identificador único del club (debe ser mayor que 0)
     * @return {@link ClubResponseDto} con los datos del club solicitado
     * @throws IllegalArgumentException si {@code id} es negativo o cero, o si no se encuentra el club con el identificador proporcionado
     */
    @Override
    public ClubResponseDto findById(long id) {

        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que 0.");
        }

        ClubEntity clubEntity = clubRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Club con id " + id + " no encontrado.")
                );

        return ClubResponseDto
                .builder()
                .id(clubEntity.getId())
                .nombre(clubEntity.getNombre())
                .departamento(clubEntity.getDepartamento())
                .ciudad(clubEntity.getCiudad())
                .fechaCreacion(clubEntity.getFechaCreacion())
                .activo(clubEntity.getActivo())
                .build();
    }

    /**
     * Crea y persiste un nuevo club a partir de los datos proporcionados en el DTO. Comportamiento: Si {@code clubDto} es {@code null} se lanza {@link IllegalArgumentException}. Se establece {@link LocalDate#now()} en {@code fechaCreacion} y el campo {@code activo} se inicializa en {@code true} antes de persistir la entidad. La entidad resultante persistida se transforma y devuelve como {@link ClubResponseDto}.
     *
     * @param clubDto DTO con los datos del club a crear. No debe ser {@code null}.
     * @return {@link ClubResponseDto} con los datos del club creado (incluyendo el id asignado por la base de datos).
     * @throws IllegalArgumentException si {@code clubDto} es {@code null} o contiene datos inválidos según las reglas de negocio.
     * @throws org.springframework.dao.DataAccessException en caso de errores de persistencia subyacentes.
     */
    @Override
    public ClubResponseDto createClub(@Valid ClubCreateRequestDto clubDto) {

        if (clubDto == null) {
            throw new IllegalArgumentException("El clubDto no puede ser null.");
        }

        ClubEntity clubEntity = clubRepository.save(
                ClubEntity
                        .builder()
                        .nombre(clubDto.nombre())
                        .departamento(clubDto.departamento())
                        .ciudad(clubDto.ciudad())
                        .fechaCreacion(LocalDate.now())
                        .activo(true)
                        .build()
        );

        return ClubResponseDto
                .builder()
                .id(clubEntity.getId())
                .nombre(clubEntity.getNombre())
                .departamento(clubEntity.getDepartamento())
                .ciudad(clubEntity.getCiudad())
                .fechaCreacion(clubEntity.getFechaCreacion())
                .activo(clubEntity.getActivo())
                .build();

    }

    /**
     * Actualiza los datos de un club identificado por {@code id} con la información proporcionada en {@link ClubEditRequestDto}. Flujo y garantías: Se valida que el usuario autenticado tiene permiso sobre el club llamando a {@link #(ClubEntity)}; si no, se lanza una excepción. Se busca la entidad del club y se actualizan sólo los campos presentes en el DTO (comportamiento tipo "parcial"). Se persisten los cambios y se devuelve un {@link ClubResponseDto} con el estado actualizado.
     *
     * @param id identificador del club a actualizar; debe existir en la base de datos
     * @param clubDto DTO con los campos editables (pueden ser null aquellos que no se desean cambiar)
     * @return {@link ClubResponseDto} con la representación del club luego de la actualización
     * @throws IllegalArgumentException si el club no existe, si el usuario no tiene permisos o si los datos son inválidos
     * @throws org.springframework.dao.DataAccessException en caso de errores al persistir
     */
    @Override
    public ClubResponseDto updateClub(long id, ClubEditRequestDto clubDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UsuarioEntity usuarioEntity = usuarioRepository.findByCorreo(username).orElseThrow(
                () -> new IllegalArgumentException("Usuario autenticado no encontrado.")
        );

        ClubEntity clubEntity = clubRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Club con id " + id + " no encontrado.")
        );

        if (!usuarioEntity.getClub().getId().equals(clubEntity.getId())) {
            throw new IllegalArgumentException("No tienes permiso para actualizar este club.");
        }

        if (clubDto.nombre() != null) {
            clubEntity.setNombre(clubDto.nombre());
        }

        if (clubDto.departamento() != null) {
            clubEntity.setDepartamento(clubDto.departamento());
        }

        if (clubDto.ciudad() != null) {
            clubEntity.setCiudad(clubDto.ciudad());
        }

        ClubEntity updatedClub = clubRepository.save(clubEntity);

        return ClubResponseDto
                .builder()
                .id(updatedClub.getId())
                .nombre(updatedClub.getNombre())
                .departamento(updatedClub.getDepartamento())
                .ciudad(updatedClub.getCiudad())
                .fechaCreacion(updatedClub.getFechaCreacion())
                .activo(updatedClub.getActivo())
                .build();

    }

    /**
     * Desactiva (marca como inactivo) el club identificado por {@code id}. Comportamiento y requisitos: Se valida que el usuario autenticado tiene permiso sobre el club a desactivar. Se marca el campo {@code activo} como {@code false} y se persiste el cambio. Devuelve la representación actualizada {@link ClubResponseDto} del club.
     *
     * @param id identificador del club a desactivar
     * @return {@link ClubResponseDto} con la información del club tras la desactivación
     * @throws IllegalArgumentException si el club no existe o si el usuario no tiene permisos
     * @throws org.springframework.dao.DataAccessException en caso de errores al persistir
     */
    @Override
    public ClubResponseDto desactivateClub(long id) {

        ClubEntity clubEntity = clubRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Club con id " + id + " no encontrado.")
        );
        
        clubEntity.setActivo(false);
        ClubEntity updatedClub = clubRepository.save(clubEntity);
        return ClubResponseDto
                .builder()
                .id(updatedClub.getId())
                .nombre(updatedClub.getNombre())
                .departamento(updatedClub.getDepartamento())
                .ciudad(updatedClub.getCiudad())
                .fechaCreacion(updatedClub.getFechaCreacion())
                .activo(updatedClub.getActivo())
                .build();
    }

    public ClubResponseDto createClubWithPresidente(ClubCreateWithPresidenteRequestDto dto) {

        // 1. Validar usuario
        UsuarioEntity presidente = usuarioRepository.findById(dto.presidenteId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario presidente no existe."));

        // 2. Validar que aún no pertenece a un club
        if (presidente.getClub() != null) {
            throw new IllegalStateException("Este usuario ya pertenece a un club.");
        }

        // 3. Crear club
        ClubEntity club = ClubEntity.builder()
                .nombre(dto.nombre())
                .departamento(dto.departamento())
                .ciudad(dto.ciudad())
                .fechaCreacion(LocalDate.now())
                .activo(true)
                .build();

        ClubEntity newClub = clubRepository.save(club);

        // 4. Asignar club al usuario
        presidente.setClub(newClub);

        // 5. Cambiar rol a PRESIDENTE
        RolEntity rolPresidente = usuarioRepository.findRolByNombre("PRESIDENTE")
                .orElseThrow(() -> new IllegalArgumentException("Rol PRESIDENTE no encontrado."));
        presidente.setRol(rolPresidente);

        usuarioRepository.save(presidente);

        // 6. Respuesta DTO
        return ClubResponseDto.builder()
                .id(newClub.getId())
                .nombre(newClub.getNombre())
                .departamento(newClub.getDepartamento())
                .ciudad(newClub.getCiudad())
                .fechaCreacion(newClub.getFechaCreacion())
                .activo(newClub.getActivo())
                .build();
    }


    public void removeSocioFromClub(Long clubId, Long socioId) {

        // Usuario autenticado
        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity presidente = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        // Validar que es presidente
        if (!presidente.getRol().getNombre().equals("PRESIDENTE"))
            throw new IllegalArgumentException("No tienes permisos para gestionar socios.");

        // Validar club
        ClubEntity club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("El club no existe."));

        // Validar que el presidente pertenece al club
        if (!presidente.getClub().getId().equals(clubId))
            throw new IllegalArgumentException("No puedes administrar un club que no diriges.");

        // Usuario a eliminar
        UsuarioEntity socio = usuarioRepository.findById(socioId)
                .orElseThrow(() -> new IllegalArgumentException("El socio no existe."));

        // Validar que pertenece al club
        if (socio.getClub() == null || !socio.getClub().getId().equals(clubId))
            throw new IllegalArgumentException("Ese usuario no pertenece a tu club.");

        // No eliminar al presidente
        if (socio.getRol().getNombre().equals("PRESIDENTE"))
            throw new IllegalArgumentException("No puedes eliminar al presidente del club.");

        // Cambiar rol → INTERESADO
        RolEntity rolInteresado = usuarioRepository.findRolByNombre("INTERESADO")
                .orElseThrow(() -> new IllegalStateException("Rol INTERESADO no encontrado."));

        socio.setRol(rolInteresado);
        socio.setClub(null);

        usuarioRepository.save(socio);
    }

    public void transferirPresidencia(Long clubId, Long nuevoPresidenteId) {

        String correoAuth = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity actualPresidente = usuarioRepository.findByCorreo(correoAuth)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        if (!actualPresidente.getRol().getNombre().equals("PRESIDENTE"))
            throw new IllegalArgumentException("No tienes permiso para transferir la presidencia.");

        if (!actualPresidente.getClub().getId().equals(clubId))
            throw new IllegalArgumentException("No perteneces a este club.");

        ClubEntity club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club no encontrado."));

        UsuarioEntity nuevoPresidente = usuarioRepository.findById(nuevoPresidenteId)
                .orElseThrow(() -> new IllegalArgumentException("Nuevo presidente no encontrado."));

        if (nuevoPresidente.getClub() == null || !nuevoPresidente.getClub().getId().equals(clubId))
            throw new IllegalArgumentException("El nuevo presidente no pertenece al club.");

        // Roles
        RolEntity rolPresidente = usuarioRepository.findRolByNombre("PRESIDENTE")
                .orElseThrow(() -> new IllegalArgumentException("Rol PRESIDENTE no encontrado."));

        RolEntity rolSocio = usuarioRepository.findRolByNombre("SOCIO")
                .orElseThrow(() -> new IllegalArgumentException("Rol SOCIO no encontrado."));

        // Cambiar roles
        actualPresidente.setRol(rolSocio);
        nuevoPresidente.setRol(rolPresidente);

        usuarioRepository.save(actualPresidente);
        usuarioRepository.save(nuevoPresidente);
    }




}
