package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.note.dto.NoteDto;
import com.back.domain.review.review.entity.Review;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.time.LocalDateTime;
import java.util.List;

public record BookmarkDetailDto(
        @JsonUnwrapped
        BookmarkDto bookmarkDto,
        LocalDateTime createAt,
        LocalDateTime startReadDate,
        LocalDateTime endReadDate,
        long readingDuration,
        List<NoteDto> notes
) {
    public BookmarkDetailDto(Bookmark bookmark, Review review) {
        this(
                new BookmarkDto(bookmark, review),
                bookmark.getCreateDate(),
                bookmark.getStartReadDate(),
                bookmark.getEndReadDate(),
                bookmark.calculateReadingDuration(),
                bookmark.getNotes().stream().map(NoteDto::new).toList()
        );
    }
}
