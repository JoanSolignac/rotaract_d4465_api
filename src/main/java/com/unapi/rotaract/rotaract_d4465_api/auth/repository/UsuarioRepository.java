package com.unapi.rotaract.rotaract_d4465_api.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unapi.rotaract.rotaract_d4465_api.auth.entity.UsuarioEntity;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

}
