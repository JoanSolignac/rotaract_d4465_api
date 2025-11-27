package com.unapi.rotaract.rotaract_d4465_api.auth.interfaces;

public interface IRepresentacionDistritalService {

    /**
     * Transfiere la representación distrital al usuario indicado.
     *
     * Reglas:
     * - Solo el usuario con rol REPRESENTANTE DISTRITAL puede realizar esta acción.
     * - El nuevo representante no puede ser PRESIDENTE.
     * - El nuevo representante debe ser SOCIO o INTERESADO.
     * - El representante saliente pasa a rol SOCIO.
     *
     * @param nuevoId ID del usuario que recibirá la representación distrital.
     */
    void transferirRepresentacionDistrital(Long nuevoId);
}
