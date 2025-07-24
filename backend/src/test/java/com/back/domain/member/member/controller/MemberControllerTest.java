package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rq.Rq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberService memberService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Rq rq;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("회원가입 성공")
    void t1() throws Exception {
        // Given
        String email = "test@example.com";
        String name = "TestUser";
        String password = "password123";
        String encodedPassword = "encodedPassword123";

        MemberController.MemberJoinReqBody reqBody = new MemberController.MemberJoinReqBody(email, name, password);
        Member newMember = new Member(name, email, encodedPassword);

        when(memberService.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(memberService.join(name, email, encodedPassword)).thenReturn(newMember);

        // When & Then
        mockMvc.perform(post("/member/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%s님 환영합니다. Bookers 회원가입이 완료되었습니다.".formatted(name)))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.name").value(name));

        verify(memberService, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(memberService, times(1)).join(name, email, encodedPassword);
    }

    @Test
    @DisplayName("로그인 성공")
    void t2() throws Exception {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        String accessToken = "mockAccessToken";

        MemberController.MemberLoginReqBody reqBody = new MemberController.MemberLoginReqBody(email, password);
        Member existingMember = new Member("TestUser", email, encodedPassword);

        when(memberService.findByEmail(email)).thenReturn(Optional.of(existingMember));
        doNothing().when(memberService).checkPassword(existingMember, password);
        when(memberService.geneAccessToken(existingMember)).thenReturn(accessToken);

        // Mock the setCookie method of Rq
        doNothing().when(rq).setCookie(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/member/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%s님 환영합니다.".formatted(email)))
                .andExpect(jsonPath("$.data.memDto.email").value(email))
                .andExpect(jsonPath("$.data.accessToken").value(accessToken));


        verify(memberService, times(1)).findByEmail(email);
        verify(memberService, times(1)).checkPassword(existingMember, password);
        verify(memberService, times(1)).geneAccessToken(existingMember);

        verify(rq, times(1)).setCookie("accessToken", accessToken);
    }
}