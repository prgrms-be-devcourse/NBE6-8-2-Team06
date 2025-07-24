package com.back.domain.review.review.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.service.ReviewService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ReviewControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AuthTokenService authTokenService;

    @Test
    @DisplayName("리뷰 작성")
    void t1() throws Exception {
        Member member = memberService.findByEmail("email").get();
        String accessToken = authTokenService.genAccessToken(member);
        ResultActions resultActions = mvc.perform(
                post("/reviews/{book_id}", 1)
                        .contentType("application/json")
                        .content("""
{
    "content": "이 책 정말 좋았어요!",
    "rate": 5
}
""").cookie(new Cookie("accessToken", accessToken))
                ).andDo(print());
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("create"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("Reviews fetched successfully"))
                ;

        assertThat(review.getId()).isEqualTo(1);
    }


}
