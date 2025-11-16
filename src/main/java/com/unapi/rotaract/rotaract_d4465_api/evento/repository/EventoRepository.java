package com.unapi.rotaract.rotaract_d4465_api.evento.repository;

import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity;
import com.unapi.rotaract.rotaract_d4465_api.evento.entity.EventoEntity.EstadoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<EventoEntity, Long> {

    /**
     * Obtiene todos los eventos cuyo estado es ACTIVO y cuya fecha de cierre ya venció.
     * Se usa para el cron de desactivación automática.
     */
    List<EventoEntity> findByEstadoAndFechaCierreBefore(EstadoEvento estado, LocalDate fechaLimite);

    /**
     * Filtrar todos los eventos por estado.
     */
    List<EventoEntity> findByEstado(EstadoEvento estado);

    /**
     * Listar todos los eventos de un club específico.
     */
    List<EventoEntity> findByClubId(Long clubId);
}
