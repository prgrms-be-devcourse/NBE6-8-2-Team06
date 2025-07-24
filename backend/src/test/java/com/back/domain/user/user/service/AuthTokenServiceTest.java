package com.back.domain.user.user.service;

import com.back.domain.user.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthTokenService 테스트")
class AuthTokenServiceTest {

    @InjectMocks
    private AuthTokenService authTokenService;

    private User testUser;
    private String jwtSecretKey = "testSecretKey545348354897892318523489523445964345";
    private int accessTokenExpSec = 3600; // 1시간

    @BeforeEach
    void setUp() {
        // private 필드에 값 주입
        ReflectionTestUtils.setField(authTokenService, "jwtSecretKey", jwtSecretKey);
        ReflectionTestUtils.setField(authTokenService, "accessTokenExpSec", accessTokenExpSec);

        // 테스트용 User 객체 생성
        testUser = new User("testuser", "test@example.com", "password");
        // BaseEntity의 ID 설정을 위해 리플렉션 사용
        ReflectionTestUtils.setField(testUser, "id", 1);
    }

    @Test
    @DisplayName("JWT 토큰 생성 성공 테스트")
    void t1() {
        // genAccessToken은 package-private이므로 리플렉션 사용
        String accessToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testUser);


        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();
        
        // 생성된 토큰이 유효한지 확인
        assertThat(authTokenService.isValid(accessToken)).isTrue();
        
        // payload 확인
        Map<String, Object> payload = authTokenService.payload(accessToken);
        assertThat(payload).isNotNull();
        assertThat(payload.get("id")).isEqualTo(testUser.getId());
        assertThat(payload.get("email")).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("유효한 JWT 토큰 검증 성공 테스트")
    void t2() {

        String validToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testUser);


        boolean isValid = authTokenService.isValid(validToken);


        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 JWT 토큰 검증 실패 테스트")
    void t3() {

        String invalidToken = "invalid.jwt.token";


        boolean isValid = authTokenService.isValid(invalidToken);


        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 JWT 토큰 검증 실패 테스트")
    void t4() {

        // 만료 시간을 1초로 설정하여 즉시 만료되는 토큰 생성
        ReflectionTestUtils.setField(authTokenService, "accessTokenExpSec", 1);
        String expiredToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testUser);
        
        // 토큰이 만료되도록 2초 대기
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        boolean isValid = authTokenService.isValid(expiredToken);


        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("유효한 토큰에서 payload 추출 성공 테스트")
    void t5() {

        String validToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testUser);


        Map<String, Object> payload = authTokenService.payload(validToken);


        assertThat(payload).isNotNull();
        assertThat(payload).hasSize(2);
        assertThat(payload.get("id")).isEqualTo(testUser.getId());
        assertThat(payload.get("email")).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("잘못된 토큰에서 payload 추출 실패 테스트")
    void t6() {

        String invalidToken = "invalid.jwt.token";


        Map<String, Object> payload = authTokenService.payload(invalidToken);


        assertThat(payload).isNull();
    }

    @Test
    @DisplayName("다른 시크릿 키로 서명된 토큰 검증 실패 테스트")
    void t7() {

        String validToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testUser);
        
        // 다른 시크릿 키로 변경
        ReflectionTestUtils.setField(authTokenService, "jwtSecretKey", "differentSecretKey249842348974897988656456");


        boolean isValid = authTokenService.isValid(validToken);


        assertThat(isValid).isFalse();
    }

}
