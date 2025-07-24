package com.back.domain.review.review.repository;

import com.back.domain.review.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findFirstByOrderByIdDesc();
}
