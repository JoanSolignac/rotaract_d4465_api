package com.unapi.rotaract.rotaract_d4465_api.convocatoria.schelude;

import com.unapi.rotaract.rotaract_d4465_api.convocatoria.entity.ConvocatoriaEntity;
import com.unapi.rotaract.rotaract_d4465_api.convocatoria.repository.ConvocatoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocatoriaScheludeService {

    private final ConvocatoriaRepository convocatoriaRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void deactivateExpiredConvocatorias() {

        List<ConvocatoriaEntity> convocatoriaEntities = convocatoriaRepository.findByActivoTrueAndFechaFinBefore(LocalDate.now());

        if (!convocatoriaEntities.isEmpty()) {
            convocatoriaEntities.forEach(
                    convocatoriaEntity -> convocatoriaEntity.setActivo(false)
            );
            log.info("Cantidad de convocatorias desactivadas: {}", convocatoriaEntities.size());
        }else{
            log.info("No hay convocatorias para desactivar");
        }
    }

}
