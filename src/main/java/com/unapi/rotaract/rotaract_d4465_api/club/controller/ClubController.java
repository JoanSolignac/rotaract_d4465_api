package com.unapi.rotaract.rotaract_d4465_api.club.controller;

import com.unapi.rotaract.rotaract_d4465_api.club.dtos.ClubResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.club.service.ClubServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubServiceImpl clubService;

    @PreAuthorize("hasRole('REPRESENTANTE DISTRITAL')")
    @GetMapping()
    public ResponseEntity<Page<ClubResponseDto>> getAllClubs(
            @Valid @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ClubResponseDto> clubsPage = clubService.findAll(page, size);
        return ResponseEntity.ok(clubsPage);
    }
}
