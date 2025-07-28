package com.back.domain.note.dto;

import com.back.domain.note.entity.Note;
import org.springframework.lang.NonNull;

public record NoteDto(
        @NonNull int id,
        @NonNull String createDate,
        @NonNull String modifyDate,
        @NonNull String title,
        @NonNull String content,
        String page
) {
    public NoteDto(Note note) {
        this(
                note.getId(),
                note.CreateDateParsing(note.getCreateDate()),
                note.UpdateDateParsing(note.getModifyDate()),
                note.getTitle(),
                note.getContent(),
                note.getPage()
        );
    }
}
