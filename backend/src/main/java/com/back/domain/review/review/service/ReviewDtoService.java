package com.back.domain.review.review.service;

import com.back.domain.review.review.dto.ReviewResponseDto;
import com.back.domain.review.review.entity.Review;
import org.springframework.stereotype.Service;

@Service
public class ReviewDtoService {
    public ReviewResponseDto reviewToReviewResponseDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rate(review.getRate())
                .memberName(review.getMember().getName())
                .memberId(review.getMember().getId())
                .likeCount(review.getLikeCount())
                .dislikeCount(review.getDislikeCount())
                .createdDate(review.getCreateDate())
                .modifiedDate(review.getModifyDate())
                .build();
    }
}
