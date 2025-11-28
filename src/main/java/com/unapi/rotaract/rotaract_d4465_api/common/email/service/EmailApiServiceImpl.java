package com.unapi.rotaract.rotaract_d4465_api.common.email.service;

import com.unapi.rotaract.rotaract_d4465_api.common.email.interfaces.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailApiServiceImpl implements IEmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void enviarCorreo(String para, String asunto, String contenidoHtml) {

        String url = "https://api.brevo.com/v3/smtp/email";

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of(
                "name", "Rotaract D4465",
                "email", "rotaractd4465noreply@gmail.com"
        ));

        body.put("to", List.of(Map.of("email", para)));
        body.put("subject", asunto);
        body.put("htmlContent", contenidoHtml);

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForObject(url, request, String.class);
    }

    /**
     * NUEVO:
     * Enviar un correo masivo a una lista de destinatarios.
     */
    @Override
    public void enviarCorreoMasivo(List<String> destinatarios, String asunto, String contenidoHtml) {

        if (destinatarios == null || destinatarios.isEmpty()) return;

        String url = "https://api.brevo.com/v3/smtp/email";

        List<Map<String, String>> listaDestinatarios = destinatarios.stream()
                .map(email -> Map.of("email", email))
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of(
                "name", "Rotaract D4465",
                "email", "rotaractd4465noreply@gmail.com"
        ));
        body.put("to", listaDestinatarios);
        body.put("subject", asunto);
        body.put("htmlContent", contenidoHtml);

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForObject(url, request, String.class);
    }
}
