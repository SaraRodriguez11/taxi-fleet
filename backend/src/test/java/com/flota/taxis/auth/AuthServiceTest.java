package com.flota.taxis.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.flota.taxis.common.exception.DuplicateResourceException;
import com.flota.taxis.common.exception.InvalidCredentialsException;
import com.flota.taxis.user.AppUser;
import com.flota.taxis.user.UserRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private TokenService tokens;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4); // costo bajo: pruebas rápidas

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, encoder, tokens);
    }

    private AppUser storedUser(String rawPassword) {
        return new AppUser("ana@correo.com", "Ana", encoder.encode(rawPassword));
    }

    @Test
    void registrarGuardaElCorreoEnMinusculasYLaContrasenaCifrada() {
        when(users.existsByEmail("ana@correo.com")).thenReturn(false);
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokens.issue(any(AppUser.class))).thenReturn("jwt");

        AuthResponse response = service.register(new RegisterRequest("Ana", "  Ana@Correo.com ", "secreta123"));

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ana@correo.com");
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("secreta123");
        assertThat(encoder.matches("secreta123", saved.getValue().getPasswordHash())).isTrue();
        assertThat(response.token()).isEqualTo("jwt");
        assertThat(response.owner().email()).isEqualTo("ana@correo.com");
    }

    @Test
    void registrarConUnCorreoExistenteFalla() {
        when(users.existsByEmail("ana@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest("Ana", "ana@correo.com", "secreta123")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void iniciarSesionConCredencialesCorrectasDevuelveElToken() {
        when(users.findByEmail("ana@correo.com")).thenReturn(Optional.of(storedUser("secreta123")));
        when(tokens.issue(any(AppUser.class))).thenReturn("jwt");
        when(tokens.expiresInSeconds()).thenReturn(3600L);

        AuthResponse response = service.login(new LoginRequest("ANA@correo.com", "secreta123"));

        assertThat(response.token()).isEqualTo("jwt");
        assertThat(response.expiresInSeconds()).isEqualTo(3600);
    }

    @Test
    void contrasenaIncorrectaYCorreoInexistenteDanElMismoError() {
        when(users.findByEmail("ana@correo.com")).thenReturn(Optional.of(storedUser("secreta123")));
        when(users.findByEmail("nadie@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("ana@correo.com", "incorrecta")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Correo o contraseña incorrectos");
        assertThatThrownBy(() -> service.login(new LoginRequest("nadie@correo.com", "lo-que-sea")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }
}
