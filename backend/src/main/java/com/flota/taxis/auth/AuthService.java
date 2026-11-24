package com.flota.taxis.auth;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flota.taxis.common.exception.DuplicateResourceException;
import com.flota.taxis.common.exception.InvalidCredentialsException;
import com.flota.taxis.user.AppUser;
import com.flota.taxis.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    /** Hash de relleno: se compara aunque el correo no exista para que la respuesta tarde igual. */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.dummyHash = passwordEncoder.encode("relleno-para-igualar-tiempos");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmail(email)) {
            throw new DuplicateResourceException("Ya existe una cuenta con ese correo");
        }
        AppUser user = users.save(new AppUser(email, request.fullName(), passwordEncoder.encode(request.password())));
        return respond(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = users.findByEmail(normalize(request.email())).orElse(null);
        boolean valid;
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash);
            valid = false;
        } else {
            valid = user.isEnabled() && passwordEncoder.matches(request.password(), user.getPasswordHash());
        }
        if (!valid) {
            throw new InvalidCredentialsException();
        }
        return respond(user);
    }

    private AuthResponse respond(AppUser user) {
        OwnerResponse owner = new OwnerResponse(user.getId(), user.getEmail(), user.getFullName());
        return new AuthResponse(tokens.issue(user), tokens.expiresInSeconds(), owner);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
