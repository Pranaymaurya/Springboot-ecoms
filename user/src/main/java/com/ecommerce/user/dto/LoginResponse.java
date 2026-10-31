package com.ecommerce.user.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    Long userId
) {}
