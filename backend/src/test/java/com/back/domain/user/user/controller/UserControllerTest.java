package com.back.domain.user.user.controller;

import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // Spring Security 필터 비활성화
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("정상적인 회원가입 요청 시 성공")
    void signUp_Success() throws Exception {
        // given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "name": "홍길동",
                    "password": "password123!"
                }
                """;

        // User 엔티티 생성 - 생성자 파라미터 순서 주의
        User mockUser = mock(User.class);
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.getName()).thenReturn("홍길동");
        when(mockUser.getPassword()).thenReturn("encodedPassword");

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123!")).thenReturn("encodedPassword");
        when(userService.join("test@example.com", "홍길동", "encodedPassword")).thenReturn(mockUser);

        // when & then
        mockMvc.perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("홍길동님 환영합니다. Bookers 회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.passward").value("encodedPassword"));

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(userService, times(1)).join("test@example.com", "홍길동", "encodedPassword");
        verify(passwordEncoder, times(1)).encode("password123!");
    }
}