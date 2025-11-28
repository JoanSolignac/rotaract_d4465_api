package com.unapi.rotaract.rotaract_d4465_api.common.services;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una notificación a un usuario específico.
     * Canal: /topic/notificaciones/usuario/{usuarioId}
     */
    public void enviarAUsuario(Long usuarioId, Object payload) {
        messagingTemplate.convertAndSend(
                "/topic/notificaciones/usuario/" + usuarioId,
                payload
        );
    }

    /**
     * Envía una notificación general a todos los suscritos.
     * Canal: /topic/notificaciones/general
     */
    public void enviarAGeneral(Object payload) {
        messagingTemplate.convertAndSend(
                "/topic/notificaciones/general",
                payload
        );
    }

    /**
     * NUEVO:
     * Envía una notificación a todos los miembros de un club.
     * Canal: /topic/notificaciones/club/{clubId}
     */
    public void enviarAClub(Long clubId, Object payload) {
        messagingTemplate.convertAndSend(
                "/topic/notificaciones/club/" + clubId,
                payload
        );
    }
}
