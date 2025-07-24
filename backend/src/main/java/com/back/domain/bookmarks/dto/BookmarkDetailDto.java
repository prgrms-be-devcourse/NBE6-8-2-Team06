package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.note.dto.NoteDto;
import com.back.domain.review.review.entity.Review;

import java.time.LocalDateTime;
import java.util.List;

public record BookmarkDetailDto(
        BookmarkDto bookmarkDto,
        LocalDateTime createAt,
        LocalDateTime startReadDate,
        LocalDateTime endReadDate,
        long readingDuration,
        BookmarkReviewDetailDto review,
        List<NoteDto> notes
) {
    public BookmarkDetailDto(Bookmark bookmark, Review  review) {
        this(
                new BookmarkDto(bookmark),
                bookmark.getCreateDate(),
                bookmark.getStartReadDate(),
                bookmark.getEndReadDate(),
                bookmark.calculateReadingDuration(),
                new BookmarkReviewDetailDto(review),
                bookmark.getNotes().stream().map(NoteDto::new).toList()
        );
    }
}
