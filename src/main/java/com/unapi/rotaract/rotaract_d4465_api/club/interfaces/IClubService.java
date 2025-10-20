package com.unapi.rotaract.rotaract_d4465_api.club.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import org.springframework.data.domain.Page;

/**
 * Contrato del servicio para operaciones relacionadas con clubes.
 * Esta interfaz expone operaciones de lectura con soporte de paginación. La
 * intención es que las implementaciones deleguen en un repositorio (p. ej.
 * Spring Data) y devuelvan objetos {@link Page} de {@link ClubEntity}.
 *
 */
public interface IClubService {

    /**
     * Recupera una página de clubes.
     * El resultado contiene los clubes de la página solicitada y metadatos de
     * paginación (total de elementos, total de páginas, etc.).
     *
     * @param page índice de la página a recuperar (0-indexado; 0 = primera página)
     * @param size número máximo de elementos por página (debe ser mayor que 0)
     * @return {@link Page} de {@link ClubEntity} con los clubes de la página. Nunca {@code null}; puede estar vacío.
     * @throws IllegalArgumentException si los parámetros son inválidos (por ejemplo, {@code size} &lt;= 0). La implementación puede elegir validar y lanzar esta excepción o normalizar los parámetros.
     */
    Page<ClubEntity> findAll(int page, int size);
}
