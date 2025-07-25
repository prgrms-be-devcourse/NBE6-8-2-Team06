package com.back.domain.review.review.service;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.service.BookService;
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
    private final BookService bookService;

    public Optional<Review> findLatest(){
        return reviewRepository.findFirstByOrderByIdDesc();
    }

    @Transactional
    public void addReview(Book book, Member member, ReviewRequestDto reviewRequestDto){
        Review review = new Review(reviewRequestDto.content(), reviewRequestDto.rate(), member, book);
        if (reviewRepository.findByBookAndMember(book, member).isPresent()) {
            throw new ServiceException("400-1", "Review already exists");
        }
        reviewRepository.save(review);
        bookService.updateBookAvgRate(book);
    }

    @Transactional
    public void deleteReview(Book book, Member member) {
        Review review = reviewRepository.findByBookAndMember(book, member)
                .orElseThrow(() -> new ServiceException("404-1","review not found"));
        reviewRepository.delete(review);
        bookService.updateBookAvgRate(book);
    }

    @Transactional
    public void modifyReview(Book book, Member member, ReviewRequestDto reviewRequestDto) {
        Review review = reviewRepository.findByBookAndMember(book, member)
                .orElseThrow(() -> new ServiceException("404-1","review not found"));
        review.setContent(reviewRequestDto.content());
        review.setRate(reviewRequestDto.rate());
        reviewRepository.save(review);
        bookService.updateBookAvgRate(book);
    }

    public long count() {
        return reviewRepository.count();
    }
}
