package com.back.domain.review.review.repository;

import com.back.domain.review.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface ReviewRepository extends JpaRepository<Review, Integer> {
}
