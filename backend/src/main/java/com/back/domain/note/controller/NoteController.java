package com.back.domain.note.controller;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.note.dto.NoteDto;
import com.back.domain.note.entity.Note;
import com.back.domain.note.service.NoteService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("bookmarks/{bookmarkId}/notes")
public class NoteController {
    private final NoteService noteService;

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "노트 다건 조회")
    public List<NoteDto> getItems(@PathVariable int bookmarkId) {
        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();

        return bookmark
                .getNotes()
                .stream()
                .map(note -> new NoteDto(note))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "노트 단건 조회")
    public NoteDto getItem(
            @PathVariable int bookmarkId,
            @PathVariable int id) {
        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();

        Note note = noteService.findNoteById(bookmark, id).get();

        return new NoteDto(note);
    }


    record NoteWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String title,
            @NotBlank
            @Size(min = 2, max = 1000)
            String content
    ) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "노트 작성")
    public RsData<NoteDto> write(
            @PathVariable int bookmarkId,
            @Valid @RequestBody NoteWriteReqBody reqBody
    ) {
        Note note = noteService.write(bookmarkId, reqBody.title, reqBody.content);

        // 미리 db에 반영
        noteService.flush();

        return new RsData<>(
                "201-1",
                "%d번 노트가 작성되었습니다.".formatted(note.getId()),
                new NoteDto(note)
        );
    }


    record NoteModifyReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String title,
            @NotBlank
            @Size(min = 2, max = 1000)
            String content
    ) {
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @Operation(summary = "노트 수정")
    public RsData<Void> modify(
            @PathVariable int bookmarkId,
            @PathVariable int id,
            @Valid @RequestBody NoteModifyReqBody reqBody
    ) {
        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();

        Note note = noteService.findNoteById(bookmark, id).get();

        noteService.modify(note, reqBody.title, reqBody.content);

        return new RsData<>(
                "200-1",
                "%d번 노트가 수정되었습니다.".formatted(id)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @Operation(summary = "노트 삭제")
    public RsData<Void> delete(
            @PathVariable int bookmarkId,
            @PathVariable int id
    ) {
        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();

        Note note = noteService.findNoteById(bookmark, id).get();

        noteService.delete(bookmark, note);

        return new RsData<>(
                "200-1",
                "%d번 노트가 삭제되었습니다.".formatted(id)
        );
    }
}
