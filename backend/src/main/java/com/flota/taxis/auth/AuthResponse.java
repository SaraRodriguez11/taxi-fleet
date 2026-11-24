package com.flota.taxis.auth;

public record AuthResponse(String token, long expiresInSeconds, OwnerResponse owner) {
}
