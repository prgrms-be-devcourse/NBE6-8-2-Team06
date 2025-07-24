package com.back.domain.review.review.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.service.ReviewService;
import com.back.global.rq.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
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

    @Test
    @DisplayName("리뷰 작성")
    void t1() throws Exception {
        ResultActions resultActions = mvc.perform(
                post("/api/v1/reviews/{book_id}", 1)
                        .contentType("application/json")
                        .content("""
{
    "content": "이 책 정말 좋았어요!",
    "rate": 5
}
""")
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
