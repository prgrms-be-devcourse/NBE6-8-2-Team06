package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.entity.Bookmark;

import java.time.LocalDateTime;

public record BookmarkDto(
        int id,
        int memberId,
        int bookId,
        BookmarkBookDetailDto book,
        String readState,
        int readPage,
        LocalDateTime date,
        double readingRate
) {
    public BookmarkDto(Bookmark bookmark) {
        this(
                bookmark.getId(),
                bookmark.getMember().getId(),
                bookmark.getBook().getId(),
                new BookmarkBookDetailDto(bookmark.getBook()),
                bookmark.getReadState().toString(),
                bookmark.getReadPage(),
                bookmark.getDisplayDate(),
                bookmark.calculateReadingRate()
        );
    }
}
