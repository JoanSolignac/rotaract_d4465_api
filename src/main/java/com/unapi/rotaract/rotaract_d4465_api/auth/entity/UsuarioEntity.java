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
    private Date fechaNacimiento;

    @Builder.Default
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro = new Date();

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    // Relación muchos a uno con RolEntity
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    @ToString.Exclude
    private RolEntity rol;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
    private ClubEntity club;
}
