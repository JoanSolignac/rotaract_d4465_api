package com.unapi.rotaract.rotaract_d4465_api.asistencia.entity;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.proyecto.entity.ProyectoEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "asistencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private ProyectoEntity proyecto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsistencia estado;  // PRESENTE / FALTA / NO_REGISTRADO

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    public enum EstadoAsistencia {
        NO_REGISTRADO,   // antes de iniciar asistencia
        PRESENTE,        // marcado por el presidente
        FALTA            // marcado automáticamente o por cierre
    }
}
