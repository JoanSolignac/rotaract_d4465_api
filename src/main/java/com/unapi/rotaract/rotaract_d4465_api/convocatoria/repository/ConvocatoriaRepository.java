package com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link ConvocatoriaEntity}.
 *
 * Proporciona operaciones CRUD básicas mediante {@link JpaRepository} y
 * consultas derivadas específicas para recuperar convocatorias por
 * distintos criterios (por ejemplo, por club o por estado).
 *
 * Contrato y comportamiento:
 * - Los métodos de consulta devuelven listas vacías o páginas vacías cuando no
 *   existen resultados que cumplan las condiciones.
 * - Las consultas siguen las convenciones de nombres de Spring Data JPA;
 *   no existe lógica de negocio en este repositorio.
 * - Las excepciones relacionadas con la persistencia (p. ej. problemas de conexión
 *   o violaciones de integridad) se propagan según la semántica de Spring Data
 *   (DataAccessException y subclases).
 */
@Repository
public interface ConvocatoriaRepository extends JpaRepository<ConvocatoriaEntity, Long> {

    /**
     * Recupera todas las convocatorias asociadas a un club determinado.
     *
     * Uso típico: listar todas las convocatorias creadas por un club sin paginación.
     *
     * @param clubId identificador del club creador de las convocatorias
     * @return lista de {@link ConvocatoriaEntity} pertenecientes al club; puede ser lista vacía
     *         si no existen convocatorias para ese club
     */
    List<ConvocatoriaEntity> findByClubId(Long clubId);

    /**
     * Recupera una página de convocatorias asociadas a un club.
     *
     * Uso típico: listados en UI con paginación y ordenamiento gestionados por
     * el objeto {@link Pageable}.
     *
     * @param clubId   identificador del club creador
     * @param pageable información de paginación y orden (número de página, tamaño, sort)
     * @return {@link Page} de {@link ConvocatoriaEntity} para la página solicitada; puede estar vacía
     */
    Page<ConvocatoriaEntity> findByClubId(Long clubId, Pageable pageable);

    /**
     * Obtiene todas las convocatorias que tengan el estado especificado.
     *
     * El estado se corresponde con {@link EventoEntity.EstadoEvento} y permite
     * filtrar convocatorias por estados del ciclo de vida (ej. PENDIENTE, ACTIVA, FINALIZADA).
     *
     * @param estado valor de {@link EventoEntity.EstadoEvento} usado como filtro
     * @return lista de {@link ConvocatoriaEntity} que coinciden con el estado; puede ser vacía
     */
    List<ConvocatoriaEntity> findByEstado(EventoEntity.EstadoEvento estado);

    /**
     * Cuenta el número de convocatorias asociadas a un club.
     *
     * Útil para estadísticas, validaciones o paginación previa cuando se necesita
     * conocer el total de elementos.
     *
     * @param clubId identificador del club
     * @return número total de convocatorias cuyo campo clubId coincide con el valor
     */
    long countByClubId(Long clubId);

    /**
     * Recupera las convocatorias de un club ordenadas por fecha de inicio
     * en orden descendente (más recientes primero).
     *
     * Uso típico: mostrar primero las convocatorias próximas o más recientes.
     *
     * @param clubId identificador del club
     * @return lista de {@link ConvocatoriaEntity} ordenada por fechaInicio descendente;
     *         puede ser lista vacía si no hay convocatorias
     */
    List<ConvocatoriaEntity> findByClubIdOrderByFechaInicioPostulacionDesc(Long clubId);

}
