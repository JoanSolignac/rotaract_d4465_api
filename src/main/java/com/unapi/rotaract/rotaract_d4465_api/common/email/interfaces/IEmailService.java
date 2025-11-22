package com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces;

public interface IEmailService {

    /**
     * Envía un correo electrónico en formato HTML.
     *
     * @param para       Correo del destinatario
     * @param asunto     Asunto del mensaje
     * @param contenidoHtml   Contenido HTML del cuerpo del correo
     */
    void enviarCorreo(String para, String asunto, String contenidoHtml);

}
