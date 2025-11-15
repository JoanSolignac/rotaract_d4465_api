package com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inscripciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convocatoria_id", nullable = false)
    private ConvocatoriaEntity convocatoria;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoInscripcion estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_inscripcion", nullable = false)
    private TipoInscripcion tipoInscripcion;

    public enum EstadoInscripcion {
        PENDIENTE,
        ACEPTADO,
        RECHAZADO
    }

    public enum TipoInscripcion {
        CONVOCATORIA,
        PROYECTO
    }

}
