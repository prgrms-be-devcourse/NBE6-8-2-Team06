package com.back.domain.review.review.dto;

import com.back.domain.review.review.entity.Review;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewWithIdDto(
        @NotNull int id,
        @NotBlank String content,
        @NotNull int rate
) {
    public ReviewWithIdDto(Review review){
        this(
                review.getId(),
                review.getContent(),
                review.getRate()
        );
    }
}
