package com.back.domain.bookmarks.dto;

import com.back.domain.review.review.entity.Review;

public record BookmarkReviewDetailDto(
        int id,
        String content,
        double rate
) {
    public BookmarkReviewDetailDto(Review review){
        this(
                review.getId(),
                review.getContent(),
                review.getRate()
        );
    }
}
