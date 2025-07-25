package com.back.domain.review.reviewRecommend.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.review.review.controller.ReviewController;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.service.ReviewService;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Rollback
public class ReviewRecommendController {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ReviewService reviewService;

    @Test
    void t1() throws Exception{
        Member member = memberService.findByEmail("email").get();
        String accessToken = memberService.geneAccessToken(member);
        mvc.perform(
                post("/reviews/{book_id}", 1)
                        .contentType("application/json")
                        .content("""
{
    "content": "이 책 정말 좋았어요!",
    "rate": 5
}
""").cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
        reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));


        ResultActions resultActions = mvc.perform(
                post("/reviews/{review_id}/recommend/{isRecommend}", 1, true)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("recommendReview"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("Review recommended successfully"))
        ;

    }

    @Test
    void t2() throws Exception{
        Member member = memberService.findByEmail("email").get();
        Member member2 = memberService.findByEmail("email2").get();
        String accessToken = memberService.geneAccessToken(member);
        String accessToken2 = memberService.geneAccessToken(member2);
        mvc.perform(
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
        mvc.perform(
                post("/reviews/{review_id}/recommend/{isRecommend}", review.getId(), true)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        ResultActions resultActions = mvc.perform(
                post("/reviews/{review_id}/recommend/{isRecommend}", review.getId(), false)
                        .cookie(new Cookie("accessToken", accessToken2))
        ).andDo(print());
        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("recommendReview"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("Review recommended successfully"))
        ;
        assertThat(review.getRecommendCount()).isEqualTo(2);
    }
}
