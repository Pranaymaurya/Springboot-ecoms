package com.ecommerce.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repo.UserRepo;
import com.ecommerce.user.service.UserService;

import static org.mockito.Mockito.mock;

class UserServiceTests {

    @Test
    void registrationHashesPasswordAndLoginIssuesToken() {
        UserRepo userRepo = mock(UserRepo.class);
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserService userService =
            new UserService(userRepo, passwordEncoder, jwtEncoder);

        User user = new User();
        user.setEmail("Taylor@Example.com");
        user.setPassword("customer-password");
        when(userRepo.findByEmailIgnoreCase("taylor@example.com"))
            .thenReturn(Optional.empty(), Optional.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(7L);
            return savedUser;
        });
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
            .thenReturn(Jwt.withTokenValue("signed-token")
                .header("alg", "HS256")
                .subject("taylor@example.com")
                .build());

        User savedUser = userService.createUser(user);

        assertNotEquals("customer-password", savedUser.getPassword());
        assertTrue(passwordEncoder.matches(
            "customer-password", savedUser.getPassword()));

        var login = userService.login(
            new LoginRequest("taylor@example.com", "customer-password"));

        assertEquals("signed-token", login.accessToken());
        assertEquals(7L, login.userId());
    }
}
