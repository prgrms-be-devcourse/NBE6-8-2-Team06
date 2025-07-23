package com.back.domain.review.review.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.service.BookService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final BookService bookService;

    @RequestMapping("/{book_id}")
    public RsData<Void> getReviews(@PathVariable("book_id") int bookId) {
        Book book = bookService.findById(bookId).orElseThrow(RuntimeException::new); // TODO need GlobalExceptionHandler and ServiceException


        return new RsData<>("200-1", "Reviews fetched successfully");
    }
}
