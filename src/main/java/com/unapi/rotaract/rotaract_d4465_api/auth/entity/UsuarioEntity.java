package com.unapi.rotaract.rotaract_d4465_api.auth.entity;

import com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity;
import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


/**
 * Entidad JPA que representa a un usuario registrado en la plataforma.
 *
 * Incluye datos personales básicos, credenciales (almacenadas encriptadas
 * por el mecanismo que utilice la capa de servicio) y metadatos de registro.
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario.
     */
    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Correo electrónico único del usuario. Se utiliza como identificador
     * para autenticación y comunicación.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    /**
     * Contraseña del usuario. Debe almacenarse de forma segura (hasheada)
     * por la capa de servicio antes de persistirla en la base de datos.
     */
    @Column(nullable = false)
    private String contrasena;

    /**
     * Ciudad de residencia del usuario (opcional).
     */
    @Column(length = 100)
    private String ciudad;

    /**
     * Fecha de nacimiento del usuario. Se almacena solo la fecha (sin hora).
     */
    private LocalDate fechaNacimiento;

    /**
     * Fecha y hora de registro del usuario. Se inicializa con la fecha actual
     * por defecto.
     */
    @Builder.Default
    private LocalDate fechaRegistro = LocalDate.now();

    /**
     * Indica si la cuenta de usuario está activa. Si es false, el usuario no
     * debería poder autenticarse ni realizar operaciones restringidas.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Rol asociado al usuario. Relación muchos-a-uno que indica el rol
     * efectivo del usuario dentro de la plataforma (por ejemplo, "ADMIN",
     * "INTERESADO", etc.).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    @ToString.Exclude
    private RolEntity rol;
    
    /**
     * Club al que pertenece el usuario. Relación muchos-a-uno con
     * {@link com.unapi.rotaract.rotaract_d4465_api.club.entity.ClubEntity}.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
    private ClubEntity club;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<InscripcionEntity> inscripciones;
}
