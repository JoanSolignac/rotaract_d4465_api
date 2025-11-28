package com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces;

import java.util.List;

/**
 * Servicio encargado del envío de correos electrónicos mediante Brevo.
 * Soporta envío individual y masivo en formato HTML.
 */
public interface IEmailService {

    /**
     * Envía un correo electrónico HTML a un único destinatario.
     *
     * @param para          Correo del destinatario
     * @param asunto        Asunto del mensaje
     * @param contenidoHtml Contenido HTML del cuerpo del correo
     */
    void enviarCorreo(String para, String asunto, String contenidoHtml);

    /**
     * Envía un correo electrónico HTML a múltiples destinatarios.
     *
     * @param destinatarios Lista de correos electrónicos
     * @param asunto        Asunto del mensaje
     * @param contenidoHtml Contenido HTML del cuerpo del correo
     */
    void enviarCorreoMasivo(List<String> destinatarios, String asunto, String contenidoHtml);
}
