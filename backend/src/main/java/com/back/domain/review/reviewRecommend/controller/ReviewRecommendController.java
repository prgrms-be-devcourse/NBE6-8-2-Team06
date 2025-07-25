package com.back.domain.review.reviewRecommend.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.service.ReviewService;
import com.back.domain.review.reviewRecommend.service.ReviewRecommendService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews/{book_id}/recommend")
@RequiredArgsConstructor
public class ReviewRecommendController {
    private final Rq rq;
    private final BookRepository bookRepository;
    private final ReviewService reviewService;
    private final ReviewRecommendService reviewRecommendService;


    @PostMapping("{isRecommend}")
    public RsData<Void> recommendReview(@PathVariable("book_id") int bookId, @PathVariable("isRecommend") boolean isRecommend) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ServiceException("404-1", "Book not found"));
        Member member = rq.getActor();
        if (member == null) {
            return new RsData<>("401-1", "Unauthorized access");
        }
        Review review = reviewService.findByBookAndMember(book, member)
                .orElseThrow(() -> new ServiceException("404-1", "Review not found"));
        reviewRecommendService.recommendReview(review, member, isRecommend);
        return new RsData<>("200-1", "Review recommended successfully");
    }

}
