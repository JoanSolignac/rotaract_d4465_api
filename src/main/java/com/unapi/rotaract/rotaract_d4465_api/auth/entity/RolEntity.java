package com.unapi.rotaract.rotaract_d4465_api.auth.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Entidad JPA que representa un rol dentro del sistema.
 *
 * Un rol define permisos o capacidades asignables a los usuarios. Se mapea a
 * la tabla "roles" y mantiene una relación uno-a-muchos con {@link UsuarioEntity}.
 */
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 150)
    private String descripcion;

    /**
     * Lista de usuarios asociados a este rol. Se establece la relación
     * uno-a-muchos desde el lado del rol; las operaciones en el rol se
     * propagan a los usuarios por cascade ALL y se eliminan huérfanos.
     */
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UsuarioEntity> usuarios;
}
