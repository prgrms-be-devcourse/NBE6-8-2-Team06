package com.back.domain.review.reviewRecommend.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.reviewRecommend.entity.ReviewRecommend;
import com.back.domain.review.reviewRecommend.repository.ReviewRecommendRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReviewRecommendService {
    private final ReviewRecommendRepository reviewRecommendRepository;

    @Transactional
    public void recommendReview(Review review, Member member, boolean isRecommend) {
        ReviewRecommend reviewRecommend = new ReviewRecommend(review, member, isRecommend);
        if (reviewRecommendRepository.findByReviewAndMember(review, member).isPresent()) {
            throw new ServiceException("400-1", "Review recommendation already exists");
        }
        reviewRecommendRepository.save(reviewRecommend);
        if (isRecommend) {
            review.setLikeCount(reviewRecommendRepository.countByReviewAndIsRecommendedTrue(review));
        } else {
            review.setDislikeCount(reviewRecommendRepository.countByReviewAndIsRecommendedFalse(review));
        }
    }

    @Transactional
    public void modifyRecommendReview(Review review, Member member, boolean isRecommend) {
        ReviewRecommend reviewRecommend = reviewRecommendRepository.findByReviewAndMember(review, member)
                .orElseThrow(() -> new NoSuchElementException("Review recommendation not found"));
        if (reviewRecommend.isRecommended() == isRecommend) {
            throw new ServiceException("400-2", "Review recommendation already set to this value");
        }
        reviewRecommend.setRecommended(isRecommend);
        reviewRecommendRepository.save(reviewRecommend);
        review.setLikeCount(reviewRecommendRepository.countByReviewAndIsRecommendedTrue(review));
        review.setDislikeCount(reviewRecommendRepository.countByReviewAndIsRecommendedFalse(review));
    }
}
