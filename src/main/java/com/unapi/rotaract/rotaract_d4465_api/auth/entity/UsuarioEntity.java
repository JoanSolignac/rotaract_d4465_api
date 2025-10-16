package com.unapi.rotaract.rotaract_d4465_api.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Entidad JPA que representa a un usuario registrado en la plataforma.
 *
 * Incluye datos personales básicos, credenciales (almacenadas encriptadas
 * por el mecanismo que utilice la capa de servicio) y metadatos de registro.
 */
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    @Column(length = 100)
    private String ciudad;

    @Temporal(TemporalType.DATE)
    /**
     * Fecha de nacimiento del usuario. Se almacena solo la fecha (sin hora).
     */
    private Date fechaNacimiento;

    @Builder.Default
    @Temporal(TemporalType.TIMESTAMP)
    /**
     * Fecha y hora de registro del usuario. Se inicializa con la fecha actual
     * por defecto.
     */
    private Date fechaRegistro = new Date();

    @Builder.Default
    @Column(nullable = false)
    /**
     * Indica si la cuenta de usuario está activa. Si es false, el usuario no
     * debería poder autenticarse ni realizar operaciones restringidas.
     */
    private Boolean activo = true;

    // Relación muchos a uno con RolEntity
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    @ToString.Exclude
    private RolEntity rol;

    /**
     * Rol asociado al usuario. Esta relación muchos-a-uno indica el rol
     * efectivo del usuario dentro de la plataforma (por ejemplo, "ADMIN",
     * "INTERESADO", etc.). El rol se carga en modo EAGER por defecto.
     */

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
    private ClubEntity club;
}
