package com.back.domain.review.reviewRecommend.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.service.ReviewService;
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
    private final ReviewService reviewService;

    @Transactional
    public void recommendReview(Review review, Member member, boolean isRecommend) {
        ReviewRecommend reviewRecommend = new ReviewRecommend(review, member, isRecommend);
        if (reviewRecommendRepository.findByReviewAndMember(review, member).isPresent()) {
            throw new ServiceException("400-1", "Review recommendation already exists");
        }
        review.getReviewRecommends().add(reviewRecommend);
        review.setLikeCount((int)review.getReviewRecommends().stream().filter(ReviewRecommend::isRecommended).count());
        review.setDislikeCount((int)review.getReviewRecommends().stream().filter(reviewRecommend1 -> !reviewRecommend1.isRecommended()).count());
        reviewRecommendRepository.save(reviewRecommend);
    }
}
