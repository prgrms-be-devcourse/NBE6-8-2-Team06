package com.back.domain.note.dto;

import com.back.domain.book.book.entity.Book;
import org.springframework.lang.NonNull;

import java.util.List;

public record NotePageDto(
        List<NoteDto> notes,
        @NonNull String imageUrl,
        @NonNull String title
) {
    public NotePageDto(List<NoteDto> notes, Book book) {
        this(
                notes,
                book.getImageUrl(),
                book.getTitle()
        );
    }
}
