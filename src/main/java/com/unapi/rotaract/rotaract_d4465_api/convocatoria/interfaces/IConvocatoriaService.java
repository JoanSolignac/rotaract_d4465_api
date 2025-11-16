package com.unapi.rotaract.rotaract_d4465_api.convocatoria.interfaces;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaCreateRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaEditRequestDto;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.dtos.ConvocatoriaResponseDto;
import org.springframework.data.domain.Page;

/**
 * Interfaz de servicio que define las operaciones principales para la gestión de convocatorias.
 * Establece el contrato que deben cumplir las implementaciones responsables de registrar,
 * consultar y modificar convocatorias dentro del sistema.
 *
 * Comportamiento general:
 * - Los métodos de consulta retornan estructuras seguras, nunca null.
 * - Los métodos de modificación deben validar reglas de negocio y coherencia temporal.
 * - Las respuestas siempre se exponen mediante {@link ConvocatoriaResponseDto}.
 */
public interface IConvocatoriaService {

    /**
     * Recupera una página de convocatorias en función de los parámetros de paginación.
     *
     * @param page índice de página (0-based)
     * @param size cantidad máxima de elementos por página
     * @return página de convocatorias transformadas a {@link ConvocatoriaResponseDto}
     */
    Page<ConvocatoriaResponseDto> findAll(int page, int size);

    /**
     * Obtiene una convocatoria según su identificador único.
     *
     * @param id identificador de la convocatoria
     * @return representación como {@link ConvocatoriaResponseDto}
     * @throws IllegalArgumentException si no existe una convocatoria con el id proporcionado
     */
    ConvocatoriaResponseDto findById(Long id);

    /**
     * Registra una nueva convocatoria utilizando los datos proporcionados en el DTO.
     *
     * @param dto información requerida para crear la convocatoria
     * @return convocatoria registrada en formato {@link ConvocatoriaResponseDto}
     */
    ConvocatoriaResponseDto create(ConvocatoriaCreateRequestDto dto);

    /**
     * Actualiza parcialmente una convocatoria existente según los campos proporcionados.
     *
     * @param id identificador de la convocatoria a actualizar
     * @param dto campos editables de la convocatoria
     * @return representación actualizada como {@link ConvocatoriaResponseDto}
     */
    ConvocatoriaResponseDto update(Long id, ConvocatoriaEditRequestDto dto);
}
