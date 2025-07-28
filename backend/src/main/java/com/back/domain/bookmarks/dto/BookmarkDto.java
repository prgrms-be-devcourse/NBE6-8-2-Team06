package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.review.review.entity.Review;

import java.time.LocalDateTime;

public record BookmarkDto(
        int id,
        int bookId,
        BookmarkBookDetailDto book,
        String readState,
        int readPage,
        LocalDateTime date,
        double readingRate,
        BookmarkReviewDetailDto review
) {
    public BookmarkDto(Bookmark bookmark, Review review) {
        this(
                bookmark.getId(),
                bookmark.getBook().getId(),
                new BookmarkBookDetailDto(bookmark.getBook()),
                bookmark.getReadState().toString(),
                bookmark.getReadPage(),
                bookmark.getDisplayDate(),
                bookmark.calculateReadingRate(),
                review != null ? new BookmarkReviewDetailDto(review):null
        );
    }
}
