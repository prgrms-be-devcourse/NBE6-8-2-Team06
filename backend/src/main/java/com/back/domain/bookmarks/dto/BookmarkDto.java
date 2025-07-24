package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.note.dto.NoteDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public record BookmarkDto(
        int id,
        int user_id,
        int book_id,
        //book 내용 추가 예정
        String read_state,
        int read_page,
        LocalDateTime created_at,
        LocalDateTime read_start_at,
        LocalDateTime read_end_at,
        long reading_duration,
        //내가 쓴 리뷰 추가 예정
        List<NoteDto> notes
) {
    public BookmarkDto(Bookmark bookmark){
        this(
                bookmark.getId(),
                bookmark.getUser().getId(),
                bookmark.getBook().getId(),
                bookmark.getReadState().toString(),
                bookmark.getReadPage(),
                bookmark.getCreateDate(),
                bookmark.getStartReadTime(),
                bookmark.getEndReadTime(),
                calculateReadingDuration(bookmark.getStartReadTime(), bookmark.getEndReadTime()),
                bookmark.getNotes().stream().map(NoteDto::new).collect(Collectors.toList())
        );
    }
    private static long calculateReadingDuration(LocalDateTime start, LocalDateTime end){
        LocalDateTime effectiveEnd = (end == null) ? LocalDateTime.now() : end;
        return ChronoUnit.DAYS.between(start, effectiveEnd);
    }
}
