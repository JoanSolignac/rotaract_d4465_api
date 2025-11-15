package com.unapi.rotaract.rotaract_d4465_api.inscripcion.repository;

import com.unapi.rotaract.rotaract_d4465_api.inscripcion.entity.InscripcionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<InscripcionEntity, Long> {

    boolean existsByUsuarioIdAndConvocatoriaId(Long usuarioId, Long convocatoriaId);

    boolean existsByUsuarioIdAndEstado(Long usuarioId, InscripcionEntity.EstadoInscripcion estado);

    Page<InscripcionEntity> findByConvocatoriaId(Long convocatoriaId, Pageable pageable);

    int rechazarPendientesPorConvocatoria(Long id);
}
