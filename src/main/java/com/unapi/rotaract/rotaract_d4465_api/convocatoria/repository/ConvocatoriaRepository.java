package com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository

public interface ConvocatoriaRepository extends JpaRepository<ConvocatoriaEntity, Long> {

    Optional<ConvocatoriaEntity> findById(long id);
    List<ConvocatoriaEntity> findByActivoTrueAndFechaFinBefore(LocalDate date);
}
