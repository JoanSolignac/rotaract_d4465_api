package com.unapi.rotaract.rotaract_d4465_api.common.email.controller;

import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailTestController {

    private final IEmailService emailService;

    @GetMapping("/test")
    public String enviarCorreoDePrueba() {

        emailService.enviarCorreo(
                "TU_CORREO_REAL@gmail.com", // reemplazar por tu correo
                "Prueba API Brevo - Rotaract D4465",
                "<h1>¡Correo enviado correctamente usando la API de Brevo!</h1>"
                        + "<p>Este mensaje fue enviado desde el backend Rotaract D4465 usando la API REST.</p>"
        );

        return "OK - correo enviado con API Brevo";
    }
}
