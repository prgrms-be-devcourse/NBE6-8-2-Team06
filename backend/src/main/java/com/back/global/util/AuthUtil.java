package com.back.global.util;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.standard.util.Ut;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUtil {
    private final MemberRepository memberRepository;

    @Value("${custom.jwt.secretKey}")
    private String secretKey;

    public Member getMemberFromRequest(HttpServletRequest request) {
        try {
            String token = extractAccessTokenFromCookie(request);
            if (token == null || !Ut.jwt.isValid(secretKey, token)) {
                log.debug("유효하지 않은 토큰 또는 토큰 없음 (비로그인 상태)");
                return null;
            }

            Map<String, Object> payload = Ut.jwt.payload(secretKey, token);
            String email = (String) payload.get("email");

            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null) {
                log.debug("토큰에서 사용자 정보 추출 성공: {}", email);
            } else {
                log.warn("토큰은 유효하지만 사용자 정보를 찾을 수 없음: {}", email);
            }

            return member;
        } catch (Exception e) {
            log.debug("토큰 파싱 실패 (비로그인 상태): {}", e.getMessage());
            return null;
        }
    }

    private String extractAccessTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}