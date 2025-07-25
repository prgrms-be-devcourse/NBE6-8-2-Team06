package com.back.domain.review.review.service;

import com.back.domain.book.book.entity.Book;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.dto.ReviewRequestDto;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import com.back.global.exception.ServiceException;
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
                .orElseThrow(() -> new ServiceException("404-1","review not found"));
        reviewRepository.delete(review);
    }

    @Transactional
    public void modifyReview(Book book, Member member, ReviewRequestDto reviewRequestDto) {
        Review review = reviewRepository.findByBookAndMember(book, member)
                .orElseThrow(() -> new ServiceException("404-1","review not found"));
        review.setContent(reviewRequestDto.content());
        review.setRate(reviewRequestDto.rate());
        reviewRepository.save(review);
    }
}
