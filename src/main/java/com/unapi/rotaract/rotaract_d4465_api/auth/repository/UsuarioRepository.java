package com.unapi.rotaract.rotaract_d4465_api.auth.repository;

import java.util.Optional;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.RolEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;

/**
 * Repositorio JPA para la entidad UsuarioEntity.
 *
 * Provee operaciones CRUD básicas heredadas de {@link JpaRepository} y
 * consultas específicas relacionadas con usuarios y roles.
 *
 * Contrato resumido:
 * - Entrada: parámetros de consulta (correo, id de club, nombre de rol, etc.).
 * - Salida: entidades {@link UsuarioEntity} o {@link RolEntity}, envueltas en Optional o Page cuando procede.
 * - Errores: las consultas siguen la semántica de Spring Data JPA y lanzarán excepciones de persistencia
 *   (por ejemplo DataAccessException) en caso de fallos en el acceso a datos.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param correo el correo electrónico del usuario a buscar
     * @return Optional que contiene el {@link UsuarioEntity} si existe un usuario con ese correo,
     *         o un Optional vacío si no se encuentra ninguno.
     */
    Optional<UsuarioEntity> findByCorreo(String correo);

    /**
     * Busca un usuario activo por su correo electrónico.
     *
     * @param correo el correo electrónico del usuario a buscar
     * @return Optional que contiene el {@link UsuarioEntity} si existe un usuario activo con ese correo,
     *         o un Optional vacío si no se encuentra ninguno.
     */
    Optional<UsuarioEntity> findByCorreoAndActivoTrue(String correo);

    /**
     * Consulta personalizada para obtener un rol por su nombre.
     *
     * Esta consulta se define con JPQL. Se utiliza cuando se necesita resolver información
     * de roles sin cargar toda la colección de roles desde otras relaciones.
     *
     * @param nombre nombre del rol a buscar
     * @return Optional que contiene el {@link RolEntity} si existe un rol con ese nombre,
     *         o un Optional vacío si no se encuentra ninguno.
     */
    @Query("SELECT r FROM RolEntity r WHERE r.nombre = :nombre")
    Optional<RolEntity> findRolByNombre(String nombre);

    /**
     * Obtiene una página de usuarios activos pertenecientes a un club, excluyendo
     * usuarios cuyo rol coincida con el nombre proporcionado.
     *
     * Útil para listados paginados donde se debe filtrar un rol concreto (por ejemplo,
     * excluir administradores al listar miembros comunes).
     *
     * @param clubId     identificador del club al que pertenecen los usuarios
     * @param rolExcluido nombre del rol que se debe excluir de los resultados
     * @param pageable   objeto {@link Pageable} que contiene información de paginación y orden
     * @return página de {@link UsuarioEntity} que cumplen las condiciones
     */
    Page<UsuarioEntity> findByClubIdAndActivoTrueAndRol_NombreNot(
            Long clubId,
            String rolExcluido,
            Pageable pageable
    );

    /**
     * Busca el primer usuario activo de un club que tenga un rol con el nombre indicado.
     *
     * Este método es práctico cuando se espera obtener un único usuario por rol (por ejemplo,
     * obtener el presidente o un responsable) y no se necesita una lista completa.
     *
     * @param clubId identificador del club
     * @param rol nombre del rol buscado
     * @return Optional que contiene el primer {@link UsuarioEntity} que coincide y está activo,
     *         o un Optional vacío si no existe ninguno.
     */
    Optional<UsuarioEntity> findFirstByClubIdAndRol_NombreAndActivoTrue(
            Long clubId,
            String rol
    );

    /**
     * Cuenta la cantidad de usuarios activos en un club.
     *
     * @param clubId identificador del club
     * @return número de usuarios activos asociados al club
     */
    long countByClubIdAndActivoTrue(Long clubId);


}
