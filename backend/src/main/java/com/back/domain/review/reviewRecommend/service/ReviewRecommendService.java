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

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReviewRecommendService {
    private final ReviewService reviewService;
    private final ReviewRecommendRepository reviewRecommendRepository;

    @Transactional
    public void recommendReview(int reviewId, Member member, boolean isRecommend) {
        Review review = reviewService.findById(reviewId).orElseThrow(()->new NoSuchElementException("Review not found"));
        ReviewRecommend reviewRecommend = new ReviewRecommend(review, member, isRecommend);
        if (reviewRecommendRepository.findByReviewAndMember(review, member).isPresent()) {
            throw new ServiceException("400-1", "Review recommendation already exists");
        }
        reviewRecommendRepository.save(reviewRecommend);
        if (isRecommend) {
            review.setLikeCount(review.getLikeCount() + 1);
        } else {
            review.setDislikeCount(review.getDislikeCount() + 1);
        }
    }

    @Transactional
    public void modifyRecommendReview(int reviewId, Member member, boolean isRecommend) {
        Review review = reviewService.findById(reviewId).orElseThrow(()->new NoSuchElementException("Review not found"));
        ReviewRecommend reviewRecommend = reviewRecommendRepository.findByReviewAndMember(review, member)
                .orElseThrow(() -> new NoSuchElementException("Review recommendation not found"));
        if (reviewRecommend.isRecommended() == isRecommend) {
            throw new ServiceException("400-2", "Review recommendation already set to this value");
        }
        reviewRecommend.setRecommended(isRecommend);
        reviewRecommendRepository.save(reviewRecommend);
        int likeCount = review.getLikeCount();
        int dislikeCount = review.getDislikeCount();
        if (isRecommend) {
            likeCount++;
            dislikeCount--;
        }else{
            likeCount--;
            dislikeCount++;
        }
        review.setLikeCount(likeCount);
        review.setDislikeCount(dislikeCount);
    }

    @Transactional
    public void cancelRecommendReview(int reviewId, Member member) {
        Review review = reviewService.findById(reviewId).orElseThrow(()->new NoSuchElementException("Review not found"));
        ReviewRecommend reviewRecommend = reviewRecommendRepository.findByReviewAndMember(review, member)
                .orElseThrow(() -> new NoSuchElementException("Review recommendation not found"));
        reviewRecommendRepository.delete(reviewRecommend);
        if (reviewRecommend.isRecommended()) {
            review.setLikeCount(review.getLikeCount() - 1);
        }else{
            review.setDislikeCount(review.getDislikeCount() - 1);
        }
    }

    public Boolean isRecommended(Review review, Member member) {
        return reviewRecommendRepository.findByReviewAndMember(review, member)
                .map(ReviewRecommend::isRecommended)
                .orElse(null);
    }
}
