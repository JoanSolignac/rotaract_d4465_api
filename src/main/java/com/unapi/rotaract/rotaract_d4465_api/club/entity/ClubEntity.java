package com.unapi.rotaract.rotaract_d4465_api.club.entity;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Entidad JPA que representa un Club dentro de la plataforma.
 *
 * Contiene información geográfica básica (departamento y ciudad),
 * el nombre único del club, metadatos como la fecha de creación y el
 * estado de actividad, y la lista de miembros asociados.
 */
@Entity
@Table(name = "clubs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre público del club. Debe ser único dentro del sistema.
     */
    @Column(nullable = false, unique = true, length = 155)
    private String nombre;

    /**
     * Departamento donde se ubica el club (p. ej. una provincia o estado).
     */
    @Column(nullable = false, length = 155)
    private String departamento;

    /**
     * Ciudad donde se encuentra el club.
     */
    @Column(nullable = false, length = 155)
    private String ciudad;

    /**
     * Fecha de creación del registro del club. Se almacena sólo la fecha
     * (sin hora) y por defecto se inicializa con la fecha actual.
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalDate fechaCreacion = LocalDate.now();

    /**
     * Indica si el club está activo en la plataforma. Si es false, el
     * club no debería participar en operaciones normales del sistema.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Miembros asociados al club. Relación uno-a-muchos con
     * {@link com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity}.
     *
     * - mappedBy: nombre del atributo en UsuarioEntity que referencia al club.
     * - cascade: se propagan operaciones de persistencia al listado de miembros.
     * - orphanRemoval: elimina usuarios huérfanos (dependiendo de la lógica de negocio).
     */
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UsuarioEntity> miembros;
}