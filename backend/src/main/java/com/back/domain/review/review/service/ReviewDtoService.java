package com.back.domain.review.review.service;

import com.back.domain.book.book.entity.Book;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.dto.ReviewRequestDto;
import com.back.domain.review.review.dto.ReviewResponseDto;
import com.back.domain.review.review.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
                .isRecommended(null)
                .createdDate(review.getCreateDate())
                .modifiedDate(review.getModifyDate())
                .build();
    }

    public Review reviewRequestDtoToReview(ReviewRequestDto reviewRequestDto, Member member, Book book) {
        return new Review(
                reviewRequestDto.content(),
                reviewRequestDto.rate(),
                member,
                book
        );
    }

    public void updateReviewFromRequest(Review review, ReviewRequestDto reviewRequestDto) {
        review.setContent(reviewRequestDto.content());
        review.setRate(reviewRequestDto.rate());
    }
}
