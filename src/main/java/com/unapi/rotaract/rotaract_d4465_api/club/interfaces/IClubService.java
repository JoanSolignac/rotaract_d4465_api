package com.unapi.rotaract.rotaract_d4465_api.club.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubResponseDto;
import org.springframework.data.domain.Page;

/**
 * Contrato del servicio para operaciones relacionadas con clubes.
 * Esta interfaz expone operaciones de lectura con soporte de paginación. La
 * intención es que las implementaciones deleguen en un repositorio (p. ej.
 * Spring Data) y devuelvan objetos {@link Page} con {@link ClubResponseDto}.
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
     * @return {@link Page} de {@link ClubResponseDto} con los clubes de la página. Nunca {@code null}; puede estar vacío.
     * @throws IllegalArgumentException si los parámetros son inválidos (por ejemplo, {@code size} &lt;= 0). La implementación puede elegir validar y lanzar esta excepción o normalizar los parámetros.
     */
    Page<ClubResponseDto> findAll(int page, int size);

    /**
     * Recupera un club por su identificador.
     *
     * @param id identificador único del club (debe ser mayor que 0)
     * @return {@link ClubResponseDto} con los datos del club solicitado
     * @throws IllegalArgumentException si {@code id} es negativo o cero
     * @throws java.util.NoSuchElementException si no se encuentra un club con el id proporcionado. Las implementaciones también pueden lanzar una excepción personalizada del proyecto para modelar el caso "no encontrado".
     */
    ClubResponseDto findById(long id);

    /**
     * Crea un nuevo club a partir de los datos proporcionados en el DTO.
     *
     * La implementación debe validar los campos obligatorios del DTO
     * (por ejemplo, nombre no vacío) y persistir la entidad correspondiente.
     * Esta operación normalmente se ejecuta en una transacción.
     *
     *
     * @param clubDto DTO con los datos del club a crear. No debe ser {@code null}.
     *                Se espera que contenga los campos mínimos requeridos por la regla de negocio.
     * @throws IllegalArgumentException si {@code clubDto} es {@code null} o contiene
     *         valores inválidos (por ejemplo, nombre vacío, formato inválido).
     * @throws org.springframework.dao.DataAccessException en caso de errores de persistencia subyacentes.
     *         Las implementaciones pueden traducir errores de bajo nivel a excepciones de dominio más específicas
     *         (por ejemplo, violaciones de unicidad).
     */
    ClubResponseDto createClub(ClubCreateRequestDto clubDto);

    /**
     * Actualiza los datos de un club existente identificado por {@code id}.
     *
     * La implementación debe:
     * - Validar que el identificador y los datos del DTO son correctos.
     * - Comprobar que la entidad existe y lanzar una excepción adecuada si no existe.
     * - Aplicar los cambios y persistir la entidad en una transacción.
     *
     * Esta operación utiliza {@link ClubEditRequestDto} que representa los
     * campos editables por la API. Dependiendo de la implementación, el DTO
     * puede representar una actualización parcial (patch) o completa (put).
     *
     * Buenas prácticas recomendadas para la implementación:
     * - Validar campos obligatorios del DTO antes de persistir.
     * - No sobrescribir campos que no estén presentes en el DTO si la
     *   semántica es de actualización parcial.
     * - Ejecutar la operación dentro de una transacción para garantizar
     *   consistencia.
     *
     * @param id identificador único del club a actualizar (debe ser mayor que 0)
     * @param clubDto DTO con los nuevos datos del club; no debe ser {@code null}.
     * @return {@link ClubResponseDto} con los datos actualizados del club
     * @throws IllegalArgumentException si {@code id} es negativo o cero, o si {@code clubDto} es {@code null} o inválido
     * @throws java.util.NoSuchElementException si no existe un club con el identificador proporcionado
     * @throws org.springframework.dao.DataAccessException en caso de errores de persistencia al guardar los cambios
     */
    ClubResponseDto updateClub(long id, ClubEditRequestDto clubDto);

    /**
     * Desactiva (marca como inactivo) el club identificado por {@code id}.
     *
     * Esta operación típicamente no elimina físicamente la entidad, sino que
     * cambia un flag/status que evita que el club aparezca en listados activos.
     * La implementación debe persistir el cambio y devolver el estado resultante.
     *
     * @param id identificador del club a desactivar (debe ser mayor que 0)
     * @return {@link ClubResponseDto} con la información del club luego de la desactivación
     * @throws IllegalArgumentException si {@code id} es negativo o cero
     * @throws java.util.NoSuchElementException si no se encuentra un club con el id proporcionado
     * @throws org.springframework.dao.DataAccessException en caso de errores de persistencia
     */
    ClubResponseDto desactivateClub(long id);
}
