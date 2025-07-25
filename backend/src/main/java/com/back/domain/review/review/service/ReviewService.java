package com.back.domain.review.review.service;

import com.back.domain.book.book.entity.Book;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.dto.ReviewRequestDto;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void addReview(Book book, Member member, ReviewRequestDto reviewRequestDto){
        Review review = new Review(reviewRequestDto.content(), reviewRequestDto.rate(), member, book);
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Book book, Member member) {
        Review review = reviewRepository.findByBookAndMember(book, member)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for the given book and member"));
        reviewRepository.delete(review);
    }
}
