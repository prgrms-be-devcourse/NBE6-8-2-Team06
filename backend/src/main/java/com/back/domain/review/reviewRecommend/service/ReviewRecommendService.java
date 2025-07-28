package com.back.domain.review.reviewRecommend.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.reviewRecommend.entity.ReviewRecommend;
import com.back.domain.review.reviewRecommend.repository.ReviewRecommendRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
