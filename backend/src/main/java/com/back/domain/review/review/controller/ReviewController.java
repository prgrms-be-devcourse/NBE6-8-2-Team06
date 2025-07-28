package com.back.domain.review.review.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;

import com.back.domain.member.member.service.MemberService;
import com.back.domain.review.review.dto.ReviewRequestDto;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.service.ReviewService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final BookRepository bookRepository;
    private final Rq rq;
    private final ReviewRecommendService reviewRecommendService;

    @PostMapping("/{book_id}")
    public RsData<Void> create(@PathVariable("book_id") int bookId, @RequestBody ReviewRequestDto reviewRequestDto) {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new ServiceException("404-1", "Book not found"));
        Member member = rq.getActor();
        reviewService.addReview(book, member, reviewRequestDto);
        return new RsData<>("201-1", "Reviews fetched successfully");
    }

    @DeleteMapping("/{book_id}")
    public RsData<Void> delete(@PathVariable("book_id") int bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ServiceException("404-1", "Book not found"));
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        reviewService.deleteReview(book, member);
        return new RsData<>("200-1", "Review deleted successfully");
    }

    @PutMapping("/{book_id}")
    public RsData<Void> modify(@PathVariable("book_id") int bookId, @RequestBody ReviewRequestDto reviewRequestDto) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ServiceException("404-1", "Book not found"));
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        reviewService.modifyReview(book, member, reviewRequestDto);
        return new RsData<>("200-1", "Review modified successfully");
    }

    @PostMapping("/{review_id}/recommend/{is_recommend}")
    public RsData<Void> recommendReview(@PathVariable("review_id") int reviewId, @PathVariable("is_recommend") boolean isRecommend) {
        Review review = reviewService.findById(reviewId).orElseThrow(()->new ServiceException("404-1", "Review not found"));
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        reviewRecommendService.recommendReview(review, member, isRecommend);
        return new RsData<>("201-1", "Review recommended successfully");
    }
}
