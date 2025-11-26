package com.unapi.rotaract.rotaract_d4465_api.miembro.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;
import com.unapi.rotaract.rotaract_d4465_api.auth.repository.UsuarioRepository;
import com.unapi.rotaract.rotaract_d4465_api.miembro.dtos.MiembroResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.miembro.dtos.RepresentanteResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.miembro.dtos.TotalUsuariosResponseDto;
import com.unapi.rotaract.rotaract_d4465_api.miembro.interfaces.IMiembroService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MiembroServiceImpl implements IMiembroService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Page<MiembroResponseDto> listarMiembrosPaginados(Pageable pageable) {

        Page<UsuarioEntity> pagina = usuarioRepository.findAll(pageable);

        return pagina.map(user ->
                new MiembroResponseDto(
                        user.getId(),
                        user.getNombre(),
                        user.getCorreo(),
                        user.getRol().getNombre(),
                        user.getActivo()
                )
        );
    }

    @Override
    public TotalUsuariosResponseDto obtenerTotalUsuarios() {
        Long total = usuarioRepository.count();
        return new TotalUsuariosResponseDto(total);
    }

    @Override
    public RepresentanteResponseDto obtenerRepresentanteDistrital() {

        UsuarioEntity representante = usuarioRepository.findByRolNombre("REPRESENTANTE DISTRITAL");

        if (representante == null) {
            return new RepresentanteResponseDto(null, null, null);
        }

        return new RepresentanteResponseDto(
                representante.getId(),
                representante.getNombre(),
                representante.getCorreo()
        );
    }
}
