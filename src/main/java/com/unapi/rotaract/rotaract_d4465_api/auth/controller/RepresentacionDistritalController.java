package com.unapi.rotaract.rotaract_d4465_api.auth.controller;

import com.unapi.rotaract.rotaract_d4465_api.auth.service.RepresentacionDistritalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/representacion")
@RequiredArgsConstructor
public class RepresentacionDistritalController {

    private final RepresentacionDistritalService representacionService;

    @PreAuthorize("hasAuthority('ROLE_REPRESENTANTE DISTRITAL')")
    @PostMapping("/transferir/{nuevoId}")
    public ResponseEntity<?> transferirRepresentacion(@PathVariable Long nuevoId) {

        representacionService.transferirRepresentacionDistrital(nuevoId);

        return ResponseEntity.ok(Map.of(
                "message", "Representación distrital transferida correctamente"
        ));
    }
}
