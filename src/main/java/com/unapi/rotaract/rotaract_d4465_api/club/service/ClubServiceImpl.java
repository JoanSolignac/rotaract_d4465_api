package com.unapi.rotaract.rotaract_d4465_api.club.service;

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

    /**
     * Recupera una página de clubes. Construye un {@link Pageable} a partir de los parámetros recibidos y delega
     * la consulta en {@link ClubRepository#findAll(Pageable)}.
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
     * Recupera un club por su identificador.
     * Este método delega en {@link ClubRepository#findById(Object)} y transforma la
     * entidad obtenida en {@link ClubResponseDto}.
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
     * Crea y persiste un nuevo club a partir de los datos proporcionados en el DTO.
     *
     * Comportamiento:
     * - Si {@code clubDto} es {@code null} se lanza {@link IllegalArgumentException}.
     * - Se establece {@link LocalDate#now()} en {@code fechaCreacion} y el campo {@code activo}
     *   se inicializa en {@code true} antes de persistir la entidad.
     * - La entidad resultante persistida se transforma y devuelve como {@link ClubResponseDto}.
     *
     *
     * @param clubDto DTO con los datos del club a crear. No debe ser {@code null}.
     * @return {@link ClubResponseDto} con los datos del club creado (incluyendo el id asignado por la base de datos).
     * @throws IllegalArgumentException si {@code clubDto} es {@code null} o contiene datos inválidos según las reglas de negocio.
     * @throws org.springframework.dao.DataAccessException en caso de errores de persistencia subyacentes.
     */
    @Override
    public ClubResponseDto createClub(@Valid ClubResponseDto clubDto) {

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
}
