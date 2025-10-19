package com.unapi.rotaract.rotaract_d4465_api.auth.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa un rol dentro del sistema.
 *
 * Un rol define permisos o capacidades asignables a los usuarios. Se mapea a
 * la tabla "roles" y mantiene una relación uno-a-muchos con {@link UsuarioEntity}.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolEntity {

    /**
     * Identificador único del rol.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre identificador del rol (p. ej. "INTERESADO", "INVITADO", ETC). Debe ser único.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    /**
     * Descripción legible del propósito o alcance del rol (opcional).
     */
    @Column(length = 150)
    private String descripcion;

    /**
     * Lista de usuarios asociados a este rol.
     *
     * - mappedBy: nombre del atributo en {@link UsuarioEntity} que referencia al rol.
     * - cascade: se propagan operaciones de persistencia al listado de usuarios.
     * - orphanRemoval: elimina usuarios huérfanos si la relación lo determina.
     */
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UsuarioEntity> usuarios;
}
