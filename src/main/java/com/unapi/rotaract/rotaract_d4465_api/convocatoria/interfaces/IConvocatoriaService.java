package com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces;


import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import org.springframework.data.domain.Page;

/**
 * Contrato del servicio para operaciones relacionadas con convocatorias.
 *
 * Expone operaciones básicas de lectura paginada, consulta individual, creación y
 * actualización parcial de convocatorias emitidas por un club. Las implementaciones
 * típicamente delegan en repositorios (por ejemplo Spring Data JPA) y se encargan de
 * validar parámetros, transformar entidades a DTO y manejar excepciones de persistencia.
 *
 * Los métodos retornan DTOs de respuesta {@link ConvocatoriaResponseDto} que representan
 * el estado público de la convocatoria tras la operación solicitada.
 */
public interface IConvocatoriaService {

    /**
     * Recupera una página de convocatorias.
     *
     * El resultado incluye la página solicitada y metadatos de paginación (total de elementos,
     * número total de páginas, etc.). La implementación puede normalizar valores fuera de rango
     * (por ejemplo, convertir size negativo a un valor por defecto) o lanzar excepciones si así
     * lo define la regla de negocio.
     *
     * @param page índice de la página (0-indexado; 0 = primera página)
     * @param size cantidad máxima de elementos por página (debe ser mayor que 0)
     * @return {@link Page} de {@link ConvocatoriaResponseDto}; nunca {@code null}. Puede estar vacía si no hay datos.
     * @throws IllegalArgumentException si {@code size} <= 0 o {@code page} < 0 (según validación de la implementación)
     */
    Page<ConvocatoriaResponseDto> findAll(int page, int size);

    /**
     * Recupera una convocatoria por su identificador único.
     *
     * @param id identificador de la convocatoria (debe ser mayor que 0)
     * @return {@link ConvocatoriaResponseDto} con los datos de la convocatoria
     * @throws IllegalArgumentException si {@code id} es nulo, negativo o cero
     * @throws java.util.NoSuchElementException si no existe una convocatoria con el id proporcionado
     */
    ConvocatoriaResponseDto findById(Long id);

    /**
     * Crea una nueva convocatoria a partir de los datos proporcionados.
     *
     * La implementación debe validar los campos obligatorios del DTO de creación
     * (por ejemplo título, fechas coherentes, requisitos, relación con club) y persistir la entidad.
     * Esta operación debería ejecutarse en una transacción. Tras persistir, retorna el estado
     * completo mediante un {@link ConvocatoriaResponseDto}.
     *
     * @param dto DTO con los datos necesarios para crear la convocatoria. No debe ser {@code null}.
     * @return {@link ConvocatoriaResponseDto} representando la convocatoria creada
     * @throws IllegalArgumentException si {@code dto} es {@code null} o contiene valores inválidos (fechas inconsistentes, campos vacíos)
     * @throws org.springframework.dao.DataAccessException si ocurre un error de persistencia (violaciones de integridad, etc.)
     */
    ConvocatoriaResponseDto create(ConvocatoriaCreateRequestDto dto);

    /**
     * Actualiza parcialmente una convocatoria existente.
     *
     * Sólo los campos presentes (no nulos) del {@link ConvocatoriaEditRequestDto} deben
     * ser aplicados. La implementación debe verificar la existencia de la convocatoria,
     * validar reglas (por ejemplo fechaFin no anterior a fechaInicio si se modifica) y
     * persistir los cambios dentro de una transacción.
     *
     * Buenas prácticas recomendadas:
     * <ul>
     *   <li>No sobrescribir campos no enviados si la semántica es de patch.</li>
     *   <li>Validar coherencia temporal entre fechas si ambas se actualizan.</li>
     *   <li>Registrar auditoría de cambios si aplica.</li>
     * </ul>
     *
     * @param id identificador de la convocatoria a actualizar (debe ser mayor que 0)
     * @param dto DTO con los campos editables; no debe ser {@code null}
     * @return {@link ConvocatoriaResponseDto} con el estado actualizado de la convocatoria
     * @throws IllegalArgumentException si {@code id} es inválido o {@code dto} es {@code null}
     * @throws java.util.NoSuchElementException si no existe una convocatoria con el id proporcionado
     * @throws org.springframework.dao.DataAccessException si ocurre un error de persistencia al guardar los cambios
     */
    ConvocatoriaResponseDto update(Long id, ConvocatoriaEditRequestDto dto);

}
