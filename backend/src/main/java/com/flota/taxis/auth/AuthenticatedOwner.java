package com.flota.taxis.auth;

/**
 * Propietario autenticado, tomado del token JWT. Los controladores lo reciben con
 * {@code @AuthenticationPrincipal} y pasan {@code id()} a los servicios para filtrar los datos.
 */
public record AuthenticatedOwner(Long id, String email, String name) {
}
