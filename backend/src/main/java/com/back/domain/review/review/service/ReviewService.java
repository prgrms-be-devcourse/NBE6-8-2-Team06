package com.back.domain.review.review.service;

import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public Optional<Review> findLatest(){
        return reviewRepository.findFirstByOrderByIdDesc();
    }
}
