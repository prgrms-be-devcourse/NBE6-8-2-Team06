package com.back.domain.review.review.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.service.BookService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.dto.ReviewRequestDto;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.service.ReviewService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final BookService bookService;
    private final Rq rq;

    @RequestMapping("/{book_id}")
    public RsData<Void> getReviews(@PathVariable("book_id") int bookId, @RequestBody ReviewRequestDto reviewRequestDto) {
        Book book = bookService.findById(bookId).orElseThrow(()->new ServiceException("404-1", "Book not found"));
        Member member = rq.getActor();
        reviewService.addReview(book, member, reviewRequestDto);
        return new RsData<>("200-1", "Reviews fetched successfully");
    }
}
