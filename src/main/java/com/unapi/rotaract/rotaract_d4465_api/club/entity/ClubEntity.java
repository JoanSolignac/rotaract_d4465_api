package com.unapi.rotaract.rotaract_d4465_api.club.entity;

import java.time.LocalDate;
import java.util.List;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "clubs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Entidad JPA que representa un club dentro de la plataforma Rotaract D4465.
 *
 * Cada instancia mapea a la tabla "clubs" y contiene información básica del
 * club —como nombre, ubicación, fecha de creación y estado— así como la
 * relación con sus miembros (usuarios).
 */
public class ClubEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column( nullable = false, length = 100)
    private String departamento;

    @Column( nullable = false, length = 100)
    private String ciudad;

    @Builder.Default
    /**
     * Fecha y hora de creación del registro del club.
     * Se inicializa por defecto con la fecha actual.
     */
    private LocalDate fechaCreacion =  LocalDate.now();

    @Builder.Default
    @Column(nullable = false)
    /**
     * Indica si el club está activo en la plataforma. Por defecto es true.
     */
    private Boolean activo = true;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UsuarioEntity> miembros;

    /**
     * Relación uno-a-muchos con `UsuarioEntity` que representa los miembros
     * asociados a este club. Se aplica cascade ALL y orphanRemoval para que
     * los cambios se propaguen y las referencias huérfanas se eliminen.
     */
}
