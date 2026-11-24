package com.flota.taxis.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.flota.taxis.config.FleetProperties;
import com.flota.taxis.user.AppUser;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

class TokenServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef-secreto";

    private static SecretKey key(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private static JwtDecoder decoder(String secret) {
        return NimbusJwtDecoder.withSecretKey(key(secret)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private final TokenService tokens = new TokenService(
            new NimbusJwtEncoder(new ImmutableSecret<>(key(SECRET))),
            new FleetProperties(null, null, new FleetProperties.Security(SECRET, Duration.ofHours(2)), null));

    private static AppUser user() {
        AppUser user = new AppUser("Ana@Correo.com", "Ana Pérez", "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        return user;
    }

    @Test
    void elTokenEmitidoSeValidaYTraeElIdYElCorreoDelPropietario() {
        Jwt jwt = decoder(SECRET).decode(tokens.issue(user()));

        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("ana@correo.com");
        assertThat(jwt.getClaimAsString("name")).isEqualTo("Ana Pérez");
        assertThat(tokens.expiresInSeconds()).isEqualTo(7200);
    }

    @Test
    void unTokenFirmadoConOtraClaveEsRechazado() {
        String token = tokens.issue(user());

        assertThatThrownBy(() -> decoder("otra-clave-distinta-de-32-caracteres-minimo!").decode(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void laClaveCortaHaceFallarElArranque() {
        assertThatThrownBy(() -> new FleetProperties.Security("corta", null))
                .isInstanceOf(IllegalStateException.class);
    }
}
