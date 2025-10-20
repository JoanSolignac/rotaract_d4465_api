package com.unapi.rotaract.rotaract_d4465_api.club.service;

import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.club.interfaces.IClubService;
import com.unapi.rotaract.rotaract_d4465_api.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
     * @return {@link Page} de {@link ClubEntity} con los clubes de la página. Nunca {@code null}; puede estar vacío.
     * @throws IllegalArgumentException si {@code size} es menor o igual que 0
     */
    @Override
    public Page<ClubResponseDto> findAll(int page, int size) {

        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que 0.");
        }

        List<ClubResponseDto> clubList = clubRepository.findAll(PageRequest.of(page, size))
                .stream()
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
}
