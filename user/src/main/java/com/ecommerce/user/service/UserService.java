package com.ecommerce.user.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.LoginResponse;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repo.UserRepo;

@Service 
public class UserService {
    
    private final UserRepo Userrepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public UserService(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder) {
        this.Userrepo=userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public User getUserById(Long id){
        return Userrepo.findById(id).orElseThrow(() ->
                    new RuntimeException("User not found with id: " + id));
    }

    public void deleteUserById(Long id) {
        User user = Userrepo.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("User not found with id: " + id));

        Userrepo.delete(user);
    }

    public User createUser(User user){
        if (user.getEmail() == null || user.getEmail().isBlank() ||
            user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                "Email and password are required");
        }

        user.setEmail(user.getEmail().trim().toLowerCase(Locale.ROOT));
        if (Userrepo.findByEmailIgnoreCase(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return Userrepo.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.email() == null ||
            request.password() == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = Userrepo.findByEmailIgnoreCase(
                request.email().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid email or password"));

        String storedPassword = user.getPassword();
        if (storedPassword == null ||
            !passwordEncoder.matches(request.password(), storedPassword)) {
            if (storedPassword == null ||
                !storedPassword.equals(request.password())) {
                throw new IllegalArgumentException(
                    "Invalid email or password");
            }
            user.setPassword(passwordEncoder.encode(request.password()));
            Userrepo.save(user);
        }

        Instant now = Instant.now();
        long expiresInSeconds = 3600;
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("ecommerce-user")
            .issuedAt(now)
            .expiresAt(now.plus(expiresInSeconds, ChronoUnit.SECONDS))
            .subject(user.getEmail())
            .claim("userId", user.getId().toString())
            .build();
        String token = jwtEncoder.encode(
            JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader
                    .with(MacAlgorithm.HS256)
                    .build(),
                claims
            )
        ).getTokenValue();

        return new LoginResponse(
            token, "Bearer", expiresInSeconds, user.getId());
    }

    public List<User> getAllUsers() {
        return Userrepo.findAll();
    }

    public User updateUser(Long Id,User user){

        User user2=Userrepo.findById(Id).orElseThrow(() -> new RuntimeException("User not found with id: " + Id));
        user2.setAddresses(user.getAddresses());
        user2.setEmail(user.getEmail());
        user2.setName(user.getName());

        return Userrepo.save(user2);
    }

}
