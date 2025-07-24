package com.back.domain.user.user.service;

import com.back.domain.user.user.entity.User;
import com.back.global.standard.util.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class AuthTokenService {
    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.jwt.expirationSeconds}")
    private int accessTokenExpSec;

    String genAccessToken(User user) {
        int id = user.getId();
        String email = user.getEmail();

        return Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpSec,
                Map.of(
                        "id",id,
                        "email",email
                )
        );
    }
    public Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken);

        if(parsedPayload == null) return null;
        int id = (int) parsedPayload.get("id");
        String email = (String) parsedPayload.get("email");

        return Map.of("id", id, "email", email);
    }
    public boolean isValid(String accessToken) { return Ut.jwt.isValid(jwtSecretKey, accessToken); }
}
